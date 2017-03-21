package com.cahill.ml.examples

import java.io.File
import java.util

import com.cahill.ml.{CrossValidationResults, MLAlgorithm}
import com.cahill.optimization.Parameter
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

class SparkRandomForestOptmizationExample extends MLAlgorithm {



  override def run(params: util.Map[String, Parameter]): CrossValidationResults = {

    val conf = new SparkConf().setAppName("Random_Forrest_Optimization").setMaster("local[*]")
    val sc = new SparkContext(conf)
    //load multiple files
    val data:RDD[LabeledPoint] = sc.makeRDD(parseMultipleARFFs(getFilesFromResources))
    val splits = data.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))

    //se parameters here
    val numClasses = if (params.containsKey("numClasses")) params.get("numClasses").getRunningValue.toInt else 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = if (params.containsKey("numTrees")) params.get("numTrees").getRunningValue.toInt else 3 //default should be more in practice
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "variance"
    val maxDepth = if (params.containsKey("maxDepth")) params.get("maxDepth").getRunningValue.toInt else 4
    val maxBins = if (params.containsKey("maxBins")) params.get("maxBins").getRunningValue.toInt else 32

    val model = RandomForest.trainRegressor(trainingData,categoricalFeaturesInfo,numTrees,featureSubsetStrategy,impurity,maxDepth,maxBins,100)

    val labelsAndPredictions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val rankedPredicitions = labelsAndPredictions.sortBy(s => s._2, ascending = false).map(r => r._1).map(d => d.toInt).collect()


    val results = new CrossValidationResults(rankedPredicitions)
    sc.stop()
    results
  }

  //Handle multipe files and return a single Seq[LabeledPoint]
  def parseMultipleARFFs(files:Array[String]) : Seq[LabeledPoint] = {
    files.map(s => parseARFF(s)).reduce((a,b) => a++b)
  }

  //throw out unneeded data (anything that starts with "@")
  def parseARFF(file:String) : Seq[LabeledPoint] = {
    Source.fromFile(new File(file))
      .getLines()
      .filter(s => !s.startsWith("@"))
      .filter(s => !s.isEmpty)
      .map(lineToLabeledPoint).toSeq
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

  def getFilesFromResources: Array[String] = {
    val base = "examples/SparkRandomForestData/"
    val classLoader = this.getClass.getClassLoader
    Array(classLoader.getResource(base + "1year.arff").getFile, classLoader.getResource(base + "2year.arff").getFile,
      classLoader.getResource(base + "3year.arff").getFile, classLoader.getResource(base + "4year.arff").getFile,
      classLoader.getResource(base + "5year.arff").getFile)
  }

}
