package com.cahill.creditdefaultrisk

import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.{Pipeline, PipelineStage, ROC}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
  * Code for Kaggle: https://www.kaggle.com/c/home-credit-default-risk
  */
object Learning {

  private val MASTER_URL:String = "masterURL"
  private val PATHTODATA:String = "pathToData"
  //https://stackoverflow.com/questions/41714698/how-to-get-accuracy-precision-recall-and-roc-from-cross-validation-in-spark-ml
  //https://stackoverflow.com/questions/37527753/vectorassembler-does-not-support-the-stringtype-type-scala-spark-convert

  def main (args: Array[String]): Unit = {
    val optMap = args.map(s => {
      val split = s.split("=")
      (split(0), split(1))
    }).toMap

    val masterURL = if (optMap.contains(MASTER_URL)) optMap.get(MASTER_URL).toString else "local"
    val pathToFolder = if (optMap.contains(PATHTODATA)) optMap.get(MASTER_URL).toString else "C:\\Users\\ph9qum\\Desktop\\defaultdata\\"

    run(masterURL, pathToFolder)
  }

  def run(masterURL:String, pathToFolder:String): Unit = {
    val spark = SparkSession
      .builder()
      .master(masterURL)
      .appName("Decision Tree")
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

//    val bureau = getBureauBalanceDataFrame(spark, pathToFolder)
//    val bureauBalance = getBureauBalanceDataFrame(spark, pathToFolder)
//    val ccBalanace = getCreditCardBalanceDataFrame(spark, pathToFolder)
//    val installmentsPayments = getInstallmentsPaymentsDataFrame(spark, pathToFolder)
//    val posCashBalance = getPOSCashBalanceDataFrame(spark, pathToFolder)
//    val previousApp = getPreviousApplicationDataFrame(spark, pathToFolder)


    val naMap = getApplicationSchema(false).map(sf => sf.name -> getNADefault(sf)).toMap

    val testApplicationDataReal = getApplicationDataFrame(spark, pathToFolder, false).na.fill(naMap)
    val trainApplicationDataAll = getApplicationDataFrame(spark, pathToFolder, true).na.fill(naMap).withColumnRenamed("TARGET", "label")

    val trainApplicationDataArray = trainApplicationDataAll.randomSplit(Array(0.8, 0.8))
    val trainApplicationData = trainApplicationDataArray(0)
    val testApplicationData = trainApplicationDataArray(1)

    val trainStringTypeFields = trainApplicationDataAll.schema.filter(st => st.dataType.equals(StringType)).toList
    val trainNotStringFields = trainApplicationDataAll.schema.toList.filterNot(trainStringTypeFields.toSet)

    // Set up Pipeline.
    val stages = new mutable.ArrayBuffer[PipelineStage]()

    // (1) For classification, re-index classes.
    val labelColName = "indexedLabel"
    val cleanTrainStringFields = new ListBuffer[String]()

    trainStringTypeFields.foreach(sf => {
      if (sf.name.equals("SK_ID_CURR"))
        return
      if (!sf.name.equals("label")) {
        val labelIndex = new StringIndexer()
          .setInputCol(sf.name)
          .setOutputCol(sf.name.+("1"))
        cleanTrainStringFields += sf.name.+("1")
        stages += labelIndex
      } else {
        val labelIndex = new StringIndexer()
          .setInputCol(sf.name)
          .setOutputCol(labelColName)
        stages += labelIndex
      }
    })

    //Add strings to
    val featuresAssembler = new VectorAssembler()
      .setInputCols((trainNotStringFields.filterNot(sf => sf.name.equals("SK_ID_CURR")).map(sf => sf.name):::cleanTrainStringFields.toList).toArray)
      .setOutputCol("features")
    stages += featuresAssembler


    val decisionTree = new DecisionTreeClassifier().setMaxBins(58)
    stages += decisionTree
    val pipeline = new Pipeline().setStages(stages.toArray)


    val startTime = System.nanoTime()
    val pipelineModel = pipeline.fit(trainApplicationData)
    val elapsedTime = (System.nanoTime() - startTime) / 1e9
    println(s"Training time: $elapsedTime seconds")

    val treeModel = pipelineModel.stages.last.asInstanceOf[DecisionTreeClassificationModel]
    if (treeModel.numNodes < 20) {
      println(treeModel.toDebugString) // Print full model.
    } else {
      println(treeModel) // Print model summary.
    }

    println("Training data results:")
    ROC.evaluateClassificationModel(pipelineModel, trainApplicationData, "label")
    println("Test data results:")
    ROC.evaluateClassificationModel(pipelineModel, testApplicationData, "label")


    //Run on test data and report results
    val array = ROC.runClassificationModel(pipelineModel, testApplicationDataReal, "SK_ID_CURR")
    println("Real Test data results:")
    array.map(a => (a._1, a._2, a._3.apply(0), a._3.apply(1))).foreach(println)

    spark.stop()
  }

