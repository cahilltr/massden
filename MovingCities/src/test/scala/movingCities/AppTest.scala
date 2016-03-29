package movingCities

import org.junit._
import Assert._

@Test
class AppTest {

    val movingCities = new MovingCities()

    val webpage = movingCities.getCitiesWebPage()
    assertNotNull(webpage)

    val testSource = scala.io.Source.fromFile(this.getClass.getClassLoader.getResource("index.html").toURI)
    val lines = try testSource.mkString finally testSource.close()

    val cityList = movingCities.parseForTable(lines)

    movingCities.readCitysLatLong()

    assertTrue(cityList.length == 20)
    cityList.foreach( c => {
        val splits = c.split("\\|")
        println(splits.apply(1) + " : " + movingCities.getGeoPoint(splits.apply(1)))
        println(splits.apply(2) + " : " + movingCities.getGeoPoint(splits.apply(2)))
    })

    movingCities.writeOutCitysLatLong()


    @Test
    def testOK() = assertTrue(true)

}


