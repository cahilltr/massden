package Solr.general;

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

  public static void main(String[] args) throws IOException, SolrServerException {


    CloudSolrServer cloudSolrServer = new CloudSolrServer(args[0]);
    cloudSolrServer.setDefaultCollection(args[1]);

    int totalDocs = 1000;
    for (int i = 0; i < totalDocs; i++) {
      SolrInputDocument document = new SolrInputDocument();
      document.addField("id", "" + i);
      document.addField("test_i", i);
      document.addField("test_s", "this is a string");
      document.addField("test_t", "this is some text");
      cloudSolrServer.add(document);
    }
    cloudSolrServer.commit();
  }
}
