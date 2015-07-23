package SimulatedAnnealing;

import SimulatedAnnealing.utils.SchoolInfo;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by cahillt on 7/21/15.
 * This class is used to do the acutal Simulated Annealing
 * SA currently works best when using Distance only
 */
public class SimulatedAnnealingProcess {


  private Map<String, List<String>> currentConferences;
  private Map<String, List<String>> bestConferences;
  private double travelWeight;
  private double sizeWeight;
  private Map<String, SchoolInfo> schoolInfoMap;
  private int conferenceSize = 10;

  private double schoolsSmall = 1.25;
  private double schoolsBig = 2.85;

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, Map<String, SchoolInfo> schoolInfoMap) {
    this.travelWeight = travelWeight;
    this.sizeWeight = sizeWeight;
    this.currentConferences = new HashMap<>();
    this.schoolInfoMap = schoolInfoMap;
  }

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, double schoolsSmall, double schoolsBig,
                                   Map<String, SchoolInfo> schoolInfoMap) {

    this(travelWeight, sizeWeight, schoolInfoMap);
    this.schoolsSmall = schoolsSmall;
    this.schoolsBig = schoolsBig;
  }


  public void initConferences() {
    ArrayList<String> schools = new ArrayList<>(this.schoolInfoMap.keySet());
    Collections.shuffle(schools);

    int confNum = 0;
    int curSchoolCount = 0;
    for (String school : schools) {
      if (curSchoolCount == conferenceSize) {
        confNum++;
        this.currentConferences.put(confNum + "", new ArrayList<>());
        this.currentConferences.get(confNum + "").add(school);
        curSchoolCount = 0;
      } else {
        if (!this.currentConferences.containsKey(confNum + "")) {
          this.currentConferences.put(confNum + "", new ArrayList<>());
        }
        this.currentConferences.get(confNum + "").add(school);
      }
      curSchoolCount++;
    }
    this.bestConferences = new HashMap<>(this.currentConferences);
  }

  public double getScore(Map<String, List<String>> conference) {
    double averageDistanceConf = 0;
    double averageSizeConf = 0;
    for (Map.Entry<String, List<String>> entry : conference.entrySet()) {
      List<String> schools = entry.getValue();
      averageDistanceConf += getAverageDistances(new ArrayList<>(schools), 0);
      averageSizeConf += getAverageSize(schools);
    }
    return ((averageSizeConf / this.currentConferences.size()) * this.sizeWeight) +
            ((averageDistanceConf / this.currentConferences.size()) * this.travelWeight);
  }

  public static double acceptanceProbability(double energy, double newEnergy, double temperature) {
    // If the new solution is better, accept it
    if (newEnergy < energy) {
      return 1.0;
    }
    // If the new solution is worse, calculate an acceptance probability
    return Math.exp((energy - newEnergy) / temperature);
  }


  private double getAverageSize(List<String> schools) {
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
      SchoolInfo schoolInfo = this.schoolInfoMap.get(school);
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

  private double getSchoolSize(String school) {
    SchoolInfo schoolInfo = this.schoolInfoMap.get(school);
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

  private double getAverageDistances(List<String> schools, double distance) {
    if (schools.size() == 0 ) {
      return 0;
    }
    SchoolInfo schoolInfo = this.schoolInfoMap.get(schools.get(0));
    schools.remove(schools.get(0));
    for (String otherSchool : schools) {
      SchoolInfo otherSchoolInfo = this.schoolInfoMap.get(otherSchool);
      distance += getDistance(schoolInfo.getLat(), schoolInfo.getLong(), otherSchoolInfo.getLat(), otherSchoolInfo.getLong());
    }
    if (schools.size() == 2) {
      return distance;
    } else {
      distance += getAverageDistances(schools, distance);
    }
    return distance / conferenceSize;
  }

  private double getDistance(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon1 - lon2;
    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    return (dist);
  }

  private double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
  }

  private double rad2deg(double rad) {
    return (rad * 180.0 / Math.PI);
  }

  public Map<String, List<String>> getNeighbour(Map<String, List<String>> conferences) {
    List confNames = new ArrayList<>(conferences.keySet());
    Collections.shuffle(confNames);
    String conf1 = confNames.get(0).toString();
    String conf2 = confNames.get(1).toString();
    String conf3 = confNames.get(2).toString();

    //merge conferences
    List<String> mergedSchools = new ArrayList<>();
    mergedSchools.addAll(conferences.get(conf1));
    mergedSchools.addAll(conferences.get(conf2));
    mergedSchools.addAll(conferences.get(conf3));

    conferences.remove(conf1);
    conferences.remove(conf2);
    conferences.remove(conf3);

    //Redistribute Conferences by distance
//    Map<String, List<String>> newConferences = newConferencesDistanceOnly(mergedSchools, conf1, conf2, conf3);
//    conferences.putAll(newConferences);

    //Redisctribute by Size of school and distance
    Map<String, List<String>> newConferences = newConferencesDistanceAndSize(mergedSchools, conf1, conf2, conf3);
    conferences.putAll(newConferences);

    return conferences;
  }

  private Map<String, Double> sortMapDouble(Map<String, Double> distances) {
    final Map<String, Double> results = new LinkedHashMap<>();
    Stream<Map.Entry<String, Double>> st = distances.entrySet().stream();

    st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> results.put(e.getKey(), e.getValue()));
    return results;
  }

  private Map<String, Double> sortDistanceAndSize(Map<String, Double> distances, Map<String, Double> size, double checkSize) {
    Map<String, Double> sortedDistances = sortMapDouble(distances);

    List<String> bigSchools = new ArrayList<>();
    List<String> smallSchools = new ArrayList<>();
    List<String> medSchools = new ArrayList<>();

    Map<String, Double> score = new HashMap<>();
    for (Map.Entry<String, Double> s : sortedDistances.entrySet()) {
      String key = s.getKey();
      if (size.get(key) <= schoolsSmall) {
        smallSchools.add(key);
      } else if (size.get(key) >= schoolsBig) {
        bigSchools.add(key);
      } else {
        medSchools.add(key);
      }
    }

    if (checkSize <= schoolsSmall) {
      for (String s : smallSchools) {
        score.put(s, distances.get(s));
      }
      for (String s : medSchools) {
        score.put(s, distances.get(s));
      }
      for (String s : bigSchools) {
        score.put(s, distances.get(s));
      }
    } else if (checkSize >= schoolsBig){
      for (String s : bigSchools) {
        score.put(s, distances.get(s));
      }
      for (String s : medSchools) {
        score.put(s, distances.get(s));
      }
      for (String s : smallSchools) {
        score.put(s, distances.get(s));
      }
    } else {
      Map<String, Double> sortedSize = sortClosestToPoint(checkSize, size);
      for (String s : medSchools) {
        score.put(s, distances.get(s));
        sortedSize.remove(s);
      }
      int i = medSchools.size();
      if (i != this.conferenceSize) {
        for (Map.Entry<String, Double> school : sortedSize.entrySet()) {
          if (i == this.conferenceSize) {
            break;
          }
          String key = school.getKey();
          score.put(key, distances.get(key));
        }
      }
    }

    return score;
  }

  public Map<String, Double> sortClosestToPoint(double point, Map<String, Double> points) {
    Map<String, Double> sorted = new HashMap<>();

    for (Map.Entry<String, Double> p : points.entrySet()) {
      sorted.put(p.getKey(), Math.abs(point - p.getValue()));
    }

    return sortMapDouble(sorted);
  }

  public Map<String, List<String>> newConferencesDistanceAndSize(List<String> schools, String conf1, String conf2, String conf3) {
    if (schools.size() == 0) {
      return null;
    }
    Collections.shuffle(schools);

    String s1 = schools.get(0);
    SchoolInfo school1 = this.schoolInfoMap.get(s1);
    schools.remove(s1);

    String s2 = null;

    Map<String, Double> schoolsDistances = new HashMap<>();
    Map<String, Double> schoolSizes = new HashMap<>();
    for (String school : schools) {
      SchoolInfo otherSchoolInfo = this.schoolInfoMap.get(school);
      Double d = getDistance(school1.getLat(), school1.getLong(), otherSchoolInfo.getLat(), otherSchoolInfo.getLong());
      schoolsDistances.put(school, d);
      schoolSizes.put(school, getSchoolSize(school));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, getSchoolSize(s1));
    int i = 0;
    int size = schoolsDistances.size();
    List<String> s1Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < this.conferenceSize - 1) {
        s1Conference.add(entry.getKey());
      } else if (i == (size - 1)) {
        s2 = entry.getKey();
      }
      i++;
    }
    schools.remove(s2);
    schools.removeAll(s1Conference);

    SchoolInfo school2 = this.schoolInfoMap.get(s2);
    schoolsDistances = new HashMap<>();
    schoolSizes = new HashMap<>();
    for (String school : schools) {
      SchoolInfo otherSchoolInfo = this.schoolInfoMap.get(school);
      Double d = getDistance(school2.getLat(), school2.getLong(), otherSchoolInfo.getLat(), otherSchoolInfo.getLong());
      schoolsDistances.put(school, d);
      schoolSizes.put(school, getSchoolSize(school));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, getSchoolSize(s2));
    i = 0;
    List<String> s2Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < this.conferenceSize - 1) {
        s2Conference.add(entry.getKey());
      } else {
        break;
      }
      i++;
    }
    schools.removeAll(s2Conference);

    Map<String, List<String>> newConferences = new HashMap<>();
    s1Conference.add(s1);
    newConferences.put(conf1, s1Conference);
    s2Conference.add(s2);
    newConferences.put(conf2, s2Conference);
    newConferences.put(conf3, schools);
    return newConferences;
  }

  public Map<String, List<String>> newConferencesDistanceOnly(List<String> schools, String conf1, String conf2, String conf3) {
    if (schools.size() == 0) {
      return null;
    }
    Collections.shuffle(schools);

    String s1 = schools.get(0);
    SchoolInfo school1 = this.schoolInfoMap.get(s1);
    schools.remove(s1);

    String s2 = null;
    String s3 = null;

    Map<String, Double> schoolsDistances = new HashMap<>();
    for (String school : schools) {
      SchoolInfo otherSchoolInfo = this.schoolInfoMap.get(school);
      Double d = getDistance(school1.getLat(), school1.getLong(), otherSchoolInfo.getLat(), otherSchoolInfo.getLong());
      schoolsDistances.put(school, d);
    }
    schoolsDistances = sortMapDouble(schoolsDistances);
    int i = 0;
    int size = schoolsDistances.size();
    List<String> s1Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < this.conferenceSize - 1) {
        s1Conference.add(entry.getKey());
      } else if (i == (size - 1)) {
        s2 = entry.getKey();
      }
      i++;
    }
    schools.remove(s2);
    schools.removeAll(s1Conference);

    SchoolInfo school2 = this.schoolInfoMap.get(s2);
    schoolsDistances = new HashMap<>();
    for (String school : schools) {
      SchoolInfo otherSchoolInfo = this.schoolInfoMap.get(school);
      Double d = getDistance(school2.getLat(), school2.getLong(), otherSchoolInfo.getLat(), otherSchoolInfo.getLong());
      schoolsDistances.put(school, d);
    }
    schoolsDistances = sortMapDouble(schoolsDistances);
    i = 0;
    size = schoolsDistances.size();
    List<String> s2Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < this.conferenceSize - 1) {
        s2Conference.add(entry.getKey());
      } else if (i == (size - 1)){
        s3 = entry.getKey();
      }
      i++;
    }
    schools.remove(s3);
    schools.removeAll(s2Conference);

    Map<String, List<String>> newConferences = new HashMap<>();
    s1Conference.add(s1);
    newConferences.put(conf1, s1Conference);
    s2Conference.add(s2);
    newConferences.put(conf2, s2Conference);
    schools.add(s3);
    newConferences.put(conf3, schools);
    return newConferences;
  }


  public Map<String, List<String>> simulatedAnnealing() {

    initConferences();

    double coolingRate = 0.002;
    double temp = 10000;
    Map<String, List<String>> newConferences;
    while (temp > 1) {

      //Get Neighbour
      newConferences = getNeighbour(new HashMap<>(this.currentConferences));

      double currentEnergy = getScore(this.currentConferences);
      double neighbourEnergy = getScore(newConferences);

      if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
        this.currentConferences = new HashMap<>(newConferences);
      }

      if (neighbourEnergy < getScore(this.bestConferences)) {
        this.bestConferences = new HashMap<>(newConferences);
      }

      temp *= 1 - coolingRate;
    }

    return bestConferences;
  }

  /* Goals
   * Reduce travel distance
   * Smiliar sized schools play each other
   */
}