  def castColumnTo(df: DataFrame,
                   columnName: String,
                   targetType: DataType ) : DataFrame = {
    df.withColumn( columnName, df(columnName).cast(targetType) )
  }

  /**
    * All client's previous credits provided by other financial institutions that were reported to Credit Bureau (for clients who have a loan in our sample).
    * For every loan in our sample, there are as many rows as number of credits the client had in Credit Bureau before the application date.
    */
  def getBureauDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val bureauSchema = new StructType()
      .add(StructField("SK_ID_CURR", LongType,false))
      .add(StructField("SK_ID_BUREAU", LongType,false))
      .add(StructField("CREDIT_ACTIVE", StringType,false))
      .add(StructField("CREDIT_CURRENCY", StringType,false))
      .add(StructField("DAYS_CREDIT", DoubleType,false))
      .add(StructField("CREDIT_DAY_OVERDUE", DoubleType,false))
      .add(StructField("DAYS_CREDIT_ENDDATE", DoubleType,true))
      .add(StructField("DAYS_ENDDATE_FACT", DoubleType,true))
      .add(StructField("AMT_CREDIT_MAX_OVERDUE", DoubleType,true))
      .add(StructField("CNT_CREDIT_PROLONG", DoubleType,true))
      .add(StructField("AMT_CREDIT_SUM", DoubleType,true))
      .add(StructField("AMT_CREDIT_SUM_DEBT", DoubleType,true))
      .add(StructField("AMT_CREDIT_SUM_LIMIT", DoubleType,true))
      .add(StructField("AMT_CREDIT_SUM_OVERDUE", DoubleType,true))
      .add(StructField("CREDIT_TYPE", StringType,true))
      .add(StructField("DAYS_CREDIT_UPDATE", DoubleType,true))
      .add(StructField("AMT_ANNUITY", DoubleType,true))

