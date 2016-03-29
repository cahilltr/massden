package movingCities.hbase

import HBaseHandlerTest._
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.{CellUtil, HBaseTestingUtility}
import org.apache.hadoop.hbase.util.Bytes
import org.junit._
import Assert._

/**
  * Created by cahillt on 3/23/16.
  * Test for HBase handling.
  * Need the object HBaseHanlder test because HBaseTestingUtility needs to be static to start. Note the import above of
  * HBaseHandlerTest._
  * http://docs.scala-lang.org/tutorials/tour/singleton-objects.html
  */
@Test
class HBaseHandlerTest {

  final val TEST_TABLE_NAME = "testTable"
  final val coordFamily = Bytes.toBytes("coords")
  final val latQualifier = Bytes.toBytes("lat")
  final val longQualifier = Bytes.toBytes("lng")
  final val timeFamily = Bytes.toBytes("times")

  final val newToCity = "Chanute,Ks"

  val testTable = testUtil.createTable(Bytes.toBytes(TEST_TABLE_NAME), Array(coordFamily, timeFamily))

  val hBaseHandler = new HBaseHandler(TEST_TABLE_NAME, testUtil.getConfiguration)

  val citiesMap = Map("Indianapolis,In" -> Array("Austin, Tx", "34.000", "11.00"),
                      "Austin,Tx" -> Array("Indianapolis,In", "22.00", "23.23"),
                      "Phoenix,Az" -> Array("Charolotte,Nc", "83.23", "20.20"),
                      "Charlotte,Nc" -> Array("Phoenix,Az", "93.92", "00.02"))

  @Test
  def runTests(): Unit = {
    testPutCityLocation()
    testGetCityLocation()
    testGetToCities()
    testScanCities()
  }

  def testPutCityLocation(): Unit = {
    citiesMap.foreach(f => {
      hBaseHandler.putCityLocation(f._1, f._2.apply(1) + "," + f._2.apply(2))
      hBaseHandler.putToCity(f._1, f._2.apply(0), System.nanoTime.toString)
    })

    citiesMap.foreach(f = f => {
      val checkGet = new Get(Bytes.toBytes(f._1))
      assertTrue(testTable.exists(checkGet))

      val toCity = f._2.apply(0)
      val lat = f._2.apply(1)
      val lng = f._2.apply(2)

      val results = testTable.get(checkGet)
      assertEquals(lat, Bytes.toString(CellUtil.cloneValue(results.getColumnLatestCell(coordFamily, latQualifier))))
      assertEquals(lng, Bytes.toString(CellUtil.cloneValue(results.getColumnLatestCell(coordFamily, longQualifier))))

      val familyMap = results.getFamilyMap(timeFamily)
      assertEquals(1, familyMap.size())

      val ksIter = familyMap.keySet().iterator()

      while (ksIter.hasNext) {
        val n = ksIter.next()
        val quanlifier = Bytes.toString(n)
        val value = Bytes.toString(familyMap.get(n))
        assertNotNull(quanlifier)
        assertEquals(toCity, value)
      }

    })
  }

  def testGetCityLocation(): Unit = {
    citiesMap.foreach(f => {
      val latLng = hBaseHandler.getCityLocation(f._1)
      val splits = latLng.split(",")
      assertEquals(f._2.apply(1),splits.apply(0))
      assertEquals(f._2.apply(2),splits.apply(1))
    })
  }

  def testGetToCities(): Unit = {
    citiesMap.foreach(f => {
      val toCities = hBaseHandler.getToCities(f._1)
      val toCity = f._2.apply(0)
      assertEquals(1, toCities.size)
      assertEquals(toCity, toCities.get(toCities.keySet.head).get)
    })

    citiesMap.foreach( f => {
      hBaseHandler.putToCity(f._1, newToCity, System.nanoTime.toString)
    })

    citiesMap.foreach(f => {
      val toCities = hBaseHandler.getToCities(f._1)
      val toCity = f._2.apply(0)
      var keySets = toCities.keySet
      val testList = toCity :: newToCity :: Nil
      assertEquals(2, toCities.size)
      assertTrue(testList.contains(toCities.get(keySets.head).get))
      keySets -= keySets.head
      assertTrue(testList.contains(toCities.get(keySets.head).get))
    })
  }

  def testScanCities(): Unit = {
    val scannedList = hBaseHandler.scan()
    assertEquals(4, scannedList.size)
    scannedList.foreach( c => {
      assertNotNull(c.getCity)
      assertNotNull(c.getLat)
      assertNotNull(c.getLng)

      val toCities = c.getToCities
      assertTrue(toCities.keySet.contains(newToCity))
      toCities.foreach( f => {
        assertTrue(f._2.nonEmpty)
      })
    })
  }

}

object HBaseHandlerTest {
  Assume.assumeTrue(!System.getProperty("os.name").toLowerCase().contains("window"))
  val testUtil = new HBaseTestingUtility
  testUtil.startMiniCluster()
}
