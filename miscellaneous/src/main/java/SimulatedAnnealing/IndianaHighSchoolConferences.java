package SimulatedAnnealing;

import SimulatedAnnealing.utils.LoadSchoolInfo;
import SimulatedAnnealing.utils.SchoolInfo;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by cahillt on 7/20/15.
 */
public class IndianaHighSchoolConferences {


  public static void main(String[] args) throws Exception {

    Map<String, SchoolInfo> schoolInfo = null;
    if (args.length == 3) {
      String schoolSports = args[0];
      String schoolAddress = args[1];
      String schoolInfoFileWrite = args[2];

      File f = new File(schoolInfoFileWrite);
      f.createNewFile();

      Map<String, SchoolInfo> schoolAndAddresses = LoadSchoolInfo.getSchoolAddresses(schoolAddress, schoolSports);

      for (Map.Entry<String, SchoolInfo> entry : schoolAndAddresses.entrySet()) {
        System.out.println(entry.getValue().toString());
        FileUtils.writeStringToFile(f, entry.getValue().toString() + System.lineSeparator(), true);
      }
    } else if (args.length == 1) {
      String schoolInfoPath = args[0];
      schoolInfo = LoadSchoolInfo.loadMap(schoolInfoPath);
      for (Map.Entry<String, SchoolInfo> entry : schoolInfo.entrySet()) {
        System.out.println(entry.getKey() + ":" + entry.getValue().toString());
      }
    } else {
      throw new Exception("Takes either 1 or 3 arguments");
    }

    //TODO Simulated Annealing
    SimulatedAnnealingProcess simulatedAnnealingProcess = new SimulatedAnnealingProcess(.5, .5, schoolInfo);
    Map<String, List<String>> results = simulatedAnnealingProcess.simulatedAnnealing();
    for (Map.Entry<String, List<String>> result : results.entrySet()) {
      System.out.println("Conferenece Name: " + result.getKey());
      List<String> schools = result.getValue();
      for (String school : schools) {
        System.out.println("  " + school);
      }
    }
  }


}
