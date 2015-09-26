
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create an inital populate of MongoDB
 */
public class InitalPopulateMongo {

  public static void main(String[] args) throws ParseException, URISyntaxException, IOException {
    Option mongoHostOpt = Option.builder("mongoHost")
            .required(true)
            .longOpt("mongoHost")
            .hasArg()
            .build();

    Option mongoPortOpt = Option.builder("mongoPort")
            .required(true)
            .longOpt("mongoPort")
            .hasArg()
            .build();

    Option mongoDBOpt = Option.builder("mongoDB")
            .required(true)
            .longOpt("mongoDB")
            .hasArg()
            .build();

    Option mongoCollectionOpt = Option.builder("mongoCollection")
            .required(true)
            .longOpt("mongoCollection")
            .hasArg()
            .build();

    Option solrConnectionOpt = Option.builder("solrConnection")
            .required(false)
            .longOpt("solrConnection")
            .hasArg()
            .build();

    Option solrCollectionOpt = Option.builder("solrCollection")
            .required(false)
            .longOpt("solrCollection")
            .hasArg()
            .build();

    Options options = new Options();
    options.addOption(mongoHostOpt);
    options.addOption(mongoPortOpt);
    options.addOption(solrConnectionOpt);
    options.addOption(solrCollectionOpt);
    options.addOption(mongoDBOpt);
    options.addOption(mongoCollectionOpt);

    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = parser.parse(options, args);
    String mongoHost = cmd.getOptionValue("mongoHost");
    int mongoPort = Integer.parseInt(cmd.getOptionValue("mongoPort"));
    String mongoDB = cmd.getOptionValue("mongoDB");
    String mongoCollection = cmd.getOptionValue("mongoCollection");
    String solrConnection = cmd.getOptionValue("solrConnection");
    String solrCollection = cmd.getOptionValue("solrCollection");

    File firstNamesFile = new File(InitalPopulateMongo.class.getClassLoader().getResource("firstNames.txt").toURI());
    List<String> firstNames = new ArrayList<>();
    File lastNamesFile = new File(InitalPopulateMongo.class.getClassLoader().getResource("lastNames.txt").toURI());
    List<String> lastNames = new ArrayList<>();

    BufferedReader br = new BufferedReader(new FileReader(firstNamesFile));
    String line;
    while ((line = br.readLine()) != null) {
      firstNames.add(line);
    }

    br = new BufferedReader(new FileReader(lastNamesFile));
    while ((line = br.readLine()) != null) {
      lastNames.add(line);
    }


//    MongoClient mongoClient = new MongoClient(mongoHost, mongoPort);
//
//    MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoDB);
//
//    if (mongoDatabase.getCollection(mongoCollection) != null) {
//      System.out.println(mongoCollection + " is already created. Exiting.");
//      System.exit(10);
//    }
//
//    mongoDatabase.createCollection(mongoCollection);
//
//    MongoCollection mongoTable = mongoDatabase.getCollection(mongoCollection);

    for (String fName : firstNames) {
      for (int i = 0; i < lastNames.size() - 100; i++) {
        //TODO create names and add to mongo and Solr
        //Use created/updated field for a status to know if an update or insert is needed.



      }
    }



  }
}
