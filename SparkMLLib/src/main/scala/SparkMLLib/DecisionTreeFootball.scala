package SparkMLLib

import java.io.FileInputStream
import java.util.Properties

import org.apache.log4j.LogManager
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors


/**
  * Created by cahillt on 2/22/16.
  * Creates decision tree to determine if a player will be a long term player in the NFL or not based on
  * Combine Results and the College attended.
  */
object DecisionTreeFootball {
  val log = LogManager.getLogger(this.getClass)

  def main (args: Array[String]) {

    val properties = new Properties()

    if (args.length == 1) {
      val Array(propertiesFile) = args
      properties.load(new FileInputStream(propertiesFile))
    }

    //Long Term Definition
    val longTerm = properties.getProperty("long.term", "5").toInt
    log.info("Long Term: " + longTerm)
    //Impurity to use
    val impurityString = properties.getProperty("impurity", "entropy")
    log.info("Impurity: " + impurityString)
    //Max Depth
    val max_depth = properties.getProperty("max.depth", "10").toInt
    log.info("Max Depth: " + max_depth)
    //Max Bins addition to add to collegeMap.size
    val max_bins_addition = properties.getProperty("max.bins.addition", "100").toInt
    log.info("Max Bins Addition: " + max_bins_addition)
    //Display All output of test Data label and predictions
    val display_label_predictions = properties.getProperty("display.label.predictions", "false").toBoolean
    log.info("Display Label Predictions: " + display_label_predictions)
    //Display Tree Model
    val display_tree_model = properties.getProperty("display.tree.model", "false").toBoolean
    log.info("Display Tree Model: " + display_tree_model)
    //Percent data that is training data
    val training_data_percent = properties.getProperty("percent.training.data", "0.85").toDouble
    log.info("Percent Data that is Training Data: " + training_data_percent)
    //Percent data that is test data
    val test_data_percent = properties.getProperty("percent.test.data", "0.15").toDouble
    log.info("Percent Data that is Training Data: " + test_data_percent)


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

    val findYearsPlayed = udf((year_end: Int, year_start: Int) => if ((year_end - year_start) >= longTerm) 1 else 0)

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

    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int](0 -> collegeMap.size)
    val impurity = impurityString
    val maxDepth = max_depth
    val maxBins = collegeMap.size + max_bins_addition


    val labeledData = joinedCleaned.rdd.map(r => {
      val values = Array(collegeMap.get(r.getAs[String]("college")).get.toDouble, r.getAs[Double]("heightinchestotal"),
        r.getAs[Double]("hands"), r.getAs[Double]("fortyyd"), r.getAs[Double]("twentyyd"), r.getAs[Double]("tenyd"),
        r.getAs[Double]("twentyss"), r.getAs[Double]("threecone"), r.getAs[Double]("vertical"), r.getAs[Int]("broad").toDouble,
        r.getAs[Int]("bench").toDouble, r.getAs[Int]("wonderlic").toDouble, r.getAs[Double]("nflgrade"),
        r.getAs[Double]("arms"), r.getAs[Int]("weight").toDouble, r.getAs[Int]("picktotal").toDouble)
      val label = r.getAs[Int]("long_term")
      LabeledPoint(label, Vectors.dense(values))
    })

    val splits = labeledData.randomSplit(Array(training_data_percent, test_data_percent))
    val (trainingData, testData) = (splits(0), splits(1))
    trainingData.cache()
    testData.cache()

    val model = DecisionTree.trainClassifier(labeledData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    trainingData.unpersist()

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    testData.unpersist()

    if (display_tree_model) {
      println("Learned classification tree model:\n" + model.toDebugString)
    }
    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
    println("Test Error = " + testErr)
    if (display_label_predictions) {
      println("Predictions Count: " + labelAndPreds.count())
      println("Actual : Prediction")
      labelAndPreds.collect().foreach(f => println(f._1 + " : " + f._2))
    }
  }
}
