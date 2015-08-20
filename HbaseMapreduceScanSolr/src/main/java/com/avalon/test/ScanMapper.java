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


  protected void addFieldToSolrInputDocument(SolrInputDocument solrInputDocument, byte[] qualifierBytes, byte[] valueBytes) {
    String qualifier = Bytes.toString(qualifierBytes);

    String solrField;
    String solrFieldValue;

    solrFieldValue = Bytes.toString(valueBytes);
    solrField = qualifier;

    logger.debug("Adding to doc:");
    logger.debug("Field: " + solrField);
    logger.debug("Value: " + solrFieldValue);
    solrInputDocument.addField(solrField, solrFieldValue);
  }

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
