package movingCities

import java.io.{File, PrintWriter}

import com.google.maps.{GeoApiContext, GeocodingApi}
import org.htmlcleaner.{ContentNode, HtmlCleaner, TagNode}

import scala.io.Source

/**
  * Created by cahillt on 3/21/16.
  * Written as a scratch pad
  */
class MovingCities {
  private val context = new GeoApiContext()
    .setQueryRateLimit(10)
  private  var geoPointsMap:Map[String, String] = Map.empty

  private var cityLatLongPath = "~/test/cityLatLong.txt"

  def main (args: Array[String]): Unit = {
    val apiKey = args.apply(0)
    context.setApiKey(apiKey)

    var cityLatLongPath = args.apply(1)
  }

  def getCitiesWebPage() : String = {
    Source.fromURL("http://www.bestplaces.net/cost-of-living/").mkString
  }

  def getGeoPoint(cityState:String) : String = {
    if (geoPointsMap.contains(cityState)) {
      geoPointsMap.get(cityState).toString
    } else {
      val results = GeocodingApi.geocode(context, cityState).await()
      val lat = results.apply(0).geometry.location.lat
      val lng = results.apply(0).geometry.location.lng
      val point = lat + "," + lng

      geoPointsMap += cityState -> point
      point
    }
  }

  def parseForTable(html:String) : Array[String] = {
    val htmlCleaner = new HtmlCleaner()
    val tagNode = htmlCleaner.clean(html)
    val tableChildren = tagNode
      .getElementListByName("table", true).get(2)
      .getAllChildren.get(0)
      .asInstanceOf[TagNode].getAllChildren()

    val time = System.nanoTime()
    tableChildren.toArray().drop(1).map(f => f.asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent)
    val listStrings = tableChildren.toArray().drop(1).map( f => {
      val fromCity = f.asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent
      val toCity = f.asInstanceOf[TagNode].getAllChildren.get(1).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[TagNode].getAllChildren.get(0).asInstanceOf[ContentNode].getContent
      time + "|" + fromCity + "|" + toCity
    })

    listStrings
  }

  def writeOutCitysLatLong() : Unit = {
    val writer = new PrintWriter(new File("test.txt" ))

    geoPointsMap.foreach(f => {
      writer.write(f._1 + "|" + f._2 + "\r\n")
    })
    writer.flush()
    writer.close()
  }

  def readCitysLatLong() : Unit = {
    try {
      val lines = Source.fromFile(new File("test.txt")).getLines()
      lines.foreach(f => {
        val splits = f.split("\\|")
        val cityState = splits.apply(0)
        val point = splits.apply(1)
        geoPointsMap += cityState -> point
      })
    } catch {
      case  e: Exception => System.out.println(e)
    }
  }

}
