package SparkLDA

import java.text.BreakIterator

import org.apache.spark.input.PortableDataStream
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{AutoDetectParser, ParseContext}
import org.apache.tika.sax.BodyContentHandler
import scopt.OptionParser

import scala.collection.mutable

object SparkLDAMain {
  private val usableMetadataArray = Array[String]("content", "created", "creator", "created", "title", "Author", "producer", "Content-Type", "Message-From", "Message-To", "Message-Cc", "subject", "Last-Author")

  private case class Params(
                            input: Seq[String] = Seq.empty,
                            k: Int = 20,
                            maxIterations: Int = 10,
                            docConcentration: Double = -1,
                            topicConcentration: Double = -1,
                            vocabSize: Int = 10000,
                            stopwordFile: String = "",
                            checkpointDir: Option[String] = None,
                            checkpointInterval: Int = 10) extends AbstractParams[Params]

  def main(args: Array[String]) {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("LDAExample") {
      head("LDAExample: an example LDA app for plain text data.")
      opt[Int]("k")
        .text(s"number of topics. default: ${defaultParams.k}")
        .action((x, c) => c.copy(k = x))
      opt[Int]("maxIterations")
        .text(s"number of iterations of learning. default: ${defaultParams.maxIterations}")
        .action((x, c) => c.copy(maxIterations = x))
      opt[Double]("docConcentration")
        .text(s"amount of topic smoothing to use (> 1.0) (-1=auto)." +
          s"  default: ${defaultParams.docConcentration}")
        .action((x, c) => c.copy(docConcentration = x))
      opt[Double]("topicConcentration")
        .text(s"amount of term (word) smoothing to use (> 1.0) (-1=auto)." +
          s"  default: ${defaultParams.topicConcentration}")
        .action((x, c) => c.copy(topicConcentration = x))
      opt[Int]("vocabSize")
        .text(s"number of distinct word types to use, chosen by frequency. (-1=all)" +
          s"  default: ${defaultParams.vocabSize}")
        .action((x, c) => c.copy(vocabSize = x))
      opt[String]("stopwordFile")
        .text(s"filepath for a list of stopwords. Note: This must fit on a single machine." +
          s"  default: ${defaultParams.stopwordFile}")
        .action((x, c) => c.copy(stopwordFile = x))
      opt[String]("checkpointDir")
        .text(s"Directory for checkpointing intermediate results." +
          s"  Checkpointing helps with recovery and eliminates temporary shuffle files on disk." +
          s"  default: ${defaultParams.checkpointDir}")
        .action((x, c) => c.copy(checkpointDir = Some(x)))
      opt[Int]("checkpointInterval")
        .text(s"Iterations between each checkpoint.  Only used if checkpointDir is set." +
          s" default: ${defaultParams.checkpointInterval}")
        .action((x, c) => c.copy(checkpointInterval = x))
      arg[String]("<input>...")
        .text("input paths (directories) to plain text corpora." +
          "  Each text file line should hold 1 document.")
        .unbounded()
        .required()
        .action((x, c) => c.copy(input = c.input :+ x))
    }

    parser.parse(args, defaultParams).map { params =>
      run(params)
    }.getOrElse {
      parser.showUsageAsError
      sys.exit(1)
    }
  }

