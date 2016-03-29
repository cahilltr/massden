package movingCities.search

import movingCities.hbase.HBaseHandler
import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.common.SolrInputDocument
import scala.collection.JavaConverters._

/**
  * Created by cahillt on 3/25/16.
  * Index data from HBase to Solr
  * arguments Solr-ZKHost hbase-table solr-collection
  */
object HBaseIndexSolr {

  def main (args: Array[String]) : Unit = {
    HBaseIndexSolr.indexSolrFromHBase(args)
  }

  def indexSolrFromHBase(args: Array[String]) = {
    val collection = args.apply(2)
    val zkHost = args.apply(0)
    val hbaseTable = args.apply(1)
    val cloudSolrClient = new CloudSolrClient(zkHost)
    cloudSolrClient.setDefaultCollection(collection)

    val hbaseHandler = new HBaseHandler(hbaseTable)
    val allCities = hbaseHandler.scan()

    cloudSolrClient.connect()

    var geoPointsMap:Map[String, String] = Map.empty

    allCities.foreach(f => {
      val parentDoc = createFromCitySolrDocument(f.getCity, f.getLat.toDouble, f.getLng.toDouble)
      val citiesMap = f.getToCities
      val childDocs = citiesMap.map( e => {
        val city = e._1
        val times = e._2.map(t => t.toLong)
        var latlng = ""
        if (geoPointsMap.contains(city)) { //reduce calls to HBase
         latlng = geoPointsMap.get(city).get
        } else {
          latlng = hbaseHandler.getCityLocation(city)
          geoPointsMap += city -> latlng
        }
        val latlngSplits = latlng.split(",")
        createChildSolrDocument(city, latlngSplits.apply(0).toDouble, latlngSplits.apply(1).toDouble, times)
      })
      childDocs.foreach(sid => parentDoc.addChildDocument(sid))
      cloudSolrClient.add(parentDoc)
    })
    cloudSolrClient.commit()
  }

  def createFromCitySolrDocument(city:String, lat:Double, lng:Double) : SolrInputDocument = {
    val solrDocument = new SolrInputDocument
    solrDocument.addField("city_t", city)
    solrDocument.addField("latitude_d", lat)
    solrDocument.addField("longitude_d", lng)
    solrDocument.addField("coord_p", lat + "," + lng)
    solrDocument.addField("id", city + lat + lng)
    solrDocument
  }

  def createChildSolrDocument(city:String, lat:Double, lng:Double, time:List[Long]) : SolrInputDocument = {
    val solrDocument = new SolrInputDocument()
    solrDocument.addField("city_t", city)
    solrDocument.addField("latitude_d", lat)
    solrDocument.addField("longitude_d", lng)
    solrDocument.addField("coord_p", lat + "," + lng)
    solrDocument.addField("time_ls", time.asJava)
    solrDocument.addField("id", city + time + lat + lng)
    solrDocument
  }

}
