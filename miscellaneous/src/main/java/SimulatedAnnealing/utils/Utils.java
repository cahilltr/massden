package SimulatedAnnealing.utils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by cahillt on 7/24/15.
 * Utils to clean up the actual simulated Annealing Process
 */
public class Utils {

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

}
