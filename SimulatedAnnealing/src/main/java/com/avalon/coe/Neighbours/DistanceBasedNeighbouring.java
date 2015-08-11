package com.avalon.coe.Neighbours;

import com.avalon.coe.utils.Distances;
import com.avalon.coe.utils.SchoolInfo;
import com.avalon.coe.utils.Utils;

import java.util.*;

/**
 * Created by cahillt on 7/28/15.
 * Class for getting a Neighbour in SA based off distance
 */
public class DistanceBasedNeighbouring implements Neighbouring {

  private Map<String, SchoolInfo> schoolInfoMap;
  private int conferenceSize;

  public DistanceBasedNeighbouring(Map<String, SchoolInfo> schoolInfoMap, int conferenceSize) {
    this.schoolInfoMap = schoolInfoMap;
    this.conferenceSize = conferenceSize;
  }

  @Override
  public Map<String, List<String>> getNewConferences
          (List<String> schools, Distances distances) {
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
        Double d = distances.getDistance(school, seedSchool, this.schoolInfoMap);
        schoolsDistances.put(school, d);
      }

      schoolsDistances = Utils.sortMapDouble(schoolsDistances);
      int i = 0;
      List<String> conference = new ArrayList<>();
      for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
        if (i < conferenceSize - 1) {
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

}
