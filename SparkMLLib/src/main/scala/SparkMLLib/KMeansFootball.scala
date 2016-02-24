package SparkMLLib

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkContext, SparkConf}


/**
  * Created by cahillt on 2/24/16.
  */
object KMeansFootball {

  def main (args: Array[String]) {

    val conf = new SparkConf().setAppName("Decision_Tree").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    val combineDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/combine.csv")
    val playerDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/players.csv")

    val cleanedPlayerDF = playerDF
      .filter("year_end > 1999")
      .withColumnRenamed("name", "full_name")
      .drop("position")
      .drop("weight")
      .drop("college")

    val joined = combineDF.join(cleanedPlayerDF, combineDF("name") === cleanedPlayerDF("full_name") &&
      combineDF("firstname") === cleanedPlayerDF("first_name") &&
      combineDF("lastname") === cleanedPlayerDF("last_name"), "leftouter")

    val findYearsPlayed = udf((year_end: Int, year_start: Int) => if ((year_end - year_start) >= 5) 1 else 0)

    val joinedCleaned = joined
      .drop("first_name")
      .drop("last_name")
      .drop("full_name")
      .withColumn("long_term", findYearsPlayed(col("year_end"), col("year_start")))

    // year,name,firstname,lastname,position,heightfeet,heightinches,heightinchestotal,
    // weight,arms,hands,fortyyd,twentyyd,tenyd,twentyss,threecone,vertical,broad,bench,round,
    // college,pick,pickround,picktotal,wonderlic,nflgrade

    //Holy Scala, Martin Odersky would be proud.
    val collegeMap = joinedCleaned
      .select("college")
      .distinct.rdd
      .collect
      .map(r => r.getAs[String](0))
      .zipWithIndex
      .map(t => t._1 -> t._2)
      .toMap

    val labeledData = joinedCleaned.rdd.map(r => {
      val values = Array(collegeMap.get(r.getAs[String]("college")).get.toDouble, r.getAs[Double]("heightinchestotal"),
        r.getAs[Double]("hands"), r.getAs[Double]("fortyyd"), r.getAs[Double]("twentyydNO"), r.getAs[Double]("tenyd"),
        r.getAs[Double]("twentyss"), r.getAs[Double]("threecone"), r.getAs[Double]("vertical"), r.getAs[Int]("broad").toDouble,
        r.getAs[Int]("bench").toDouble, r.getAs[Int]("wonderlic").toDouble, r.getAs[Double]("nflgrade"),
        r.getAs[Double]("arms"), r.getAs[Int]("weight").toDouble, r.getAs[Int]("picktotal").toDouble)
      val label = r.getAs[Int]("long_term")
      LabeledPoint(label, Vectors.dense(values))
    })


    val splits = labeledData.randomSplit(Array(.95, .05))
    val (trainingData, testData) = (splits(0), splits(1))
    trainingData.cache()
    testData.cache()

    val numClusters = 2
    val numIterations = 20

    val clusters = KMeans.train(trainingData.map(l => l.features), numClusters, numIterations)

    trainingData.unpersist()

//    val labelAndPreds = testData.map { point =>
//      val prediction = model.predict(point.features)
//      (point.label, prediction)
//    }

//    val results = clusters.predict(testData.map(f => f.features))

    val results = testData.map(f => (f.label, clusters.predict(f.features)))
    val testErr = results.filter(r => r._1 != r._2).count.toDouble / testData.count()

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(testData.map(l => l.features))
    println("Within Set Sum of Squared Errors = " + WSSSE)

  }
}
