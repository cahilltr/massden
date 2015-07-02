package Solr.indexing;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;

/**
 * Created by cahillt on 6/30/15.
 */
public class WhitespaceVsStandardTokenizerIndexing {

  //Note: Embedded zookeeper port for solr is 9983

  //Fields that need to be defined
  //<field name="whitespace" type="text_test" indexed="true" stored="true" />
  //<field name="standard" type="text_general" indexed="true" stored="true" />

  //Field type text_test
//  <fieldType name="text_test" class="solr.TextField" positionIncrementGap="100">
  //  <analyzer type="index">
    //  <tokenizer class="solr.WhitespaceTokenizerFactory"/>
    //  <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    //  <filter class="solr.LowerCaseFilterFactory" />
  //  </analyzer>
  //  <analyzer type="query">
    //  <tokenizer class="solr.WhitespaceTokenizerFactory"/>
    //  <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    //  <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true" />
    //  <filter class="solr.LowerCaseFilterFactory" />
  //  </analyzer>
//  </fieldType>

  //Field Type text_general
//  <fieldType name="text_general" class="solr.TextField" positionIncrementGap="100">
  //  <analyzer type="index">
    //  <tokenizer class="solr.StandardTokenizerFactory"/>
    //  <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    //  <filter class="solr.LowerCaseFilterFactory"/>
  //  </analyzer>
  //  <analyzer type="query">
    //  <tokenizer class="solr.StandardTokenizerFactory"/>
    //  <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" />
    //  <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/>
    //  <filter class="solr.LowerCaseFilterFactory"/>
  //  </analyzer>
//  </fieldType>

  public static void main(String[] args) throws IOException, SolrServerException {


    CloudSolrClient cloudSolrClient = new CloudSolrClient(args[0]);
    cloudSolrClient.setDefaultCollection(args[1]);
    cloudSolrClient.connect();

    SolrInputDocument solrInputDocument = new SolrInputDocument();
    String id = Long.toString(System.currentTimeMillis());
    solrInputDocument.addField("id", id);

    String testString = "ZB*2227*2Z4";
    solrInputDocument.addField("whitespace", testString);
    solrInputDocument.addField("standard", testString);

    SolrInputDocument solrInputDocument2 = new SolrInputDocument();
    String id2 = Long.toString(System.currentTimeMillis());
    solrInputDocument2.addField("id", id2);


    String testString2 = "Hello, this! @ [at] <sat> {here}";
    solrInputDocument2.addField("whitespace", testString2);
    solrInputDocument2.addField("standard", testString2);

    cloudSolrClient.add(solrInputDocument);
    cloudSolrClient.add(solrInputDocument2);
    cloudSolrClient.commit();
  }

}
