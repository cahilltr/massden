package com.cahill.ml.examples

import java.io.File
import java.util

import com.cahill.ml.{CrossValidationResults, MLAlgorithm}
import com.cahill.optimization.Parameter
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.RandomForest

import scala.io.Source

object SparkRandomForestOptmizationExample extends MLAlgorithm {


  override def run(params: util.Map[String, Parameter]): CrossValidationResults = {

    val conf = new SparkConf().setAppName("Random_Forrest_Optimization").setMaster("local[*]")
    val sc = new SparkContext(conf)
    //TODO load multiple files
    val data:RDD[LabeledPoint] = sc.makeRDD(parseARFF(null))
    val splits = data.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))

    //TODO use parameters here
    val numClasses = params.get(0).getName
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 3 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "variance"
    val maxDepth = 4
    val maxBins = 32

    val model = RandomForest.trainRegressor(trainingData,categoricalFeaturesInfo,numTrees,featureSubsetStrategy,impurity,maxDepth,maxBins,100)

    val labelsAndPredictions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val rankedPredicitions = labelsAndPredictions.sortBy(s => s._2, ascending = false).map(r => r._1).map(d => d.toInt).collect()


    new CrossValidationResults(rankedPredicitions)
  }

  //TODO throw out unneeded data
  def parseARFF(file:String) : Seq[LabeledPoint] = {
    Source.fromFile(new File(file)).getLines().map(lineToLabeledPoint).toSeq
  }

  def lineToLabeledPoint(line:String) : LabeledPoint = {
    val break = line.split(",")
    val binaryClass = break.apply(break.length - 1).toInt
    val noClass = break.slice(0, break.length - 2)

    LabeledPoint(binaryClass, Vectors.dense(noClass.map(s => {
      if (s.equals("?"))
        Double.MinValue
      else
        s.toDouble
    })))
  }

}
