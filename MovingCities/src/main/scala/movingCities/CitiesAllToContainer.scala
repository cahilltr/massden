package movingCities

/**
  * Created by cahillt on 3/28/16.
  * Contains lat, long, and all to cities
  */
class CitiesAllToContainer(toCity:String, lat:String, lng:String) {

  private var fromCities : Map[String, List[String]] = Map()

  def putToCity(city:String, time:String): Unit = {
    if (fromCities.contains(city)) {
      val newList = fromCities.get(city).get ::: List(time)
      fromCities += (city -> newList)
    } else {
      fromCities += (city -> List(time))
    }
  }

  def getToCities: Map[String, List[String]] = {
    fromCities
  }

  def getCity:String = toCity

  def getLat: String = lat

  def getLng: String = lng
}
