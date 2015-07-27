package SimulatedAnnealing.utils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by cahillt on 7/24/15.
 * Utils to clean up the Acutal simulated Annealing Process
 */
public class Utils {

  public static double getAverageSize(List<String> schools, Map<String, SchoolInfo> schoolInfoMap, int conferenceSize) {
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

  public static  double getSchoolSize(String school, Map<String, SchoolInfo> schoolInfoMap) {
    SchoolInfo schoolInfo = schoolInfoMap.get(school);
    double base = getSize(schoolInfo.getBase());
    double bbb = getSize(schoolInfo.getBBB());
    double gbb = getSize(schoolInfo.getGBB());
    double fb = getSize(schoolInfo.getFB());
    double bsoc = getSize(schoolInfo.getBSoc());
    double gsoc = getSize(schoolInfo.getGSoc());
    double sb = getSize(schoolInfo.getSB());
    double vb = getSize(schoolInfo.getVB());
    return (base + bbb + gbb + fb + gsoc + bsoc + vb + sb)/8;
  }

  public static Map<String, Double> sortMapDouble(Map<String, Double> distances) {
    final Map<String, Double> results = new LinkedHashMap<>();
    Stream<Map.Entry<String, Double>> st = distances.entrySet().stream();

    st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> results.put(e.getKey(), e.getValue()));
    return results;
  }

  public static Map<String, Double> sortClosestToPoint(double point, Map<String, Double> points) {
    Map<String, Double> sorted = new HashMap<>();

    for (Map.Entry<String, Double> p : points.entrySet()) {
      sorted.put(p.getKey(), Math.abs(point - p.getValue()));
    }

    return Utils.sortMapDouble(sorted);
  }

  public static double getDistance(String school1, String school2, Map<String, SchoolInfo> schoolInfoMap) {
    SchoolInfo s1 = schoolInfoMap.get(school1);
    SchoolInfo s2 = schoolInfoMap.get(school2);
    return getDistance(s1.getLat(), s1.getLong(), s2.getLat(), s2.getLong());
  }

  private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon1 - lon2;
    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    return (dist);
  }

  private static double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
  }

  private static double rad2deg(double rad) {
    return (rad * 180.0 / Math.PI);
  }

  private static int getSize(String size) {
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
