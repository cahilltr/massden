import org.apache.commons.lang3.RandomUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Thread for creating data into Solr
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
          solrInputDocument.addField("address_ss", address);

          try {
            lbHttpSolrServer.add(solrInputDocument);

            count++;
            if (count == 10000) {
              System.out.println(id);
              lbHttpSolrServer.commit();
              count = 0;
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
