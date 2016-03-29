package movingCities

/**
  * Created by cahillt on 3/24/16.
  * City Set Container
  */
class MovingCitiesContainer (time:String, toCity:String, fromCity:String) extends Serializable {

  private var toCityGeo:String = ""
  private var fromCityGeo:String = ""

  def getTime:String = time

  def getToCity:String = toCity

  def getFromCity:String = fromCity

  def setToCityGeo(geo:String) = toCityGeo = geo

  def setFromCityGeo(geo:String) = fromCityGeo = geo

}
