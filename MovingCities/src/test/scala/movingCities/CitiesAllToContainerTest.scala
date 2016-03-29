package movingCities

import org.junit.Test
import org.junit._
import Assert._

/**
  * Created by cahillt on 3/28/16.
  * Test put and get of map
  */
@Test
class CitiesAllToContainerTest {


  val citiesAllToContainer = new CitiesAllToContainer("Fake", "Fake1", "Fake2")

  @Test
  def doTest() : Unit = {
    assertEquals("Fake", citiesAllToContainer.getCity)
    assertEquals("Fake1", citiesAllToContainer.getLat)
    assertEquals("Fake2", citiesAllToContainer.getLng)

    citiesAllToContainer.putToCity("test1", "time1")
    citiesAllToContainer.putToCity("test2", "test2time1")
    citiesAllToContainer.putToCity("test1", "time2")

    val fromCities = citiesAllToContainer.getToCities

    assertEquals(2, fromCities.size)
    assertEquals(2, fromCities.get("test1").get.size)
    assertEquals("time1", fromCities.get("test1").get.head)
    assertEquals("time2", fromCities.get("test1").get.tail.head)
    assertEquals(1, fromCities.get("test2").get.size)
    assertEquals("test2time1", fromCities.get("test2").get.head)

  }
}