    spark.read.option("header", "true").schema(bureauSchema).csv(pathToFolder + "bureau.csv")
  }

  /**
    * Monthly balances of previous credits in Credit Bureau.
    * This table has one row for each month of history of every previous credit reported to Credit Bureau – i.e the table has
    * (#loans in sample * # of relative previous credits * # of months where we have some history observable for the previous credits) rows.
    *
    */
  def getBureauBalanceDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val schema = new StructType()
      .add(StructField("SK_ID_BUREAU", LongType,false))
      .add(StructField("MONTHS_BALANCE", LongType,false))
      .add(StructField("STATUS", StringType,true))

    spark.read.option("header", "true").schema(schema).csv(pathToFolder + "bureau_balance.csv")
  }

  /**
    * Monthly balance snapshots of previous credit cards that the applicant has with Home Credit.
    * This table has one row for each month of history of every previous credit in Home Credit (consumer credit and cash loans)
    * related to loans in our sample – i.e. the table has (#loans in sample * # of relative previous credit cards * # of months where we have some history observable for the previous credit card) rows.
    *
    */
  def getCreditCardBalanceDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val schema = new StructType()
      .add(StructField("SK_ID_PREV", LongType, false))
      .add(StructField("SK_ID_CURR", LongType, false))
      .add(StructField("MONTHS_BALANCE", LongType, false))
      .add(StructField("AMT_BALANCE", DoubleType, true))
      .add(StructField("AMT_CREDIT_LIMIT_ACTUAL", LongType, true))
      .add(StructField("AMT_DRAWINGS_ATM_CURRENT", DoubleType, true))
      .add(StructField("AMT_DRAWINGS_CURRENT", DoubleType, true))
      .add(StructField("AMT_DRAWINGS_OTHER_CURRENT", DoubleType, true))
      .add(StructField("AMT_DRAWINGS_POS_CURRENT", DoubleType, true))
      .add(StructField("AMT_INST_MIN_REGULARITY", DoubleType, true))
      .add(StructField("AMT_PAYMENT_CURRENT", DoubleType, true))
      .add(StructField("AMT_PAYMENT_TOTAL_CURRENT", DoubleType, true))
      .add(StructField("AMT_RECEIVABLE_PRINCIPAL", DoubleType, true))
      .add(StructField("AMT_RECIVABLE", DoubleType, true))
      .add(StructField("AMT_TOTAL_RECEIVABLE", DoubleType, true))
      .add(StructField("CNT_DRAWINGS_ATM_CURRENT", DoubleType, true))
      .add(StructField("CNT_DRAWINGS_CURRENT", DoubleType, true))
      .add(StructField("CNT_DRAWINGS_OTHER_CURRENT", DoubleType, true))
      .add(StructField("CNT_DRAWINGS_POS_CURRENT", DoubleType, true))
      .add(StructField("CNT_INSTALMENT_MATURE_CUM", DoubleType, true))
      .add(StructField("NAME_CONTRACT_STATUS", StringType, true))
      .add(StructField("SK_DPD", DoubleType, true))
      .add(StructField("SK_DPD_DEF", DoubleType, true))

    spark.read.option("header", "true").schema(schema).csv(pathToFolder + "credit_card_balance.csv")
  }

  /**
    * Repayment history for the previously disbursed credits in Home Credit related to the loans in our sample.
    * There is a) one row for every payment that was made plus b) one row each for missed payment.
    * One row is equivalent to one payment of one installment OR one installment corresponding to one payment of one previous Home Credit credit related to loans in our sample.
    *
    */
  def getInstallmentsPaymentsDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val schema = new StructType()
      .add(StructField("SK_ID_PREV", LongType, false))
      .add(StructField("SK_ID_CURR", LongType, false))
      .add(StructField("NUM_INSTALMENT_VERSION", DoubleType, false))
      .add(StructField("NUM_INSTALMENT_NUMBER", DoubleType, true))
      .add(StructField("DAYS_INSTALMENT", LongType, true))
      .add(StructField("DAYS_ENTRY_PAYMENT", LongType, true))
      .add(StructField("AMT_INSTALMENT", DoubleType, true))
      .add(StructField("AMT_PAYMENT", DoubleType, true))

    spark.read.option("header", "true").schema(schema).csv(pathToFolder + "installments_payments.csv")
  }

  /**
    * Monthly balance snapshots of previous POS (point of sales) and cash loans that the applicant had with Home Credit.
    * This table has one row for each month of history of every previous credit in Home Credit (consumer credit and cash loans)
    * related to loans in our sample – i.e. the table has (#loans in sample * # of relative previous credits * # of months in which we have some history observable for the previous credits) rows.
    */
  def getPOSCashBalanceDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val schema = new StructType()
      .add(StructField("SK_ID_PREV", LongType, false))
      .add(StructField("SK_ID_CURR", LongType, false))
      .add(StructField("MONTHS_BALANCE", DoubleType, false))
      .add(StructField("CNT_INSTALMENT", DoubleType, true))
      .add(StructField("CNT_INSTALMENT_FUTURE", LongType, true))
      .add(StructField("NAME_CONTRACT_STATUS", StringType, true))
      .add(StructField("SK_DPD", DoubleType, true))
      .add(StructField("SK_DPD_DEF", DoubleType, true))

    spark.read.option("header", "true").schema(schema).csv(pathToFolder + "POS_CASH_balance.csv")
  }

  /**
    * All previous applications for Home Credit loans of clients who have loans in our sample.
    * There is one row for each previous application related to loans in our data sample.
    */
  def getPreviousApplicationDataFrame(spark:SparkSession, pathToFolder:String): DataFrame = {
    val schema = new StructType()
      .add(StructField("SK_ID_PREV", LongType, false))
      .add(StructField("SK_ID_CURR", LongType, false))
      .add(StructField("NAME_CONTRACT_TYPE", StringType, true))
      .add(StructField("AMT_ANNUITY", DoubleType, true))
      .add(StructField("AMT_APPLICATION", LongType, true))
      .add(StructField("AMT_CREDIT", DoubleType, true))
      .add(StructField("AMT_DOWN_PAYMENT", DoubleType, true))
      .add(StructField("AMT_GOODS_PRICE", DoubleType, true))
      .add(StructField("WEEKDAY_APPR_PROCESS_START", StringType, true))
      .add(StructField("HOUR_APPR_PROCESS_START", DoubleType, true))
      .add(StructField("FLAG_LAST_APPL_PER_CONTRACT", StringType, true))
      .add(StructField("NFLAG_LAST_APPL_IN_DAY", DoubleType, true))
      .add(StructField("RATE_DOWN_PAYMENT", DoubleType, true))
      .add(StructField("RATE_INTEREST_PRIMARY", DoubleType, true))
      .add(StructField("RATE_INTEREST_PRIVILEGED", DoubleType, true))
      .add(StructField("NAME_CASH_LOAN_PURPOSE", StringType, true))
      .add(StructField("NAME_CONTRACT_STATUS", StringType, true))
      .add(StructField("DAYS_DECISION", LongType, true))
      .add(StructField("NAME_PAYMENT_TYPE", StringType, true))
      .add(StructField("CODE_REJECT_REASON", StringType, true))
      .add(StructField("NAME_TYPE_SUITE", StringType, true))
      .add(StructField("NAME_CLIENT_TYPE", StringType, true))
      .add(StructField("NAME_GOODS_CATEGORY", StringType, true))
      .add(StructField("NAME_PORTFOLIO", StringType, true))
      .add(StructField("NAME_PRODUCT_TYPE", StringType, true))
      .add(StructField("CHANNEL_TYPE", StringType, true))
      .add(StructField("SELLERPLACE_AREA", DoubleType, true))
      .add(StructField("NAME_SELLER_INDUSTRY", StringType, true))
      .add(StructField("CNT_PAYMENT", DoubleType, true))
      .add(StructField("NAME_YIELD_GROUP", StringType, true))
      .add(StructField("PRODUCT_COMBINATION", StringType, true))
      .add(StructField("DAYS_FIRST_DRAWING", DoubleType, true))
      .add(StructField("DAYS_FIRST_DUE", DoubleType, true))
      .add(StructField("DAYS_LAST_DUE_1ST_VERSION", DoubleType, true))
      .add(StructField("DAYS_LAST_DUE", DoubleType, true))
      .add(StructField("DAYS_TERMINATION", DoubleType, true))
      .add(StructField("NFLAG_INSURED_ON_APPROVAL", DoubleType, true))

    spark.read.option("header", "true").schema(schema).csv(pathToFolder + "previous_application.csv")
  }

  /**
    * This is the main table, broken into two files for Train (with TARGET) and Test (without TARGET).
    * Static data for all applications. One row represents one loan in our data sample.
    *
    */
  def getApplicationDataFrame(spark:SparkSession, pathToFolder:String, isTrain:Boolean): DataFrame = {


    val filename = if (isTrain) "application_train.csv" else "application_test.csv"
    spark.read.option("header", "true").option("mode", "FAILFAST").schema(getApplicationSchema(isTrain)).csv(pathToFolder + filename)
  }

  def getApplicationSchema(isTrain:Boolean): StructType = {
    var schema = new StructType()
    .add(StructField("SK_ID_CURR", LongType, false))

    if (isTrain) {
//      new MetadataBuilder().putString("ml_attr",BinaryAttribute.defaultAttr.attrType.name).build()
      schema = schema.add(StructField("TARGET", DoubleType, false))
    }

    schema = schema
    .add(StructField("NAME_CONTRACT_TYPE", StringType, true))
    .add(StructField("CODE_GENDER", StringType, true))
    .add(StructField("FLAG_OWN_CAR", StringType, true))
    .add(StructField("FLAG_OWN_REALTY", StringType, true))
    .add(StructField("CNT_CHILDREN", DoubleType, true))
    .add(StructField("AMT_INCOME_TOTAL", DoubleType, true))
    .add(StructField("AMT_CREDIT", DoubleType, true))
    .add(StructField("AMT_ANNUITY", DoubleType, true))
    .add(StructField("AMT_GOODS_PRICE", DoubleType, true))

    .add(StructField("NAME_TYPE_SUITE", StringType, true))
    .add(StructField("NAME_INCOME_TYPE", StringType, true))
    .add(StructField("NAME_EDUCATION_TYPE", StringType, true))
    .add(StructField("NAME_FAMILY_STATUS", StringType, true))
    .add(StructField("NAME_HOUSING_TYPE", StringType, true))
    .add(StructField("REGION_POPULATION_RELATIVE", DoubleType, true))
    .add(StructField("DAYS_BIRTH", DoubleType, true))
    .add(StructField("DAYS_EMPLOYED", DoubleType, true))
    .add(StructField("DAYS_REGISTRATION", DoubleType, true))
    .add(StructField("DAYS_ID_PUBLISH", DoubleType, true))
    .add(StructField("OWN_CAR_AGE", DoubleType, true))
    .add(StructField("FLAG_MOBIL", DoubleType, true))
    .add(StructField("FLAG_EMP_PHONE", DoubleType, true))
    .add(StructField("FLAG_WORK_PHONE", DoubleType, true))
    .add(StructField("FLAG_CONT_MOBILE", DoubleType, true))
    .add(StructField("FLAG_PHONE", DoubleType, true))
    .add(StructField("FLAG_EMAIL", DoubleType, true))
    .add(StructField("OCCUPATION_TYPE", StringType, true))
    .add(StructField("CNT_FAM_MEMBERS", DoubleType, true))
    .add(StructField("REGION_RATING_CLIENT", DoubleType, true))
    .add(StructField("REGION_RATING_CLIENT_W_CITY", DoubleType, true))
    .add(StructField("WEEKDAY_APPR_PROCESS_START", StringType, true))
    .add(StructField("HOUR_APPR_PROCESS_START", DoubleType, true))
    .add(StructField("REG_REGION_NOT_LIVE_REGION", DoubleType, true))
    .add(StructField("REG_REGION_NOT_WORK_REGION", DoubleType, true))
    .add(StructField("LIVE_REGION_NOT_WORK_REGION", DoubleType, true))
    .add(StructField("REG_CITY_NOT_LIVE_CITY", DoubleType, true))
    .add(StructField("REG_CITY_NOT_WORK_CITY", DoubleType, true))
    .add(StructField("LIVE_CITY_NOT_WORK_CITY", DoubleType, true))
    .add(StructField("ORGANIZATION_TYPE", StringType, true))
    .add(StructField("EXT_SOURCE_1", DoubleType, true))
    .add(StructField("EXT_SOURCE_2", DoubleType, true))
    .add(StructField("EXT_SOURCE_3", DoubleType, true))
    .add(StructField("APARTMENTS_AVG", DoubleType, true))
    .add(StructField("BASEMENTAREA_AVG", DoubleType, true))
    .add(StructField("YEARS_BEGINEXPLUATATION_AVG", DoubleType, true))
    .add(StructField("YEARS_BUILD_AVG", DoubleType, true))
    .add(StructField("COMMONAREA_AVG", DoubleType, true))
    .add(StructField("ELEVATORS_AVG", DoubleType, true))
    .add(StructField("ENTRANCES_AVG", DoubleType, true))
    .add(StructField("FLOORSMAX_AVG", DoubleType, true))
    .add(StructField("FLOORSMIN_AVG", DoubleType, true))
    .add(StructField("LANDAREA_AVG", DoubleType, true))
    .add(StructField("LIVINGAPARTMENTS_AVG", DoubleType, true))
    .add(StructField("LIVINGAREA_AVG", DoubleType, true))
    .add(StructField("NONLIVINGAPARTMENTS_AVG", DoubleType, true))
    .add(StructField("NONLIVINGAREA_AVG", DoubleType, true))
    .add(StructField("APARTMENTS_MODE", DoubleType, true))
    .add(StructField("BASEMENTAREA_MODE", DoubleType, true))
    .add(StructField("YEARS_BEGINEXPLUATATION_MODE", DoubleType, true))
    .add(StructField("YEARS_BUILD_MODE", DoubleType, true))
    .add(StructField("COMMONAREA_MODE", DoubleType, true))
    .add(StructField("ELEVATORS_MODE", DoubleType, true))
    .add(StructField("ENTRANCES_MODE", DoubleType, true))
    .add(StructField("FLOORSMAX_MODE", DoubleType, true))
    .add(StructField("FLOORSMIN_MODE", DoubleType, true))
    .add(StructField("LANDAREA_MODE", DoubleType, true))
    .add(StructField("LIVINGAPARTMENTS_MODE", DoubleType, true))
    .add(StructField("LIVINGAREA_MODE", DoubleType, true))
    .add(StructField("NONLIVINGAPARTMENTS_MODE", DoubleType, true))
    .add(StructField("NONLIVINGAREA_MODE", DoubleType, true))
    .add(StructField("APARTMENTS_MEDI", DoubleType, true))
    .add(StructField("BASEMENTAREA_MEDI", DoubleType, true))
    .add(StructField("YEARS_BEGINEXPLUATATION_MEDI", DoubleType, true))
    .add(StructField("YEARS_BUILD_MEDI", DoubleType, true))
    .add(StructField("COMMONAREA_MEDI", DoubleType, true))
    .add(StructField("ELEVATORS_MEDI", DoubleType, true))
    .add(StructField("ENTRANCES_MEDI", DoubleType, true))
    .add(StructField("FLOORSMAX_MEDI", DoubleType, true))
    .add(StructField("FLOORSMIN_MEDI", DoubleType, true))
    .add(StructField("LANDAREA_MEDI", DoubleType, true))
    .add(StructField("LIVINGAPARTMENTS_MEDI", DoubleType, true))
    .add(StructField("LIVINGAREA_MEDI", DoubleType, true))
    .add(StructField("NONLIVINGAPARTMENTS_MEDI", DoubleType, true))
    .add(StructField("NONLIVINGAREA_MEDI", DoubleType, true))
    .add(StructField("FONDKAPREMONT_MODE", StringType, true))
    .add(StructField("HOUSETYPE_MODE", StringType, true))
    .add(StructField("TOTALAREA_MODE", DoubleType, true))
    .add(StructField("WALLSMATERIAL_MODE", StringType, true))
    .add(StructField("EMERGENCYSTATE_MODE", StringType, true))
    .add(StructField("OBS_30_CNT_SOCIAL_CIRCLE", DoubleType, true))
    .add(StructField("DEF_30_CNT_SOCIAL_CIRCLE", DoubleType, true))
    .add(StructField("OBS_60_CNT_SOCIAL_CIRCLE", DoubleType, true))
    .add(StructField("DEF_60_CNT_SOCIAL_CIRCLE", DoubleType, true))
    .add(StructField("DAYS_LAST_PHONE_CHANGE", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_2", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_3", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_4", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_5", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_6", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_7", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_8", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_9", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_10", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_11", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_12", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_13", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_14", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_15", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_16", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_17", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_18", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_19", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_20", DoubleType, true))
    .add(StructField("FLAG_DOCUMENT_21", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_HOUR", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_DAY", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_WEEK", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_MON", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_QRT", DoubleType, true))
    .add(StructField("AMT_REQ_CREDIT_BUREAU_YEAR", DoubleType, true))

    schema

  }

  def getNADefault(structField: StructField): Any = {
    val dataTypeType = structField.dataType
    dataTypeType match {
      case StringType => "a"
      case DoubleType => -1.0
      case LongType => -1L
      case _ => 0
    }
  }
}
