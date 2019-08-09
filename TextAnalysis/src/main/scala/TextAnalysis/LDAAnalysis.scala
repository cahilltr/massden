package TextAnalysis

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * Created by cahillt on 5/4/16.
  */
object LDAAnalysis {

  def main( args:Array[String] ):Unit = {
    val conf = new SparkConf().setAppName("Text_Analysis").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val text = sc.textFile(args(0))

    val cleanedText = text
      .map(f => filterStopwordsAndSynonyms(f))
      .map(s => s.trim.split(' ').toSeq)

    val wordCounts = cleanedText
      .flatMap(i => i.iterator)
      .map(s => (s, 1))
      .reduceByKey((x,y) => x + y)
      .collect()

    val vocabArray = wordCounts.takeRight(wordCounts.size - 20).map(_._1)
    val vocab: Map[String, Int] = vocabArray.zipWithIndex.toMap

    val documents = cleanedText.zipWithIndex.map { case (tokens, id) =>
      val counts = new scala.collection.mutable.HashMap[Int, Double]()
      tokens.foreach { term =>
        if (vocab.contains(term)) {
          val idx = vocab(term)
          counts(idx) = counts.getOrElse(idx, 0.0) + 1.0
        }
      }
      (id, Vectors.sparse(vocab.size, counts.toSeq))
    }








    //    val parsedData = cleanedText.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble)))
//
//    val corpus = parsedData.zipWithIndex.map(_.swap).cache()

//    val ldaModel = new LDA().setK(3).run(documents)
//
//
//
//    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
//    val topics = ldaModel.topicsMatrix
//    for (topic <- Range(0, 3)) {
//      print("Topic " + topic + ":")
//      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
//      println()
//    }

    val numTopics = 10
    val lda = new LDA().setK(numTopics).setMaxIterations(10)

    val ldaModel = lda.run(documents)
    val avgLogLikelihood = ldaModel.logLikelihood / documents.count()

    // Print topics, showing top-weighted 10 terms for each topic.
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 10)
    topicIndices.foreach { case (terms, termWeights) =>
      println("TOPIC:")
      terms.zip(termWeights).foreach { case (term, weight) =>
        println(s"${vocabArray(term.toInt)}\t$weight")
      }
      println()
    }
  }

  def filterStopwordsAndSynonyms(line:String) : String = {
    //uses default Stop words
    val analyzer = new CustomAnalyzer()
    val tokenStream = analyzer.tokenStream(null, new StringReader(line))

    tokenStream.reset()
    var lineList = new ListBuffer[String]()
    while (tokenStream.incrementToken()) {
      lineList += tokenStream.getAttribute(classOf[CharTermAttribute]).toString
    }
    tokenStream.close()
    lineList.mkString(" ")
  }
}
