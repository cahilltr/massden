package com.avalon.coe.Neighbours;

import com.avalon.coe.utils.Distances;

import java.util.List;
import java.util.Map;

/**
 * Created by cahillt on 7/28/15.
 */
public interface Neighbouring {

  Map<String, List<String>> getNewConferences(List<String> schools, Distances distances);
}
