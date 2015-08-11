package com.avalon.test;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class ScanMapper extends TableMapper<Text, Text> {
  private final static Logger logger = LoggerFactory.getLogger(ScanMapper.class);

  private final String SOLR_ID_FIELD = "id";
  private final byte[] COLUMN_FAMILY = Bytes.toBytes("metadata");
  private Configuration configuration;
  private String zkConnectString;
  private String hbaseTablename;
  private String solrCollection;
  private int numRows;
  private int commitWithin;
  private boolean reindexAll;
  private List<String> ids = new ArrayList<>();

  public void setup(Context context) {
    logger.info("Setup Starting");
    this.configuration = context.getConfiguration();
    this.zkConnectString = configuration.get("zkConnectString");
    this.hbaseTablename = configuration.get("hbaseTablename");
    this.solrCollection = configuration.get("solrCollection");
    this.numRows = Integer.parseInt(configuration.get("numRows"));
    this.commitWithin = Integer.parseInt(configuration.get("commitWithin"));
    this.reindexAll = Boolean.parseBoolean(configuration.get("reindexAll"));
    logger.info("Setup Complete");
  }

  public void map(ImmutableBytesWritable row, Result value, Context context) throws InterruptedException, IOException {
    // process data for the row from the Result instance.

    ids.add(Bytes.toString(value.getRow()));
    logger.info(Bytes.toString(value.getRow()));
    if (ids.size() > 500) {
      try {
        logger.info("processing IDS");
        processIDs(ids);
        ids.clear();
        logger.info("processed IDS");
      } catch (SolrServerException e) {
        logger.error("SolrServerException", e);
      }
    }

  }

  public void cleanup(org.apache.hadoop.mapreduce.Mapper.Context context) {
    try {
      if (ids.size() > 0){
        processIDs(ids);
      }
    } catch (IOException e) {
      logger.error("IOException", e);
    } catch (SolrServerException e) {
      logger.error("SolrServerException", e);
    } catch (InterruptedException e) {
      logger.error("InterruptedException", e);
    }
  }

  /**
   * Takes a list of IDs that are in HBase and then inserts the missing ones into Solr
   //   * @param cloudSolrServer CloudSolrServer to query
   //   * @param table HBase table to query
   * @param ids List of IDs that are in HBase
   * @throws IOException
   * @throws SolrServerException
   */
  public void processIDs(List<String> ids) throws IOException, SolrServerException, InterruptedException {
    CloudSolrServer cloudSolrServer = null;
    try {
      cloudSolrServer = new CloudSolrServer(this.zkConnectString);
      cloudSolrServer.setDefaultCollection(this.solrCollection);

      logger.debug("Processing rows");
      logger.debug("IDs found in HBase: " + ids);

      // For reindexing all of HBase into Solr there is no reason to even check if it exists in Solr
      List<String> missingIds;
      if (this.reindexAll) {
        missingIds = ids;
      } else {
        missingIds = checkRows(cloudSolrServer, ids);
      }

      if (!missingIds.isEmpty()) {
        logger.info("Creating Solr docs for IDs: " + ids);
        List<SolrInputDocument> solrInputDocuments = createSolrInputDocuments(missingIds);
        cloudSolrServer.add(solrInputDocuments, this.commitWithin);
        cloudSolrServer.commit();
      }
    }finally {
      if(cloudSolrServer != null) {
        cloudSolrServer.shutdown();
      }
    }
  }

  /**
   * Takes a list of IDs and checks if they are in Solr and returns a list of IDs that were not in Solr
   * @param cloudSolrServer CloudSolrServer to query
   * @param ids List of IDs to check in Solr
   * @return List of IDs that were not in Solr
   * @throws org.apache.solr.client.solrj.SolrServerException
   */
  protected List<String> checkRows(CloudSolrServer cloudSolrServer, List<String> ids) throws SolrServerException {
    SolrQuery query = new SolrQuery();
    query.setFields(this.SOLR_ID_FIELD);

    query.setQuery(getQueryString(ids));
    query.setStart(0);
    query.setRows(this.numRows);

    QueryResponse queryResponse = cloudSolrServer.query(query, SolrRequest.METHOD.POST);
    SolrDocumentList results = queryResponse.getResults();

    List<String> solrIDs = new ArrayList<>();
    for(SolrDocument solrDocument : results) {
      solrIDs.add(String.valueOf(solrDocument.getFieldValue(this.SOLR_ID_FIELD)));
    }

    ids.removeAll(solrIDs);
    return ids;
  }

  /**
   * Takes a list of IDs and gets the resulting data from HBase to convert into SolrInputDocuments
   //   * @param table The table that needs to be queried in HBase
   * @param ids List of IDs that need to be retrieved from HBase
   * @return List of SolrInputDocuments to insert into Solr
   * @throws java.io.IOException
   */
  protected List<SolrInputDocument> createSolrInputDocuments(List<String> ids) throws IOException, InterruptedException {
    try(HTable table = new HTable(this.configuration, this.hbaseTablename)) {
      List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
      for (String id : ids) {
        logger.debug("ID: " + id);
        Get get = new Get(Bytes.toBytes(id));
        get.addFamily(this.COLUMN_FAMILY);
        get.setMaxVersions(1);
        Result result = table.get(get);
        SolrInputDocument solrInputDocument = convertHBaseResultToSolrInputDocument(result);
        solrInputDocuments.add(solrInputDocument);
      }
      return solrInputDocuments;
    } catch (IOException e) {
      if (e instanceof NotServingRegionException) {
        logger.error("IOException", e);
        Thread.sleep(1000 * 1);
        return createSolrInputDocuments(ids);
      } else {
        throw e;
      }
    }
  }

  /**
   * Converts an HBase result row into a SolrInputDocument by iterating through the qualifiers and values
   * @param result HBase result row
   * @return new SolrInputDocument with the HBase result qualifier/values mapped to Solr fields/values
   */
  protected SolrInputDocument convertHBaseResultToSolrInputDocument(Result result) {
    SolrInputDocument solrInputDocument = new SolrInputDocument();

    logger.debug(SOLR_ID_FIELD + ": " + Bytes.toString(result.getRow()));
    solrInputDocument.addField(SOLR_ID_FIELD, Bytes.toString(result.getRow()));

    NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(this.COLUMN_FAMILY);
    for(Map.Entry<byte[], byte[]> innerEntry : familyMap.entrySet()) {
      logger.debug("Qualifier: " + Bytes.toString(innerEntry.getKey()));
      logger.debug("Value: " + Bytes.toString(innerEntry.getValue()));
      addFieldToSolrInputDocument(solrInputDocument, innerEntry.getKey(), innerEntry.getValue());
    }
    logger.debug("SolrInputDocument for ID: " + Bytes.toString(result.getRow()));
    logger.debug(solrInputDocument.toString());
    return solrInputDocument;
  }

  /**
   * Takes a qualifier and value from HBase and adds it to a SolrInputDocument
   * There are 3 cases which need to be addressed:
   *         Qualifier     | Value |  Solr Field  | Solr Value
   *  1)        a_b        |   c   |     a        |     b
   *  2) "attachment-*_a"  |   b   | attachment-* |     b
   *  3)         a         |   b   |     a        |     b
   * @param solrInputDocument SolrInputDocument to add field to
   * @param qualifierBytes qualifier in bytes from HBase row
   * @param valueBytes value in bytes from HBase row
   */
  protected void addFieldToSolrInputDocument(SolrInputDocument solrInputDocument, byte[] qualifierBytes, byte[] valueBytes) {
    String qualifier = Bytes.toString(qualifierBytes);

    String solrField;
    String solrFieldValue;

//    String[] split = qualifier.split("_", 2);
//    if(split.length == 2) {
//      solrField = split[0];
//      if(solrField.startsWith("attachment")) {
        solrFieldValue = Bytes.toString(valueBytes);
//      } else {
//        solrFieldValue = split[1];
//      }
//    } else {
      solrField = qualifier;
//      solrFieldValue = Bytes.toString(valueBytes);
//    }

    logger.debug("Adding to doc:");
    logger.debug("Field: " + solrField);
    logger.debug("Value: " + solrFieldValue);
    solrInputDocument.addField(solrField, solrFieldValue);
  }

  /**
   * Takes a list of IDs and returns the Solr query string for those IDs
   * @param ids List of IDs that need to be queried
   * @return Query string of all the IDs OR'd together
   */
  protected String getQueryString(List<String> ids) {
    if(ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException("IDs cannot be null or empty");
    }
    List<String> cleanedIDs = new ArrayList<>();
    for (String id : ids) {

      cleanedIDs.add("\"" + id + "\"");
    }
    String queryString = SOLR_ID_FIELD + ":( " + StringUtils.join(cleanedIDs, " OR ") + " )";

    logger.debug("Query String: " + queryString);
    return queryString;
  }
}
