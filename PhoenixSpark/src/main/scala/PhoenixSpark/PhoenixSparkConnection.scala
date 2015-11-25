package PhoenixSpark

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
  * Created by cahillt on 11/25/15.
  */
object PhoenixSparkConnection {

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

    System.out.println(df.count())
  }

}
