package Solr.query;

import org.apache.commons.cli.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to test Boolean query limitiation
 * Note that the Documents should Already be
 * in Solr prior to querying.
 */
public class SolrBooleanMaxClauseWorkAround {
  private final static Logger logger = LoggerFactory.getLogger(SolrBooleanMaxClauseWorkAround.class);

  public static void main(String[] args) throws IOException, SolrServerException, ParseException {

    Option connectionOpt = Option.builder("SolrConnection")
            .required(true)
            .longOpt("SolrConnection")
            .hasArg()
            .build();

    Option collectionOpt = Option.builder("SolrCollection")
            .required(true)
            .longOpt("SolrCollection")
            .hasArg()
            .build();

    Option numDocsOpt = Option.builder("NumDocs")
            .required(false)
            .longOpt("NumDocs")
            .hasArg()
            .build();


    Options options = new Options();
    options.addOption(connectionOpt);
    options.addOption(collectionOpt);
    options.addOption(numDocsOpt);

    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = parser.parse(options, args);
    String solrConnect = cmd.getOptionValue("SolrConnection");
    logger.info("SolrConnection: " + solrConnect);
    String solrCollection = cmd.getOptionValue("SolrCollection");
    logger.info("SolrCollection: " + solrCollection);
    int numDocs = Integer.parseInt(cmd.getOptionValue("NumDocs", "1000"));
    logger.info("NumDocs: " + numDocs);

    CloudSolrServer cloudSolrServer = new CloudSolrServer(solrConnect);
    cloudSolrServer.setDefaultCollection(solrCollection);

    List<String> ids = new ArrayList<>(numDocs);
    for (int i = 0; i < numDocs; i++) {
      ids.add("" + i);
      System.out.println("" + i);
    }

    int i = 1;
    String query = "id:(";
    for (String id : ids) {
      if (i == 1024) {
        query += id + ") AND id:(";
        i = 1;
      } else {
        query += " " + id + " ";
        i++;
      }
    }

    if (query.endsWith("(")) {
      query = query.substring(0, query.length() - 9);
    } else {
      query += ")";
    }

    System.out.println("Query: " + query);

    SolrQuery query1 = new SolrQuery(query);
    QueryResponse response = cloudSolrServer.query(query1, SolrRequest.METHOD.POST);
    System.out.println("Response Time: " + response.getElapsedTime());
    cloudSolrServer.close();
  }
}
