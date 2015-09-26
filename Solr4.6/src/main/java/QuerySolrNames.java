import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used to query Solr Names
 * java -cp Solr46.jar QuerySolrNames -solrCollection http://cluster:8983/solr/collection1,http://cluster:7574/solr/collection1,
 * http://cluster:8984/solr/collection1 -querySize 1000
 */
public class QuerySolrNames {

  public static void main(String[] args) throws ParseException, IOException {
    Option solrConnectionOpt = Option.builder("solrConnection")
            .required(true)
            .longOpt("solrConnection")
            .hasArg()
            .build();

    Option querySizeOpt = Option.builder("querySize")
            .required(false)
            .longOpt("querySize")
            .hasArg()
            .build();

    Option newNameRatioOpt = Option.builder("newNameRatio")
            .required(false)
            .longOpt("newNameRatio")
            .hasArg()
            .build();

    Options options = new Options();
    options.addOption(solrConnectionOpt);
    options.addOption(querySizeOpt);
    options.addOption(newNameRatioOpt);

    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = parser.parse(options, args);
    String solrConnection = cmd.getOptionValue("solrConnection");
    int querySize = Integer.parseInt(cmd.getOptionValue("querySize", "100"));
    double newNameRatio = Double.parseDouble(cmd.getOptionValue("newNameRatio", "0.8"));

    InputStream firstNameInputStream = InitalPopulateMongo.class.getClassLoader().getResourceAsStream("firstNames.txt");
    List<String> firstNames = new ArrayList<>();
    InputStream lastNameInputStream = InitalPopulateMongo.class.getClassLoader().getResourceAsStream("lastNames.txt");
    List<String> lastNames = new ArrayList<>();

    BufferedReader br = new BufferedReader(new InputStreamReader(firstNameInputStream));
    String line;
    while ((line = br.readLine()) != null) {
      firstNames.add(line);
    }

    br = new BufferedReader(new InputStreamReader(lastNameInputStream));
    while ((line = br.readLine()) != null) {
      lastNames.add(line);
    }
    System.out.println(solrConnection);

    LBHttpSolrServer lbHttpSolrServer = new LBHttpSolrServer(solrConnection.split(","));
//http://localhost:8983/solr/collection1/select?shards=localhost:8983/solr/collection1,localhost:7574/solr/collection1&indent=true&q=*:*&wt=json&rows=10&debug=true
//http://localhost:8983/solr/collection1/select?shards=localhost:8983/solr/collection1,localhost:7574/solr/collection1,localhost:8984/solr/collection1&indent=true&q=*:*&wt=json&rows=10&debug=true
    String solrNodes = solrConnection.replace("http://", "");
    Random random = new Random();
    int count = 0;
    List<String> names = new ArrayList<>();
    for (String fName : firstNames) {
      for (String lName : lastNames) {
        names.add("\"" + fName + " " + lName + "\"");
        names.add("\"" + fName.substring(0, fName.length() - 2) + " " + lName + "\"");
        if (random.nextFloat() > newNameRatio) {
          names.add("\"" + lName + " " + fName + "\"");
          count++;
        }
        count += 2;
        if (count >= querySize) {
          try {
            SolrQuery query = new SolrQuery("person_name_txt: " + StringUtils.join(names, " OR "));
            query.add("shards", solrNodes);
            QueryResponse qr = lbHttpSolrServer.query(query, SolrRequest.METHOD.POST);
            lbHttpSolrServer.commit(false, false);
            System.out.println("Response Time: " + qr.getElapsedTime());
            names.clear();
            count = 0;
          } catch (SolrServerException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

}
