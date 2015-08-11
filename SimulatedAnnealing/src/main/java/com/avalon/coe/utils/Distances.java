package com.avalon.coe.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cahillt on 7/28/15.
 * Used for Handling distances and handling a map of distances.
 */
public class Distances {

  private Map<String, Double> schoolDistances = new HashMap<>();


  public double getDistance(String s1, String otherSchool, Map<String, SchoolInfo> schoolInfoMap) {
    if (this.schoolDistances.containsKey(otherSchool + s1)) {
      return schoolDistances.get(otherSchool + s1);
    } else if (schoolDistances.containsKey(s1 + otherSchool)) {
      return schoolDistances.get(s1 + otherSchool);
    } else {
      double d = getDistanceSchools(otherSchool, s1, schoolInfoMap);
      schoolDistances.put(otherSchool + s1, d);
      return d;
    }
  }

  private double getDistanceSchools(String school1, String school2, Map<String, SchoolInfo> schoolInfoMap) {
    SchoolInfo s1 = schoolInfoMap.get(school1);
    SchoolInfo s2 = schoolInfoMap.get(school2);
    return getDistanceLatLong(s1.getLat(), s1.getLong(), s2.getLat(), s2.getLong());
  }

  private double getDistanceLatLong(double lat1, double lon1, double lat2, double lon2) {
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
}
