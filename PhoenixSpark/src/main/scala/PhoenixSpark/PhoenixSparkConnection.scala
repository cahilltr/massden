package PhoenixSpark

import org.apache.spark.SparkContext
import org.apache.spark.sql.{Column, SQLContext}

/**
  * Created by cahillt on 11/25/15.
  */
object PhoenixSparkConnection {

  def avg(col:Column): Unit = {

  }

  def main (args: Array[String]) {
    if (args.length != 2) {
      System.err.println("Usage: PhoenixSparkConnection <zkURL> <table>")
      System.exit(1)
    }

    val Array(zkURL, table) = args

    val sc = new SparkContext()
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.load(
      "org.apache.phoenix.spark",
      Map("table" -> table, "zkUrl" -> zkURL)
    )

    //select all SPY entries
    df.filter(df("SYMBOL") === "SPY").show(20)

//    df.agg(avg(df("OPEN")))OPEN
//    df.filter(df("SYMBOL") === "SPY").agg(avg(df("OPEN")))
    val stuff = df.filter(df("SYMBOL") === "SPY")
//    stuff.agg(avg(df("OPEN")))
//    stuff.agg(df("OPEN"))
    val x = df.filter(df("SYMBOL") === "SPY").groupBy("SYMBOL").agg(df("OPEN"))

    System.out.println("HELLO " + x)
    System.out.println("HELLO2 " + df.count())
  }

}
