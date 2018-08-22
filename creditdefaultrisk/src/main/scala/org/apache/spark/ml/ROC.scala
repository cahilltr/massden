package org.apache.spark.ml

import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.lit

object ROC {

  def evaluateClassificationModel(model: Transformer, data: DataFrame, labelColName: String): Unit = {
    evaluateClassificationModel2(model,data,labelColName)
  }

  def runClassificationModel(model: Transformer, data: DataFrame, idColName: String): Array[(Long, Double, Array[Double])] = {
    val addedData = data.withColumn("label", lit(2))
    val fullPredictions = model.transform(addedData).cache()
    //Probability column is a vector
    //https://spark.apache.org/docs/latest/ml-classification-regression.html#decision-tree-classifier
    fullPredictions.select(idColName, "prediction", "probability")
      .rdd
      .map(r => (r.getLong(0), r.getDouble(1), r.getAs[DenseVector](2).values)).collect()
  }

  private[ml] def evaluateClassificationModel2(model: Transformer, data: DataFrame, labelColName: String): Unit = {
    val fullPredictions = model.transform(data).cache()
    val predictions = fullPredictions.select("prediction").rdd.map(_.getDouble(0))
    val labels = fullPredictions.select(labelColName).rdd.map(_.getDouble(0))
    // Print number of classes for reference.
    //TODO: Determine how to use metadata correctly
//    val numClasses = MetadataUtils.getNumClasses(fullPredictions.schema(labelColName)) match {
//      case Some(n) => n
//      case None => throw new RuntimeException(
//        "Unknown failure when indexing labels for classification.")
//    }
    val accuracy = new MulticlassMetrics(predictions.zip(labels)).accuracy
//    println(s"  Accuracy ($numClasses classes): $accuracy")
    println(s"  Accuracy (2 classes): $accuracy")
  }


}
