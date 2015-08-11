package com.avalon.coe.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cahillt on 7/24/15.
 */
public class Conferences {

  private Map<String, List<String>> conferences;
  private int conferenceSize = 10;

  public Conferences(int conferenceSize) {
    this.conferences = new HashMap<>();
    this.conferenceSize = conferenceSize;
  }

  public Conferences(Map<String, List<String>> someConferences) {
    this.conferences = someConferences;
  }

  public Conferences(Conferences conferences) {
    this.conferences = conferences.getConferences();
  }

  public int size() {
    return this.conferences.size();
  }

  public List<String> getConferenceNames() {
    return new ArrayList<>(this.conferences.keySet());
  }

  public void setConferences(Map<String, List<String>> newConferences) {
    this.conferences.putAll(newConferences);
  }

  public void removeConference(String conf) {
    this.conferences.remove(conf);
  }

  public List<String> getConference(String conf) {
    return this.conferences.get(conf);
  }

  public Map<String, List<String>> getConferences() {
    return this.conferences;
  }

  public void initConferences(Map<String, SchoolInfo> schoolInfoMap) {
    ArrayList<String> schools = new ArrayList<>(schoolInfoMap.keySet());
    Collections.shuffle(schools);

    int confNum = 0;
    int curSchoolCount = 0;
    for (String school : schools) {
      if (curSchoolCount == conferenceSize) {
        confNum++;
        this.conferences.put(confNum + "", new ArrayList<>());
        this.conferences.get(confNum + "").add(school);
        curSchoolCount = 0;
      } else {
        if (!this.conferences.containsKey(confNum + "")) {
          this.conferences.put(confNum + "", new ArrayList<>());
        }
        this.conferences.get(confNum + "").add(school);
      }
      curSchoolCount++;
    }
  }

  public List<String> getAllSchools() {
    List<String> schools = new ArrayList<>();

    for (Map.Entry<String,List<String>> entry :this.conferences.entrySet()) {
      schools.addAll(entry.getValue().stream().collect(Collectors.toList()));
    }

    return schools;
  }

  public void clearConferences() {
    this.conferences.clear();
  }
}
