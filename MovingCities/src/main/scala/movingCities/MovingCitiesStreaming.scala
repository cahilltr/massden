package movingCities

import java.io.{File, FileInputStream}

import com.google.maps.{GeoApiContext, GeocodingApi}
import movingCities.hbase.HBaseHandler
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.htmlcleaner.{ContentNode, HtmlCleaner, TagNode}

import scala.collection.mutable
import scala.io.Source

/**
  * Created by cahillt on 3/23/16.
  * Spark Streaming example
  * take arg for conf as hbaseconfig
  * http://stackoverflow.com/questions/24519660/spark-streaming-with-twitter-no-output-streams-registered-so-nothing-to-execu
  * https://console.developers.google.com/
  * arguments number-of-time-to-glean-data time_in_millis_to_run_events google_dev_api_key
  */
object MovingCitiesStreaming {
  private val context = new GeoApiContext()
    .setQueryRateLimit(10)

  def main (args: Array[String]): Unit = {
    MovingCitiesStreaming.start(args)
  }

  def start(args: Array[String]): Unit = {

    val loopCount = args.apply(0).toInt
    val triggerEvents = args.apply(1).toLong

    this.context.setApiKey(args.apply(2).toString)

    val sparkConf = new SparkConf().setAppName("QueueStream")

    //    Milliseconds(triggerEvents)
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    val hbaseConfigPath = args.apply(3).toString


    val rddQueue = new mutable.Queue[RDD[MovingCitiesContainer]]()

    val inputStream = ssc.queueStream(rddQueue)

    val gps = inputStream.map( f => {
      val hBaseConfiguration = HBaseConfiguration.create
      hBaseConfiguration.addResource(new FileInputStream(new File(hbaseConfigPath)))
      val hbaseHandler = new HBaseHandler("cities", hBaseConfiguration)
      f.setToCityGeo(getGeoPoint(f.getToCity, hbaseHandler))
      f.setFromCityGeo(getGeoPoint(f.getFromCity, hbaseHandler))
      println("RDD1: " + f.getFromCity + "," + f.getToCity)
      hbaseHandler.closeConnection()
      f
    })

    //get cites geo and put into hbase
    gps.foreachRDD(rdd => rdd.foreach( s => {
      println("RDD2: Made it")
      val hBaseConfiguration = HBaseConfiguration.create
      hBaseConfiguration.addResource(new FileInputStream(new File(hbaseConfigPath)))
      val hbaseHandler = new HBaseHandler("cities", hBaseConfiguration)
      hbaseHandler.putToCity(s.getToCity, s.getFromCity, s.getTime.toString)
      hbaseHandler.closeConnection()
      println("RDD2: " + s.getToCity + "," + s.getFromCity)
    }))

    ssc.start()

    var loopsDone = 0
    while(loopsDone < loopCount) {
      rddQueue.synchronized {
        rddQueue += ssc.sparkContext.makeRDD(parseForTable(getCitiesWebPage).toSeq)
      }
      Thread.sleep(triggerEvents * 5)
      loopsDone += 1
    }
//    ssc.stop()
    ssc.awaitTermination()
  }

  def getCitiesWebPage: String = {
    Source.fromURL("http://www.bestplaces.net/cost-of-living/").mkString
  }

  def parseForTable(html:String) : Array[MovingCitiesContainer] = {
    val htmlCleaner = new HtmlCleaner()
    val tagNode = htmlCleaner.clean(html)
    val tableChildren = tagNode
      .getElementListByName("table", true).get(2)
      .getAllChildren.get(0)
      .asInstanceOf[TagNode].getAllChildren

    tableChildren.toArray().drop(1).map(f => f.asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent)
    val listStrings = tableChildren.toArray().drop(1).map( f => {
      val fromCity = f.asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent
      val toCity = f.asInstanceOf[TagNode].getAllChildren.get(1).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent
      println(System.nanoTime().toString + "," + toCity + "," + fromCity)
      new MovingCitiesContainer(System.nanoTime().toString, toCity, fromCity)
    })

    listStrings
  }

  def getGeoPoint(cityState:String, hBaseHandler: HBaseHandler) : String = {
    val hbaseCityLocationResults = hBaseHandler.getCityLocation(cityState)
    if (!hbaseCityLocationResults.isEmpty) {
      hbaseCityLocationResults
    } else {
      val results = GeocodingApi.geocode(context, cityState).await()
      val lat = results.apply(0).geometry.location.lat
      val lng = results.apply(0).geometry.location.lng
      val point = lat + "," + lng
      hBaseHandler.putCityLocation(cityState, point)
      point
    }
  }

}
