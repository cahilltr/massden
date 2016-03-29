package movingCities.hbase

import movingCities.{CitiesAllToContainer}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.LogManager

import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * Created by cahillt on 3/23/16.
  * HBase handler for spark streaming.
  */
class HBaseHandler (tableName:String, conf: Configuration) {
  val log = LogManager.getLogger(this.getClass)

  def this(tableName:String) = this(tableName, HBaseConfiguration.create())

  private val config = conf
  private var conn = ConnectionFactory.createConnection(config)

  private var admin = conn.getAdmin
  private final val name = TableName.valueOf(tableName)
  private val htable = conn.getTable(name)

  private final val coordFamily = Bytes.toBytes("coords")
  private final val latQualifier = Bytes.toBytes("lat")
  private final val longQualifier = Bytes.toBytes("lng")

  private final val timeFamily = Bytes.toBytes("times")

  try {
    if (!admin.tableExists(name)) {
      throw new Exception("HBase table " + tableName + "does not exist. Please create and populate.")
    }
  } catch {
    case e:Exception =>
      log.error(e)
      reconnectConnection()
      log.info("Reconnected Admin for initialization")
      if (!admin.tableExists(name)) {
        throw new Exception("HBase table " + tableName + "does not exist. Please create and populate.")
      }
  }

  def getCityLocation(cityState:String) : String = {
    val getRow = new Get(Bytes.toBytes(cityState))

    if (checkTableExists(getRow)) {
      val result = getARow(getRow)
      val lat = Bytes.toString(CellUtil.cloneValue(result.getColumnLatestCell(coordFamily, latQualifier)))
      val lng = Bytes.toString(CellUtil.cloneValue(result.getColumnLatestCell(coordFamily, longQualifier)))
      lat + "," + lng
    } else {
      ""
    }
  }

  def checkTableExists(aGet:Get) : Boolean = {
    try {
      htable.exists(aGet)
    } catch {
      case e:Exception =>
        log.error(e)
        reconnectConnection()
        log.info("Reconnected")
        checkTableExists(aGet)
    }
  }

  def getARow(aGet:Get) : Result = {
    try {
      htable.get(aGet)
    } catch {
      case e:Exception =>
        log.error(e)
        reconnectConnection()
        log.info("Reconnected")
        getARow(aGet)
    }
  }

  def putARow(aPut: Put) : Unit = {
    try {
      htable.put(aPut)
    } catch {
      case e:Exception =>
        log.error(e)
        reconnectConnection()
        log.info("Reconnected")
        putARow(aPut)
    }
  }

  def putCityLocation (cityState: String, latLng: String) : Boolean = {
    val bytes = Bytes.toBytes(cityState)
    val getRow = new Get(bytes)
    val putRow = new Put(bytes)

    if (!checkTableExists(getRow)) {
      val splits = latLng.split(",")
      if (splits.length != 2) return false
      putRow.addColumn(coordFamily, Bytes.toBytes("lat"), Bytes.toBytes(splits.apply(0)))
      putRow.addColumn(coordFamily, Bytes.toBytes("lng"), Bytes.toBytes(splits.apply(1)))
      putARow(putRow)
    }
    true
  }

  def putToCity(cityState:String, toCityState:String, time:String) : Boolean = {
    val bytes = Bytes.toBytes(cityState)
    val getRow = new Get(bytes)
    val putRow = new Put(bytes)

    if (checkTableExists(getRow)) {
      putRow.addColumn(timeFamily, Bytes.toBytes(time.toString), Bytes.toBytes(toCityState))
      putARow(putRow)
      true
    } else {
      false
    }
  }

  def getToCities(cityState:String) : Map[String, String] = {
    val getRow = new Get(Bytes.toBytes(cityState))
    if (checkTableExists(getRow)) {
      val results = getARow(getRow)
      val familyMap = results.getFamilyMap(timeFamily)
      val ksIter = familyMap.keySet().iterator()
      var returnMap = new HashMap[String, String]()
      while(ksIter.hasNext) {
        val n = ksIter.next()
        val qualifier = Bytes.toString(n)
        val value = Bytes.toString(familyMap.get(n))
        returnMap += (qualifier -> value)
      }
      returnMap
    } else {
      null
    }
  }

  def scan() : List[CitiesAllToContainer] = {
    val scan = new Scan
    scan.addFamily(coordFamily)
    scan.addFamily(timeFamily)

    var returnList = mutable.MutableList[CitiesAllToContainer]()
    val scanIter = htable.getScanner(scan).iterator()
    while (scanIter.hasNext) {
      val results = scanIter.next
      val city = Bytes.toString(results.getRow)
      val lat = Bytes.toString(CellUtil.cloneValue(results.getColumnLatestCell(coordFamily, latQualifier)))
      val lng = Bytes.toString(CellUtil.cloneValue(results.getColumnLatestCell(coordFamily, longQualifier)))
      val citiesAllToContainer = new CitiesAllToContainer(city, lat, lng)

      val familyMap = results.getFamilyMap(timeFamily)
      val familyIter = familyMap.keySet().iterator()
      while (familyIter.hasNext) {
        val qualifier = familyIter.next()
        val value = Bytes.toString(familyMap.get(qualifier))
        citiesAllToContainer.putToCity(value, Bytes.toString(qualifier))
      }
      returnList += citiesAllToContainer
    }
    returnList.toList
  }

  def reconnectConnection(): Unit = {
    conn = ConnectionFactory.createConnection(config)
    admin = conn.getAdmin
  }

  def closeConnection(): Unit = {
    conn.close()
  }

}
