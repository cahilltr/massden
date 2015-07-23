package SimulatedAnnealing.utils;

/**
 * Created by cahillt on 7/20/15.
 * Class to hold school information
 */
public class SchoolInfo {

  private String school;
  private String address;
  private String base;
  private String BBB;
  private String GBB;
  private String FB;
  private String BSoc;
  private String GSoc;
  private String SB;
  private String VB;
  private String lat;
  private String lon;

  public SchoolInfo(String school, String address) {
    this.school = school;
    this.address = address;
  }

  public String getSchool() {
    return this.school;
  }

  public String getAddress() {
    return this.address;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public String getBase() {
    return this.base;
  }

  public void setBBB(String BBB) {
    this.BBB = BBB;
  }

  public String getBBB() {
    return this.BBB;
  }

  public void setGBB(String GBB) {
    this.GBB = GBB;
  }

  public String getGBB() {
    return this.GBB;
  }

  public void setFB(String FB) {
    this.FB = FB;
  }

  public String getFB() {
    return this.FB;
  }

  public void setGSoc(String GSoc) {
    this.GSoc = GSoc;
  }

  public String getGSoc() {
    return this.GSoc;
  }

  public void setBSoc(String BSoc) {
    this.BSoc = BSoc;
  }

  public String getBSoc() {
    return this.BSoc;
  }

  public void setSB(String SB) {
    this.SB = SB;
  }

  public String getSB() {
    return this.SB;
  }

  public void setVB(String VB) {
    this.VB = VB;
  }

  public String getVB() {
    return this.VB;
  }

  public void setLong(String lon) {
    this.lon = lon;
  }

  public double getLong() {
    return Double.parseDouble(this.lon);
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public double getLat() {
    return Double.parseDouble(this.lat);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.school);
    sb.append(",");
    sb.append(this.address);
    sb.append(",");
    sb.append(this.base);
    sb.append(",");
    sb.append(this.BBB);
    sb.append(",");
    sb.append(this.GBB);
    sb.append(",");
    sb.append(this.FB);
    sb.append(",");
    sb.append(this.GSoc);
    sb.append(",");
    sb.append(this.BSoc);
    sb.append(",");
    sb.append(this.SB);
    sb.append(",");
    sb.append(this.VB);
    sb.append(",");
    sb.append(this.lat);
    sb.append(",");
    sb.append(this.lon);
    return sb.toString();
  }

}