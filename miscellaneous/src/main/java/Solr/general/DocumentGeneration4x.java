package Solr.general;

import org.apache.commons.cli.*;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Create Documents for Solr Cloud 4.x
 * This is good for use to see affects on documents during
 * Admin type operations such as migrations of cores
 * and shards.
 */
public class DocumentGeneration4x {
  private final static Logger logger = LoggerFactory.getLogger(DocumentGeneration4x.class);

  public static void main(String[] args) throws IOException, SolrServerException, ParseException {
    
    Option connectionOpt = Option.builder("SolrConnection")
            .required(true)
            .longOpt("SolrConnection")
            .build();

    Option collectionOpt = Option.builder("SolrCollection")
            .required(true)
            .longOpt("SolrCollection")
            .build();

    Option numDocsOpt = Option.builder("NumDocs")
            .required(false)
            .longOpt("NumDocs")
            .build();


    Options options = new Options();
    options.addOption(connectionOpt);
    options.addOption(collectionOpt);

    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = parser.parse(options, args);
    String solrConnect = cmd.getOptionValue("SolrConnection");
    String solrCollection = cmd.getOptionValue("SolrCollection");
    int numDocs = Integer.parseInt(cmd.getOptionValue("numDocsOpt", "1000"));

    CloudSolrServer cloudSolrServer = new CloudSolrServer(solrConnect);
    cloudSolrServer.setDefaultCollection(solrCollection);

    for (int i = 0; i < numDocs; i++) {
      SolrInputDocument document = new SolrInputDocument();
      document.addField("id", "" + i + i);
      document.addField("test_i", i);
      document.addField("test_s", "this is a string");
      document.addField("test_t", "this is some text");
      cloudSolrServer.add(document);
    }
    cloudSolrServer.commit();
  }
}
