package com.avalon;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cahillt on 1/18/15.
 */
public class SolrTutorialLoad {

  private final static String article[] = { "the", "a", "one", "some", "any" };
  private final static String noun[] = { "boy", "girl", "dog", "town", "car" };
  private final static String verb[] = { "drove", "jumped", "ran", "walked", "skipped" };
  private final static String preposition[] = { "to", "from", "over", "under", "on" };

  private final static int NO_WORDS = 5;
  private final static String SPACE = " ";
  private final static String PERIOD = ".";

  static Random r = new Random();

  public static void main(String[] args) throws IOException, SolrServerException {
    String zkhost = "localhost:9983";

    CloudSolrServer cloudSolrServer = new CloudSolrServer(zkhost);
    cloudSolrServer.setDefaultCollection("collection1");
    cloudSolrServer.connect();
    loadData(cloudSolrServer);
  }

  private static void loadData(CloudSolrServer cloudSolrServer) throws IOException, SolrServerException {
    long timeadj = 24*60*60*1000;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date date = new Date(0);

    Random r = new Random();
    double rangeMin = 0;
    double rangeMax = 1000;
    int min = 0;
    int max = 1000;

    for (int i = 0; i < 2000; i++) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id", i);
      double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
      doc.addField("double_d", randomValue);
      doc.addField("string_s", generateSentence());
      doc.addField("date_dt", df.format(date));
      doc.addField("int_i", r.nextInt((max - min) + 1) + min);

      cloudSolrServer.add(doc);
      cloudSolrServer.commit();
      date = new Date(date.getTime() + timeadj);
    }

  }

  private static String generateSentence() {
    String sentence;

    sentence = article[rand()];
    char c = sentence.charAt(0);
    sentence = sentence.replace(c, Character.toUpperCase(c));
    sentence += SPACE + noun[rand()] + SPACE;
    sentence += (verb[rand()] + SPACE + preposition[rand()]);
    sentence += (SPACE + article[rand()] + SPACE + noun[rand()]);
    sentence += PERIOD;

    return sentence;
  }

  private static int rand(){
      int ri = r.nextInt() % NO_WORDS;
      if ( ri < 0 )
        ri += NO_WORDS;
      return ri;
    }
}
