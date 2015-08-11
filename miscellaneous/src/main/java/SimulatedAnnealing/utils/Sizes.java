package SimulatedAnnealing.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cahillt on 7/28/15.
 */
public class Sizes {

  private Map<String, Double> sizes = new HashMap<>();

  public void addSize(String school, double size) {
    this.sizes.put(school, size);
  }

  public Map<String, Double> getAllCurrentSizes() {
    return this.sizes;
  }

  public double getAverageSize(List<String> schools, Map<String, SchoolInfo> schoolInfoMap, int conferenceSize) {
    double averageSize;
    //Base BBB GBB FB BSoc GSoc SB VB
    double base = 0;
    double bbb = 0;
    double gbb = 0;
    double fb = 0;
    double bsoc = 0;
    double gsoc = 0;
    double sb = 0;
    double vb = 0;

    for (String school : schools) {
      SchoolInfo schoolInfo = schoolInfoMap.get(school);
      base += getSize(schoolInfo.getBase());
      bbb += getSize(schoolInfo.getBBB());
      gbb += getSize(schoolInfo.getGBB());
      fb += getSize(schoolInfo.getFB());
      bsoc += getSize(schoolInfo.getBSoc());
      gsoc += getSize(schoolInfo.getGSoc());
      sb += getSize(schoolInfo.getSB());
      vb += getSize(schoolInfo.getVB());
    }

    base = base / conferenceSize;
    bbb = bbb / conferenceSize;
    gbb = gbb / conferenceSize;
    fb = fb / conferenceSize;
    bsoc = bsoc / conferenceSize;
    gsoc = gsoc / conferenceSize;
    vb = vb / conferenceSize;
    sb = sb / conferenceSize;
    averageSize = base + bbb + gbb + fb + gsoc + bsoc + vb + sb;

    return averageSize/8; //Divided by number of sports
  }

  public double getSchoolSize(String school, Map<String, SchoolInfo> schoolInfoMap) {
    if (this.sizes.containsKey(school)) {
      return this.sizes.get(school);
    }
    SchoolInfo schoolInfo = schoolInfoMap.get(school);
    double base = getSize(schoolInfo.getBase());
    double bbb = getSize(schoolInfo.getBBB());
    double gbb = getSize(schoolInfo.getGBB());
    double fb = getSize(schoolInfo.getFB());
    double bsoc = getSize(schoolInfo.getBSoc());
    double gsoc = getSize(schoolInfo.getGSoc());
    double sb = getSize(schoolInfo.getSB());
    double vb = getSize(schoolInfo.getVB());
    double size =  ((base * 2) + (bbb * 2) + (gbb * 2) + (fb * 2.5) + (gsoc * 2) + (bsoc * 2) + (vb * 2) + (sb * 2))/8;
    this.sizes.put(school, size);
    return size;
  }

  public double getSchoolSizeNew(String school, Map<String, SchoolInfo> schoolInfoMap) {
    SchoolInfo schoolInfo = schoolInfoMap.get(school);
    double base = getSize(schoolInfo.getBase());
    double bbb = getSize(schoolInfo.getBBB());
    double gbb = getSize(schoolInfo.getGBB());
    double fb = getSize(schoolInfo.getFB());
    double bsoc = getSize(schoolInfo.getBSoc());
    double gsoc = getSize(schoolInfo.getGSoc());
    double sb = getSize(schoolInfo.getSB());
    double vb = getSize(schoolInfo.getVB());
    double size =  (base + bbb + gbb + fb + gsoc + bsoc + vb + sb)/8;
    return size;
  }

  private int getSize(String size) {
    switch (size) {
      case "A":
        return 1;
      case "2A":
        return 2;
      case "3A":
        return 3;
      case "4A":
        return 4;
      case "5A":
        return 5;
      case "6A":
        return 6;
      case "-":
        return 0;
      default:
        return 0;
    }
  }

}
