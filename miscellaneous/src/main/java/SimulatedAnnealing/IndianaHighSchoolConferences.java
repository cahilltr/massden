package SimulatedAnnealing;

import SimulatedAnnealing.utils.Conferences;
import SimulatedAnnealing.utils.LoadSchoolInfo;
import SimulatedAnnealing.utils.SchoolInfo;
import SimulatedAnnealing.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by cahillt on 7/20/15.
 * Kicks off Simulated Annealing process
 */
public class IndianaHighSchoolConferences {


  public static void main(String[] args) throws Exception {

    Map<String, SchoolInfo> schoolInfo = null;
    if (args.length == 3) {
      String schoolSports = args[0];
      String schoolAddress = args[1];
      String schoolInfoFileWrite = args[2];

      File f = new File(schoolInfoFileWrite);
      if (!f.createNewFile()) {
        throw new Exception(schoolInfoFileWrite + " file could not be created");
      }

      Map<String, SchoolInfo> schoolAndAddresses = LoadSchoolInfo.getSchoolAddresses(schoolAddress, schoolSports);

      for (Map.Entry<String, SchoolInfo> entry : schoolAndAddresses.entrySet()) {
        System.out.println(entry.getValue().toString());
        FileUtils.writeStringToFile(f, entry.getValue().toString() + System.lineSeparator(), true);
      }
      schoolInfo = LoadSchoolInfo.loadMap(schoolInfoFileWrite);
    } else if (args.length == 1) {
      String schoolInfoPath = args[0];
      schoolInfo = LoadSchoolInfo.loadMap(schoolInfoPath);
      for (Map.Entry<String, SchoolInfo> entry : schoolInfo.entrySet()) {
        System.out.println(entry.getKey() + ":" + entry.getValue().toString());
      }
    } else {
      throw new Exception("Takes either 1 or 3 arguments");
    }

    DecimalFormat df = new DecimalFormat("#.##");
    SimulatedAnnealingProcess simulatedAnnealingProcess = new SimulatedAnnealingProcess(1, 0, schoolInfo);
    Conferences results = simulatedAnnealingProcess.simulatedAnnealing();
    for (Map.Entry<String, List<String>> result : results.getConferences().entrySet()) {
      System.out.println("Conferenece Name: " + result.getKey());
      List<String> schools = result.getValue();
      for (String school : schools) {
        StringBuilder s =
                new StringBuilder("  " + school + " - Size: " + Utils.getSchoolSize(school, schoolInfo) + "-->");
        for (String s2 : schools) {
          if (!school.equals(s2)){
            s.append(s2 + ":" + df.format(Utils.getDistance(school, s2, schoolInfo)) + ",");
          }
        }
        System.out.println(s.toString());
      }
    }
  }


}
