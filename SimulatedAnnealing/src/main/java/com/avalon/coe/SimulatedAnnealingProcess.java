package com.avalon.coe;

import com.avalon.coe.Neighbours.DistanceAndSizeBasedNeighbouring;
import com.avalon.coe.Neighbours.DistanceBasedNeighbouring;
import com.avalon.coe.Neighbours.Neighbouring;
import com.avalon.coe.utils.*;

import java.util.*;

/**
 * Created by cahillt on 7/21/15.
 * This class is used to do the actual Simulated Annealing
 * SA currently works best when using Distance only
 */
public class SimulatedAnnealingProcess {

  private Conferences currentConferences;
  private double travelWeight;
  private double sizeWeight;
  private Map<String, SchoolInfo> schoolInfoMap;
  private int conferenceSize = 10;
  private Distances distances;
  private Sizes sizes;

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, Map<String, SchoolInfo> schoolInfoMap) {
    this.travelWeight = travelWeight;
    this.sizeWeight = sizeWeight;
    this.schoolInfoMap = schoolInfoMap;
    this.distances = new Distances();
    this.sizes = new Sizes();
  }

  public SimulatedAnnealingProcess(double travelWeight, double sizeWeight, double schoolsSmall, double schoolsBig,
                                   Map<String, SchoolInfo> schoolInfoMap) {

    this(travelWeight, sizeWeight, schoolInfoMap);
    this.distances = new Distances();
    this.sizes = new Sizes();
  }

  public double getScore(Conferences conference) {
    double averageDistanceConf = 0;
    double averageSizeConf = 0;
    for (Map.Entry<String, List<String>> entry : conference.getConferences().entrySet()) {
      List<String> schools = entry.getValue();
      averageDistanceConf += getAverageDistances(new ArrayList<>(schools), 0);
      averageSizeConf += this.sizes.getAverageSize(schools, this.schoolInfoMap, this.conferenceSize);
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
      distance += this.distances.getDistance(s1, otherSchool, this.schoolInfoMap);
    }
    if (schools.size() == 2) {
      return distance;
    } else {
      distance += getAverageDistances(schools, distance);
    }
    return distance / conferenceSize;
  }


  public Conferences getNeighbour(Conferences conferences, Neighbouring neighbour) {
    List<String> allSchools = conferences.getAllSchools();
    Map<String, List<String>> newConferences = neighbour.getNewConferences(allSchools, this.distances);
    conferences.clearConferences();
    conferences.setConferences(newConferences);

    return conferences;
  }

  public Conferences simulatedAnnealing() {

    Conferences conferences = new Conferences(this.conferenceSize);
    conferences.initConferences(this.schoolInfoMap);
    Conferences bestConferences = new Conferences(conferences);
    this.currentConferences = new Conferences(conferences);

//    Neighbouring neighbour = new DistanceAndSizeBasedNeighbouring(40, .8, .05, 7, this.schoolInfoMap, this.conferenceSize, this.sizes)
//            .startAlternationWithRadiusIncrease(false)
//            .setMaxDistance(75);
    Neighbouring neighbour = new DistanceBasedNeighbouring(this.schoolInfoMap, this.conferenceSize);

    double coolingRate = 0.002;
//    double temp = 10000;
    double temp = 100000;
    Conferences newConferences;
    while (temp > 1) {

      //Get Neighbour
      newConferences = getNeighbour(new Conferences(this.currentConferences), neighbour);

      double currentEnergy = getScore(this.currentConferences);
      double neighbourEnergy = getScore(newConferences);

      if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
        this.currentConferences = new Conferences(newConferences);
      }

      if (neighbourEnergy < getScore(bestConferences)) {
        bestConferences = new Conferences(newConferences);
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