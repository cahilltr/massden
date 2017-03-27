package com.cahill.ml.examples

import java.util

import com.cahill.ml.{CrossValidationResults, MLAlgorithm}
import com.cahill.optimization.{CategoricalParameter, NumericalParameter, Parameter}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

class SparkRandomForestOptmizationExample extends MLAlgorithm {

  def run(params: util.Map[String, Parameter[_]]): CrossValidationResults = {

    Thread.sleep(10000)

    val conf = new SparkConf().setAppName("Random_Forrest_Optimization").setMaster("local[*]")
    val sc = new SparkContext(conf)
    //load multiple files
    val data:RDD[LabeledPoint] = sc.makeRDD(parseMultipleARFFs(getFilesFromResources))
    val splits = data.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))

    //TODO create get categorical and get numerical value from param
    //parameter.featureSubsetStrategy=("sqrt", "log2", "onethird"),"sqrt
    //use parameters here
    val numClasses = if (params.containsKey("final.parameter.numClasses")) getNumericParameter(params.get("final.parameter.numClasses").asInstanceOf[Parameter[Double]]) else 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = if (params.containsKey("parameter.numTrees")) getNumericParameter(params.get("parameter.numTrees").asInstanceOf[Parameter[Double]]) else 3 //default should be more in practice
//    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val featureSubsetStrategy = if (params.containsKey("parameter.featureSubsetStrategy")) getCategoricalParameter(params.get("parameter.featureSubsetStrategy").asInstanceOf[Parameter[String]]) else "auto"
    val impurity = "variance"
    val maxDepth = if (params.containsKey("parameter.maxDepth")) getNumericParameter(params.get("parameter.maxDepth").asInstanceOf[Parameter[Double]]) else 4
    val maxBins = if (params.containsKey("parameter.maxBins")) getNumericParameter(params.get("parameter.maxBins").asInstanceOf[Parameter[Double]]) else 32

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
    val classLoader = SparkRandomForestOptmizationExample.super.getClass.getClassLoader
    Source.fromInputStream(classLoader.getResourceAsStream(file))
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
    Array(base + "1year.arff", base + "2year.arff",
      base + "3year.arff", base + "4year.arff",
      base + "5year.arff")
  }

  def getCategoricalParameter(param:Parameter[String]) : String = {
    val cp = param.asInstanceOf[CategoricalParameter]
    cp.getRunningValue
  }

  def getNumericParameter(param:Parameter[Double]): Int = {
    val np = param.asInstanceOf[NumericalParameter]
    np.getRunningValue.toInt
  }

}