  private def run(params: Params): Unit = {
    val conf = new SparkConf().setAppName("LDA with Tika Processing")
    val sc = new SparkContext(conf)

    val rawFiles = sc.binaryFiles(params.input.mkString)
    System.out.println(rawFiles.count())

    val wordsCorpus = rawFiles.mapPartitions(iter => {
      val parser = new AutoDetectParser()
      val myList = iter.toList
      myList.map(f => getDocumentText(parser, f._2)).iterator
    })
    wordsCorpus.cache()
    System.out.println(wordsCorpus.count())

    val preprocessStart = System.nanoTime()
    val (corpus, vocabArray, actualNumTokens) =
      preprocess(wordsCorpus, params.vocabSize)
    corpus.cache()
    val actualCorpusSize = corpus.count()
    val actualVocabSize = vocabArray.length
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9

    println()
    println(s"Corpus summary:")
    println(s"\t Training set size: $actualCorpusSize documents")
    println(s"\t Vocabulary size: $actualVocabSize terms")
    println(s"\t Training set size: $actualNumTokens tokens")
    println(s"\t Preprocessing time: $preprocessElapsed sec")
    println()

    val lda = new LDA()
    lda.setK(params.k)
      .setMaxIterations(params.maxIterations)
      .setDocConcentration(params.docConcentration)
      .setTopicConcentration(params.topicConcentration)
      .setCheckpointInterval(params.checkpointInterval)
    if (params.checkpointDir.nonEmpty) {
      sc.setCheckpointDir(params.checkpointDir.get)
    }
    val startTime = System.nanoTime()
    val ldaModel = lda.run(corpus)
    val elapsed = (System.nanoTime() - startTime) / 1e9

    println(s"Finished training LDA model.  Summary:")
    println(s"\t Training time: $elapsed sec")
    val avgLogLikelihood = ldaModel.logLikelihood / actualCorpusSize.toDouble
    println(s"\t Training data average log likelihood: $avgLogLikelihood")
    println()

    // Print the topics, showing the top-weighted terms for each topic.
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 10)
    val topics = topicIndices.map { case (terms, termWeights) =>
      terms.zip(termWeights).map { case (term, weight) => (vocabArray(term.toInt), weight) }
    }
    println(s"${params.k} topics:")
    topics.zipWithIndex.foreach { case (topic, i) =>
      println(s"TOPIC $i")
      topic.foreach { case (term, weight) =>
        println(s"$term\t$weight")
      }
      println()
    }
    sc.stop()

  }

  def getDocumentText(parser:AutoDetectParser, pds:PortableDataStream) : String = {
    val handler = new BodyContentHandler(-1)
    val metadata = new Metadata
    val context = new ParseContext
    //parsing the file
    parser.parse(pds.open(), handler, metadata, context)
    metadata.set("content", handler.toString)

    metadata.names().filter(s => usableMetadataArray.contains(s)).map(s => metadata.get(s)).mkString(" ")
  }

  private def preprocess(rddString: RDD[String],
                          vocabSize: Int): (RDD[(Long, Vector)], Array[String], Long) = {

    // Get dataset of document texts
    // One document per line in each text file.
    val textRDD: RDD[String] = rddString

    // Split text into words
    val tokenizer = new SimpleTokenizer()
    val tokenized: RDD[(Long, IndexedSeq[String])] = textRDD.zipWithIndex().map { case (text, id) =>
      id -> tokenizer.getWords(text)
    }
    tokenized.cache()

    // Counts words: RDD[(word, wordCount)]
    val wordCounts: RDD[(String, Long)] = tokenized
      .flatMap { case (_, tokens) => tokens.map(_ -> 1L) }
      .reduceByKey(_ + _)
    wordCounts.cache()
    val fullVocabSize = wordCounts.count()
    // Select vocab
    //  (vocab: Map[word -> id], total tokens after selecting vocab)
    val (vocab: Map[String, Int], selectedTokenCount: Long) = {
      val tmpSortedWC: Array[(String, Long)] = if (vocabSize == -1 || fullVocabSize <= vocabSize) {
        // Use all terms
        wordCounts.collect().sortBy(-_._2)
      } else {
        // Sort terms to select vocab
        wordCounts.sortBy(_._2, ascending = false).take(vocabSize)
      }
      (tmpSortedWC.map(_._1).zipWithIndex.toMap, tmpSortedWC.map(_._2).sum)
    }

    val documents = tokenized.map { case (id, tokens) =>
      // Filter tokens by vocabulary, and create word count vector representation of document.
      val wc = new mutable.HashMap[Int, Int]()
      tokens.foreach { term =>
        if (vocab.contains(term)) {
          val termIndex = vocab(term)
          wc(termIndex) = wc.getOrElse(termIndex, 0) + 1
        }
      }
      val indices = wc.keys.toArray.sorted
      val values = indices.map(i => wc(i).toDouble)

      val sb = Vectors.sparse(vocab.size, indices, values)
      (id, sb)
    }

    val vocabArray = new Array[String](vocab.size)
    vocab.foreach { case (term, i) => vocabArray(i) = term }

    (documents, vocabArray, selectedTokenCount)
  }

  private class SimpleTokenizer() extends Serializable {

    private val stopwords: Set[String] =
      Set("a", "an", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "such",
        "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with")

    // Matches sequences of Unicode letters
    private val allWordRegex = "^(\\p{L}*)$".r

    // Ignore words shorter than this length.
    private val minWordLength = 3

    def getWords(text: String): IndexedSeq[String] = {

      val words = new mutable.ArrayBuffer[String]()

      // Use Java BreakIterator to tokenize text into words.
      val wb = BreakIterator.getWordInstance
      wb.setText(text)

      // current,end index start,end of each word
      var current = wb.first()
      var end = wb.next()
      while (end != BreakIterator.DONE) {
        // Convert to lowercase
        val word: String = text.substring(current, end).toLowerCase
        // Remove short words and strings that aren't only letters
        word match {
          case allWordRegex(w) if w.length >= minWordLength && !stopwords.contains(w) =>
            words += w
          case _ =>
        }

        current = end
        try {
          end = wb.next()
        } catch {
          case e: Exception =>
            // Ignore remaining text in line.
            // This is a known bug in BreakIterator (for some Java versions),
            // which fails when it sees certain characters.
            end = BreakIterator.DONE
        }
      }
      words
    }
  }
}
