package Solr.query;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Note that if the query contains '\', it must be doubled, i.e. '\\'.
 * For use with Solr 4x and below as CloudSolrServer is deprecated
 */

public class SolrQueryCloudSolrServer {
  private final static Logger logger = LoggerFactory.getLogger(SolrQueryCloudSolrServer.class);

  public static void main(String[] args) {
    Option zkConnectOpt = OptionBuilder
            .withArgName("zkconnect")
            .hasArg()
            .withDescription("ZooKeeper connect string for Solr.")
            .isRequired()
            .create("zkconnect");
    Option queryOpt = OptionBuilder
            .withArgName("query")
            .hasArg()
            .withDescription("Solr query to use")
            .isRequired()
            .create("query");
    Option collectionOpt = OptionBuilder
            .withArgName("collection")
            .hasArg()
            .withDescription("Solr collection to query")
            .isRequired()
            .create("collection");
    Option fileOpt = OptionBuilder
            .withArgName("file")
            .hasArg()
            .withDescription("Path to output file")
            .isRequired()
            .create("file");
    Option rowOpt = OptionBuilder
            .withArgName("rowsToHandle")
            .hasArg()
            .withDescription("Rows to Handle Per Cursor Mark")
            .create("rowsToHandle");
    Option sortFieldOpt = OptionBuilder
            .withArgName("sortField")
            .hasArg()
            .withDescription("Field to Sort On")
            .create("sortField");
    Option sortDirectionOpt = OptionBuilder
            .withArgName("sortDirection")
            .hasArg()
            .withDescription("Sort Direction - asc or desc")
            .create("sortDirection");
    Option returnFieldsOpt = OptionBuilder
            .withArgName("returnFields")
            .hasArg()
            .isRequired()
            .withDescription("Comma separated fields to return")
            .create("returnFields");

    Options options = new Options();
    options.addOption(zkConnectOpt);
    options.addOption(queryOpt);
    options.addOption(collectionOpt);
    options.addOption(fileOpt);
    options.addOption(rowOpt);
    options.addOption(sortFieldOpt);
    options.addOption(sortDirectionOpt);
    options.addOption(returnFieldsOpt);

    CommandLineParser parser = new BasicParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      String zkconnect = cmd.getOptionValue("zkconnect", "localhost:2181/solr");
      String query = cmd.getOptionValue("query");
      String collection = cmd.getOptionValue("collection");
      String file = cmd.getOptionValue("file");
      int rowsToHandle = Integer.parseInt(cmd.getOptionValue("rowsToHandle", "1000"));
      String sortField = cmd.getOptionValue("sortField");
      String sortDirection = cmd.getOptionValue("sortDirection").trim();
      if (!sortDirection.isEmpty()) {
        if (!sortDirection.toLowerCase().equals("desc") && !sortDirection.toLowerCase().equals("asc")) {
          logger.error("sortDirection must be either desc or asc but was " + sortDirection);
          System.exit(3);
        }
      }
      String returnFieldsComma = cmd.getOptionValue("returnFields");
      if (returnFieldsComma.isEmpty()) {
        logger.error("returnFields cannot be empty. Must have 1 field or multiple comma separated fields.");
        System.exit(4);
      }
      String[] fields = returnFieldsComma.split(",");

      File f = new File(file);
      f.createNewFile();

      CloudSolrServer cloudSolrServer = new CloudSolrServer(zkconnect);
      cloudSolrServer.setDefaultCollection(collection);
      cloudSolrServer.connect();

      SolrQuery q = new SolrQuery();
      q.setQuery(query);
      List<SolrQuery.SortClause> sorts = new ArrayList <>();
      if (!sortField.isEmpty() && !sortDirection.isEmpty()) {
        if (sortDirection.toLowerCase().equals("desc")) {
          SolrQuery.SortClause sortClause = new SolrQuery.SortClause(sortField, SolrQuery.ORDER.desc);
          sorts.add(sortClause);
        } else {
          SolrQuery.SortClause sortClause = new SolrQuery.SortClause(sortField, SolrQuery.ORDER.desc);
          sorts.add(sortClause);
        }
      }
      sorts.add(SolrQuery.SortClause.asc("id"));
      q.setSorts(sorts);
      q.setRows(rowsToHandle);

      boolean done = false;
      String cursorMark = CursorMarkParams.CURSOR_MARK_START;
      while (!done) {
        try {
          q.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
          QueryResponse rsp = cloudSolrServer.query(q);
          String nextCursorMark = rsp.getNextCursorMark();
          StringBuilder builder = new StringBuilder();
          List<String> fieldValues = new ArrayList<>();
          for (SolrDocument doc : rsp.getResults()) {
            for (String field : fields) {
              if (doc.containsKey(field)) {
                fieldValues.add(doc.getFieldValue(field).toString());
              } else {
                fieldValues.add("");
              }
            }
            builder.append(StringUtils.join(",", fieldValues));
            builder.append(System.lineSeparator());
            fieldValues.clear();
          }
          FileUtils.writeStringToFile(f, builder.toString(), true);
          if (cursorMark.equals(nextCursorMark)) {
            done = true;
          }
          cursorMark = nextCursorMark;
          logger.info("CursorMark: " + cursorMark);
        } catch (SolrServerException e) {
          logger.error("SolrServerException", e);
        } catch (IOException e) {
          logger.error("IOException", e);
        }
      }
      cloudSolrServer.shutdown();
    } catch (ParseException e) {
      logger.error("ParseException", e);
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp("main", options);
      System.exit(1);
    } catch (IOException e) {
      logger.error("IOException", e);
      System.exit(2);
    }
  }
}
