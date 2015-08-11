package com.avalon.coe;

import com.avalon.coe.utils.Distances;
import com.avalon.coe.utils.SchoolInfo;
import com.avalon.coe.utils.Sizes;
import com.avalon.coe.utils.Utils;

import java.util.*;

/**
 * Created by cahillt on 7/28/15.
 */
public class Neighbour {

  private double schoolsSmall = 1.25;
  private double schoolsBig = 2.85;


  public Neighbour() {}

  public Neighbour(double radius) {
  }






  public Map<String, List<String>> newConferencesDistanceAndSize(
          List<String> schools, String conf1, String conf2, String conf3, Map<String, SchoolInfo> schoolInfoMap,
          Distances distances, int conferenceSize, Sizes sizes) {
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
      Double d = distances.getDistance(school, s1, schoolInfoMap);
      schoolsDistances.put(school, d);
      schoolSizes.put(school, sizes.getSchoolSize(school, schoolInfoMap));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, sizes.getSchoolSize(s1, schoolInfoMap), conferenceSize);
    int i = 0;
    int size = schoolsDistances.size();
    List<String> s1Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < conferenceSize - 1) {
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
      Double d = distances.getDistance(school, s2, schoolInfoMap);
      schoolsDistances.put(school, d);
      schoolSizes.put(school, sizes.getSchoolSize(school, schoolInfoMap));
    }
    schoolsDistances = sortDistanceAndSize(schoolsDistances, schoolSizes, sizes.getSchoolSize(s2, schoolInfoMap), conferenceSize);
    i = 0;
    List<String> s2Conference = new ArrayList<>();
    for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
      if (i < conferenceSize - 1) {
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

  private Map<String, Double> sortDistanceAndSize(Map<String, Double> distances, Map<String, Double> size,
                                                  double checkSize, int conferenceSize) {
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
      //TODO i++ or some thing like that
      int i = medSchools.size();
      if (i != conferenceSize) {
        for (Map.Entry<String, Double> school : sortedSize.entrySet()) {
          if (i == conferenceSize) {
            break;
          }
          String key = school.getKey();
          score.put(key, distances.get(key));
        }
      }
    }
    return score;
  }

}
