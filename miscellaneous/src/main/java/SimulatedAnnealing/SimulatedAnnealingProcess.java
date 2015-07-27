package SimulatedAnnealing;

import SimulatedAnnealing.utils.Conferences;
import SimulatedAnnealing.utils.SchoolInfo;
import SimulatedAnnealing.utils.Utils;

import java.util.*;

/**
 * Created by cahillt on 7/21/15.
 * This class is used to do the actual Simulated Annealing
 * SA currently works best when using Distance only
 */
public class SimulatedAnnealingProcess {

  private Conferences currentConferences;
  private Conferences bestConferences;
  private double travelWeight;
  private double sizeWeight;
  private Map<String, SchoolInfo> schoolInfoMap;
  private int conferenceSize = 10;
  private Map<String, Double> schoolDistances = new HashMap<String, Double>();

  private double schoolsSmall = 1.25;
  private double schoolsBig = 2.85;

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, Map<String, SchoolInfo> schoolInfoMap) {
    this.travelWeight = travelWeight;
    this.sizeWeight = sizeWeight;
    this.schoolInfoMap = schoolInfoMap;
  }

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, double schoolsSmall, double schoolsBig,
                                   Map<String, SchoolInfo> schoolInfoMap) {

    this(travelWeight, sizeWeight, schoolInfoMap);
    this.schoolsSmall = schoolsSmall;
    this.schoolsBig = schoolsBig;
  }

  public double getScore(Conferences conference) {
    double averageDistanceConf = 0;
    double averageSizeConf = 0;
    for (Map.Entry<String, List<String>> entry : conference.getConferences().entrySet()) {
      List<String> schools = entry.getValue();
      averageDistanceConf += getAverageDistances(new ArrayList<>(schools), 0);
      averageSizeConf += Utils.getAverageSize(schools, this.schoolInfoMap, this.conferenceSize);
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

  private double getAverageDistances(List<String> schools, double distance) {
    if (schools.size() == 0 ) {
      return 0;
    }

    String s1 = schools.get(0);
    schools.remove(schools.get(0));
    for (String otherSchool : schools) {
      distance += getDistance(s1, otherSchool);
    }
    if (schools.size() == 2) {
      return distance;
    } else {
      distance += getAverageDistances(schools, distance);
    }
    return distance / conferenceSize;
  }

  private double getDistance(String s1, String otherSchool) {
    if (this.schoolDistances.containsKey(otherSchool + s1)) {
      return schoolDistances.get(otherSchool + s1);
    } else if (schoolDistances.containsKey(s1 + otherSchool)) {
      return schoolDistances.get(s1 + otherSchool);
    } else {
      double d = Utils.getDistance(otherSchool, s1, this.schoolInfoMap);
      schoolDistances.put(otherSchool + s1, d);
      return d;
    }
  }

  public Conferences getNeighbour(Conferences conferences) {
//    List confNames = new ArrayList<>(conferences.getConferenceNames());
//    Collections.shuffle(confNames);
//    String conf1 = confNames.get(0).toString();
//    String conf2 = confNames.get(1).toString();
//    String conf3 = confNames.get(2).toString();
//
//    //merge conferences
//    List<String> mergedSchools = new ArrayList<>();
//    mergedSchools.addAll(conferences.getConference(conf1));
//    mergedSchools.addAll(conferences.getConference(conf2));
//    mergedSchools.addAll(conferences.getConference(conf3));
//
//    conferences.removeConference(conf1);
//    conferences.removeConference(conf2);
//    conferences.removeConference(conf3);

    //Redisctribute by Size of school and distance
//    Map<String, List<String>> newConferences = newConferencesDistanceAndSize(mergedSchools, conf1, conf2, conf3);
//    conferences.putAll(newConferences);

    List<String> allSchools = conferences.getAllSchools();
    Map<String, List<String>> newConferences = newConferencesDistanceBased(allSchools);
    conferences.clearConferences();
    conferences.setConferences(newConferences);

    return conferences;
  }

  private Map<String, Double> sortDistanceAndSize(Map<String, Double> distances, Map<String, Double> size, double checkSize) {
    Map<String, Double> sortedDistances = Utils.sortMapDouble(distances);

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
      Map<String, Double> sortedSize = Utils.sortClosestToPoint(checkSize, size);
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



  public Map<String, List<String>> newConferencesDistanceAndSize(List<String> schools, String conf1, String conf2, String conf3) {
    //TODO Make me better
    if (schools.size() == 0) {
      return null;
    }
    Collections.shuffle(schools);

    String s1 = schools.get(0);
    schools.remove(s1);

    String s2 = null;

    Map<String, Double> schoolsDistances = new HashMap<>();
    Map<String, Double> schoolSizes = new HashMap<>();
    for (String school : schools) {
      Double d = getDistance(school, s1);
      schoolsDistances.put(school, d);
      schoolSizes.put(school, Utils.getSchoolSize(school, this.schoolInfoMap));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, Utils.getSchoolSize(s1, this.schoolInfoMap));
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

    schoolsDistances = new HashMap<>();
    schoolSizes = new HashMap<>();
    for (String school : schools) {
      Double d = getDistance(school, s2);
      schoolsDistances.put(school, d);
      schoolSizes.put(school, Utils.getSchoolSize(school, this.schoolInfoMap));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, Utils.getSchoolSize(s2, this.schoolInfoMap));
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

  public Map<String, List<String>> newConferencesDistanceBased(List<String> schools) {
    //Assumes List of all schools

    //randomly select some schools
    Collections.shuffle(schools);

    int numSeedSchools = schools.size() / this.conferenceSize;
    List<String> seedSchools = new ArrayList<>(numSeedSchools);
    for (int i = 0; i < numSeedSchools; i++) {
      seedSchools.add(schools.get(i));
      schools.remove(i);
    }

    //For each seed school find closest schools;
    Map<String, List<String>> newConferences = new HashMap<>();
    for (int j = 0; j < numSeedSchools - 1; j++) {

      String seedSchool = seedSchools.get(j);

      Map<String, Double> schoolsDistances = new HashMap<>();
      for (String school : schools) {
        Double d = getDistance(school, seedSchool);
        schoolsDistances.put(school, d);
      }

      schoolsDistances = Utils.sortMapDouble(schoolsDistances);
      int i = 0;
      List<String> conference = new ArrayList<>();
      for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
        if (i < this.conferenceSize - 1) {
          conference.add(entry.getKey());
        } else {
          break;
        }
        i++;
      }

      schools.removeAll(conference);
      conference.add(seedSchool);
      newConferences.put((j + 1) + "", conference);
    }

    schools.add(seedSchools.get(numSeedSchools - 1));
    newConferences.put((numSeedSchools) + "", schools);

    return newConferences;
  }

  public Conferences simulatedAnnealing() {

    Conferences conferences = new Conferences(this.conferenceSize);
    conferences.initConferences(this.schoolInfoMap);
    this.bestConferences = new Conferences(conferences);
    this.currentConferences = new Conferences(conferences);

    double coolingRate = 0.003;
//    double temp = 10000;
    double temp = 100000;
    Conferences newConferences;
    while (temp > 1) {

      //Get Neighbour
      newConferences = getNeighbour(new Conferences(this.currentConferences));

      double currentEnergy = getScore(this.currentConferences);
      double neighbourEnergy = getScore(newConferences);

      if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
        this.currentConferences = new Conferences(newConferences);
      }

      if (neighbourEnergy < getScore(this.bestConferences)) {
        this.bestConferences = new Conferences(newConferences);
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