import org.apache.commons.lang3.RandomUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.*;

import java.io.IOException;
import java.util.*;

/**
 * Thread for creating data into Solr
 * Assume AutoCommit is enabled.
 */
public class PopulateThread extends Thread implements Runnable {

  private String solrConnect;
  private List<String> fNames;
  private List<String> lNames;

  private static final String[] addresses = new String[]{"Elm St", "River Rd", "Lithio Pl", "Rainey St", "White Ave"};

  public PopulateThread (String solrConnect, List<String> fNames, List<String> lNames) {
    this.solrConnect = solrConnect;
    this.fNames = fNames;
    this.lNames = lNames;
  }


  @Override
  public void run() {
    try {
      LBHttpSolrServer lbHttpSolrServer = new LBHttpSolrServer(this.solrConnect.split(","));
//      ConcurrentUpdateSolrServer concurrentUpdateSolrServer = new ConcurrentUpdateSolrServer(this.solrConnect.split(",")[0], 100, 1);
      Set<String> paramses = new HashSet<>();
      paramses.add(UpdateParams.OPEN_SEARCHER +"=false");
      paramses.add(UpdateParams.WAIT_SEARCHER +"=false");
      lbHttpSolrServer.setQueryParams(paramses);
      Random r = new Random();
      int count = 0;
      for (String fName : fNames) {
        for (String lName : lNames) {
          SolrInputDocument solrInputDocument = new SolrInputDocument();
          String id = fName + lName;
          solrInputDocument.addField("id", id);
          //person_name
          solrInputDocument.addField("person_name_txt", fName + " " + lName);
          if (r.nextDouble() > .8) { // 20% of the time a person will have 2 names
            solrInputDocument.addField("person_name_txt", fName.substring(0, fName.length() - 2) + " " + lName);
          }

          int ssn = (int) (10000000 + RandomUtils.nextFloat(0, 1) * 900000000);
          solrInputDocument.addField("person_ssn_ss", ssn);
          if (r.nextDouble() > .8) {
            solrInputDocument.addField("person_ssn_ss", ssn - 1000);
          }
          String address = Math.abs(r.nextInt()) + " " + addresses[r.nextInt(addresses.length - 1)];
          solrInputDocument.addField("address_txt", address);
          if (r.nextDouble() > .8) {
            String address2 = Math.abs(r.nextInt()) + " " + addresses[r.nextInt(addresses.length - 1)];
            solrInputDocument.addField("address_txt", address2);
          }

          try {
            lbHttpSolrServer.add(solrInputDocument, 60000);

            count++;
            if (count == 10000) {
              //assume
              System.out.println(id);
            }
          } catch (SolrServerException e) {
            e.printStackTrace();
          }
        }
        try {
          lbHttpSolrServer.commit();
        } catch (SolrServerException | IOException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
