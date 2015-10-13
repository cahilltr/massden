import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Thread for querying Solr
 */
public class QuerySolrThread extends Thread implements Runnable {

  private final String solrConnection;
  private final List<String> firstNames;
  private final List<String> lastNames;
  private final double newNameRatio;
  private final int querySize;

  public QuerySolrThread(String solrConnection, List<String> firstNames, List<String> lastNames,
                         double newNameRatio, int querySize) {
    this.solrConnection = solrConnection;
    this.firstNames = firstNames;
    this.lastNames = lastNames;
    this.newNameRatio = newNameRatio;
    this.querySize = querySize;
  }

  @Override
  public void run() {
    LBHttpSolrServer lbHttpSolrServer = null;
    try {
      lbHttpSolrServer = new LBHttpSolrServer(solrConnection.split(","));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

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
            query.add("shards", this.solrConnection);
            query.add("timeAllowed", "30");
            assert lbHttpSolrServer != null;
            QueryResponse qr = lbHttpSolrServer.query(query, SolrRequest.METHOD.POST);
            lbHttpSolrServer.commit();
            System.out.println("Response Time: " + qr.getElapsedTime());
            names.clear();
            count = 0;
          } catch (SolrServerException | IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }



}
