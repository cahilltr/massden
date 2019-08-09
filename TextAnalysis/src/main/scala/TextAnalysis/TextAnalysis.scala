package TextAnalysis

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * Created by cahillt on 5/2/16.
  */
object TextAnalysis {

  def main( args:Array[String] ):Unit = {
    val conf = new SparkConf().setAppName("Text_Analysis").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val text = sc.textFile(args(0))

    val cleanedText = text.map(f => filterStopwordsAndSynonyms(f))

    val ngramText = cleanedText
      .map(line => getNGram(line, 3))
      .flatMap(i => i.iterator)

    val ngramCount = ngramText
      .map(ngram => (ngram, 1))
      .reduceByKey((x,y) => x + y)

    val keyCount = ngramCount.count()
    val countSum = ngramCount.map(x => x._2).reduce((x,y) => x + y)
    val mean = countSum.toDouble / keyCount

    val tmp = ngramCount.map(x => x._2).map(i => Math.pow(i - mean,2.0)).reduce((x,y) => x + y)

    val stddev = Math.sqrt(1.0/keyCount * tmp)


//    val SQLContext = new SQLContext(sc)
//    val schema = StructType(Array(StructField("value", StringType,nullable = false), StructField("counts", IntegerType, nullable = false)))
//
//    val ngramDF = SQLContext.createDataFrame(ngramCount.map(f => Row(f)), schema)



    val ngramZScore = ngramCount.map(x => (x._1, (x._2 - mean)/stddev))

    ngramZScore.foreach(x => println(x._1 + " : " + x._2))

  }

  def getNGram(line:String, n:Int) : List[String] = {
    val hello = for (i <- 1 to n) yield {
      line.split(" ").sliding(i).toList.map( s => s.mkString(" " ))
    }

    hello.flatten.toList
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
