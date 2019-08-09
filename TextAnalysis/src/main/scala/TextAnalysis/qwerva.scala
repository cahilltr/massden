package TextAnalysis

import java.io.StringReader

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.synonym.{SynonymFilter, SynonymMap}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.CharsRef

import scala.collection.mutable.ListBuffer

/**
  * Created by cahillt on 5/2/16.
  */
object qwerva {

  def main( args:Array[String] ):Unit = {
    val words = "the bee is the bee of the bees"

//    words.split(' ').sliding(2).toList.map(f => f.mkString(" ")).toList
    val n = 3

//    val ngrams = for (i <- 1 to n ) yield words.split(" ").sliding(i).map(p => p.toList).flatMap( x => x)

//    words.split(' ').sliding(2).foreach( p => println(p.mkString(" ")))
//words.split("asdf").sliding(2).map(p => p.toList).flatMap( x => x)


//    val ngrams = (for( i <- 1 to n) yield w.sliding(i).map(p => p.toList)).flatMap(x => x)
//    ngrams foreach println

//    val eqreqw = 1 to 3 map { i =>
//      words.split(" ").sliding(i).toList.map( s => s.mkString(" " )).toList
//    }
//
//    val hello = for (i <- 1 to n) yield {
//      words.split(" ").sliding(i).toList.map( s => s.mkString(" " ))
//    }
//
//    val flattenedHello = hello.flatten.toList
//
//
//    eqreqw.foreach(p => p.foreach(f => println()))

    val line = "this is a great day"

//    val analyzer = new StandardAnalyzer()
//    val tokenStream = analyzer.tokenStream(null, new StringReader(line))
//
//    val builder = new SynonymMap.Builder(true)
//
//    builder.add(new CharsRef("large"), new CharsRef("big"), false)
//    builder.add(new CharsRef("great"), new CharsRef("good"), false)
//
//    val filter = new SynonymFilter(tokenStream, builder.build(), true)

    val analyzer = new CustomAnalyzer()
    val tokenStream = analyzer.tokenStream(null, new StringReader(line))

    tokenStream.reset()
    var lineList = new ListBuffer[String]()
    while (tokenStream.incrementToken()) {
      lineList += tokenStream.getAttribute(classOf[CharTermAttribute]).toString
    }
    tokenStream.close()
    println(lineList.mkString(" "))

  }

}
