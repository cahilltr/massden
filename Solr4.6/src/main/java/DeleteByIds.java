import org.apache.commons.cli.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Delete By Test
 */
public class DeleteByIds {

  public static void main(String[] args) throws ParseException, IOException {
    Option solrConnectionOpt = Option.builder("solrConnection")
            .required(true)
            .longOpt("solrConnection")
            .hasArg()
            .build();

    Options options = new Options();
    options.addOption(solrConnectionOpt);

    CommandLineParser parser = new DefaultParser();

    CommandLine cmd = parser.parse(options, args);
    String solrConnection = cmd.getOptionValue("solrConnection");

    System.out.println(solrConnection);

    ConcurrentUpdateSolrServer concurrentUpdateSolrServer = new ConcurrentUpdateSolrServer(solrConnection, 10, 1);

    HttpSolrServer httpSolrServer = new HttpSolrServer(solrConnection);

    SolrQuery q = new SolrQuery("*:*");
    q.set("rows", 10);

    try {
      QueryResponse queryResponse = httpSolrServer.query(q);
      for (SolrDocument solrDocument : queryResponse.getResults()) {
        String id = (String) solrDocument.getFieldValue("id");
//        UpdateResponse updateResponse = concurrentUpdateSolrServer.deleteById(id, 30000);
        UpdateResponse updateResponse = httpSolrServer.deleteById(id, 30000);
        System.out.println(updateResponse.getElapsedTime());

      }
    } catch (SolrServerException e) {
      e.printStackTrace();
    }
  }
}
