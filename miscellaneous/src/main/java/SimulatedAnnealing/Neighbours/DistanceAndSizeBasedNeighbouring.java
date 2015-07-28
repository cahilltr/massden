package SimulatedAnnealing.Neighbours;

import SimulatedAnnealing.utils.Distances;
import SimulatedAnnealing.utils.SchoolInfo;
import SimulatedAnnealing.utils.Sizes;
import SimulatedAnnealing.utils.Utils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;

/**
 * Created by cahillt on 7/28/15.
 * Class for getting a Neighbour in SA based off distance and School size
 */
public class DistanceAndSizeBasedNeighbouring implements Neighbouring {

  private double schoolSizeStandardDeviation = -1;
  private double schoolDefaultRadius = 50;
  private double radiusIncrease = 15;
  private double distanceFromSizeSDIncrease = .2;
  private double marginFromSD = .5;
  //if true, increases radius first; if false, increases distance from SD first
  boolean increaseRadius = false;

  private Sizes sizes;
  private int conferenceSize;
  private Map<String, SchoolInfo> schoolInfoMap;

  public DistanceAndSizeBasedNeighbouring(Map<String, SchoolInfo> schoolInfoMap,
                                          int conferenceSize, Sizes sizes) {
    this.schoolInfoMap = schoolInfoMap;
    this.conferenceSize = conferenceSize;
    this.sizes = sizes;
  }

  public DistanceAndSizeBasedNeighbouring
          (double distanceFromSizeSDIncrease, double radiusIncrease,
           Map<String, SchoolInfo> schoolInfoMap, int conferenceSize, Sizes sizes) {
    this.radiusIncrease = radiusIncrease;
    this.distanceFromSizeSDIncrease = distanceFromSizeSDIncrease;
    this.schoolInfoMap = schoolInfoMap;
    this.conferenceSize = conferenceSize;
    this.sizes = sizes;
  }

  public DistanceAndSizeBasedNeighbouring(double schoolDefaultRadius, double marginFromSD,
                                          double radiusIncrease, double distanceFromSizeSDIncrease,
                                          Map<String, SchoolInfo> schoolInfoMap, int conferenceSize, Sizes sizes) {
    this.schoolDefaultRadius = schoolDefaultRadius;
    this.marginFromSD = marginFromSD;
    this.radiusIncrease = radiusIncrease;
    this.distanceFromSizeSDIncrease = distanceFromSizeSDIncrease;
    this.schoolInfoMap = schoolInfoMap;
    this.conferenceSize = conferenceSize;
    this.sizes = sizes;
  }

  public void startAlternationWithRadiusIncrease(boolean increaseRadius) {
    this.increaseRadius = increaseRadius;
  }

  @Override
  public Map<String, List<String>> getNewConferences (List<String> schools, Distances distances) {
    Collections.shuffle(schools);

    if (schoolSizeStandardDeviation == -1) {
      SummaryStatistics summaryStatistics = new SummaryStatistics();
      for (String school : schools) {
        double size = this.sizes.getSchoolSize(school, this.schoolInfoMap);
        summaryStatistics.addValue(size);
        this.sizes.addSize(school, size);
      }
      schoolSizeStandardDeviation = summaryStatistics.getStandardDeviation();
    }

    int numSeedSchools = schools.size() / this.conferenceSize;
    List<String> seedSchools = new ArrayList<>(numSeedSchools);
    for (int i = 0; i < numSeedSchools; i++) {
      seedSchools.add(schools.get(i));
      schools.remove(i);
    }

    //TODO rewrite with size of school within .5 schoolSizeStandardDeviation
    //All schools within radius then expand radius then size

    Map<String, List<String>> newConferences = new HashMap<>();
    for (int j = 0; j < numSeedSchools - 1; j++) {
      String seedSchool = seedSchools.get(j);

      Map<String, Double> schoolsDistances = new HashMap<>();
      for (String school : schools) {
        Double d = distances.getDistance(school, seedSchool, this.schoolInfoMap);
        schoolsDistances.put(school, d);
      }

      double seedSchoolSize = this.sizes.getSchoolSize(seedSchool, this.schoolInfoMap);
      schoolsDistances = Utils.sortMapDouble(schoolsDistances);

      //sort sizes, closest to point
      Map<String, Double> schoolsSizes =
              Utils.sortClosestToPoint(this.sizes.getSchoolSize(seedSchool, this.schoolInfoMap), this.sizes.getAllCurrentSizes());

      List<String> conference = new ArrayList<>();
      int k = 0;

      double radius = this.schoolDefaultRadius;
      while (k != (this.conferenceSize - 1)) {
        //New distance from the Standard Deviation
        double sdMargin = (schoolSizeStandardDeviation * marginFromSD);

        List<String> added = new ArrayList<>();
        for (Map.Entry<String, Double> entry : schoolsDistances.entrySet()) {
          String key = entry.getKey();
          double schoolSize = schoolsSizes.get(key);
          if (entry.getValue() < radius && Math.abs(seedSchoolSize - schoolSize) <= sdMargin) {
            conference.add(key);
            added.add(key);
            k++;
            if (k == (this.conferenceSize - 1)) {
              break;
            }
          }
        }
        if (increaseRadius) {
          //increase radius
          radius += radiusIncrease;
          increaseRadius = false;
        } else {
          //increase SD margin distance
          this.marginFromSD += distanceFromSizeSDIncrease;
          increaseRadius = true;
        }
        //Remove schools from size and Distances lists here to prevent concurrent modification exception
        for (String add : added) {
          schoolsSizes.remove(add);
          schoolsDistances.remove(add);
        }

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
