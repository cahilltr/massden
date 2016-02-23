package SparkMLLib

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors


/**
  * Created by cahillt on 2/22/16.
  *
  */
object DecisionTreeFootball {

  def main (args: Array[String]) {

    val conf = new SparkConf().setAppName("Decision_Tree")
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    val combineDF = sqlContext.read.format("com.databricks.spark.csv").option("header","true").option("inferSchema", "true").load("file:///root/combine.csv")
    val playerDF = sqlContext.read.format("com.databricks.spark.csv").option("header","true").option("inferSchema", "true").load("file:///root/players.csv")

    val cleanedPlayerDF = playerDF
      .filter("year_end < 1999")
      .withColumnRenamed("name", "full_name")
      .drop("position")
      .drop("weight")
      .drop("college")

//    val cleanedPlayerDF = playerDF.filter("year_end < 1999").withColumnRenamed("name", "full_name").drop("position").drop("weight").drop("college")

    val joined = combineDF.join(cleanedPlayerDF, combineDF("name") === cleanedPlayerDF("full_name") &&
      combineDF("firstname") === cleanedPlayerDF("first_name") &&
      combineDF("lastname") === cleanedPlayerDF("last_name"), "leftouter")


//    val joined = combineDF.join(cleanedPlayerDF, combineDF("name") === cleanedPlayerDF("full_name") && combineDF("firstname") === cleanedPlayerDF("first_name") && combineDF("lastname") === cleanedPlayerDF("last_name"), "leftouter")

    val findYearsPlayed = udf((year_start: Int, year_end: Int) => if ((year_end - year_start) >= 5) 1 else 0)

    val joinedCleaned = joined
      .drop("first_name")
      .drop("last_name")
      .drop("full_name")
      .withColumn("long_term", findYearsPlayed(col("year_end"), col("year_start")))

    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 6
    val maxBins = 40

//    year,name,firstname,lastname,position,heightfeet,heightinches,heightinchestotal,
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
      val values = Array(r.getAs[Double]("heightinchestotal"), r.getAs[Int]("weight").toDouble, r.getAs[Double]("arms"),
        r.getAs[Double]("hands"), r.getAs[Double]("fortyyd"), r.getAs[Double]("twentyyd"), r.getAs[Double]("tenyd"),
        r.getAs[Double]("twentyss"), r.getAs[Double]("threecone"), r.getAs[Double]("vertical"), r.getAs[Int]("broad").toDouble,
        r.getAs[Int]("bench").toDouble, r.getAs[Int]("round").toDouble, r.getAs[Int]("wonderlic").toDouble, r.getAs[Double]("nflgrade"),
        r.getAs[Int]("picktotal").toDouble, collegeMap.get(r.getAs[String]("college")).get.toDouble)
      val label = r.getAs[Int]("long_term")
      LabeledPoint(label, Vectors.dense(values))
    })

    val splits = labeledData.randomSplit(Array(0.9, 0.1))
    val (trainingData, testData) = (splits(0), splits(1))

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)



    // Evaluate model on test instances and compute test error
//    val labelAndPreds = testData.map { point =>
//      val prediction = model.predict(point.features)
//      (point.label, prediction)
//    }
//    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
//    println("Test Error = " + testErr)
    println("Learned classification tree model:\n" + model.toDebugString)
    println("Algo: " + model.algo.toString)
//    println("Predictions Count: " + labelAndPreds.count())
//    println("Actual : Prediction")
//    labelAndPreds.collect().foreach(f => println(f._1 + " : " + f._2))
  }
}
