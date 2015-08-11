package com.avalon.test;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

public class ScanMapReduce {
  private final static Logger logger = LoggerFactory.getLogger(ScanMapReduce.class);

  public static void main(String[] args) throws Exception {

    Option solrZKOpt = OptionBuilder
            .withArgName("solrZK")
            .hasArg()
            .isRequired()
            .withDescription("")
            .create("solrZK");
    Option hbaseTableNameOpt = OptionBuilder
            .withArgName("hbaseTableName")
            .hasArg()
            .isRequired()
            .withDescription("")
            .create("hbaseTableName");
    Option solrCollectionOpt = OptionBuilder
            .withArgName("solrCollection")
            .hasArg()
            .isRequired()
            .withDescription("")
            .create("solrCollection");
    Option commitWithinOpt = OptionBuilder
            .withArgName("commitWithin")
            .hasArg()
            .withDescription("")
            .create("commitWithin");
    Option numRowsOpt = OptionBuilder
            .withArgName("numRows")
            .hasArg()
            .withDescription("")
            .create("numRows");
    Option reindexAllOpt = OptionBuilder
            .withArgName("reindexAll")
            .hasArg()
            .withDescription("")
            .create("reindexAll");

    Options options = new Options();
    options.addOption(solrZKOpt);
    options.addOption(hbaseTableNameOpt);
    options.addOption(solrCollectionOpt);
    options.addOption(commitWithinOpt);
    options.addOption(numRowsOpt);
    options.addOption(reindexAllOpt);

    CommandLineParser parser = new BasicParser();
    try {
      CommandLine cmd = parser.parse(options, args);

      final String zkConnectString = cmd.getOptionValue("solrZK");
      final String hbaseTableName = cmd.getOptionValue("hbaseTableName");
      final String solrCollection = cmd.getOptionValue("solrCollection");
      int commitWithin = Integer.parseInt(cmd.getOptionValue("commitWithin", "-1"));
      final int numRows = Integer.parseInt(cmd.getOptionValue("numRows", "100"));
      boolean reindexAll = Boolean.parseBoolean(cmd.getOptionValue("reindexAll", "false"));


      Configuration hconf = HBaseConfiguration.create();
      hconf.addResource(FileUtils.openInputStream(new File("/etc/hbase/conf/hbase-site.xml")));

//      HBaseAdmin hBaseAdmin = new HBaseAdmin(hconf);
//      if (!hBaseAdmin.tableExists(hbaseTableName)) {
//        HTableDescriptor hTableDescriptor = new HTableDescriptor(hbaseTableName);
//        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(Bytes.toBytes("metadata"));
//
//        hTableDescriptor.addFamily(hColumnDescriptor);
//        hBaseAdmin.createTable(hTableDescriptor);
//        HTable hTable = new HTable(hconf, hbaseTableName);
//        createData(hTable);
//      }

      Scan scan = new Scan();
      scan.setCaching(500);
      scan.setCacheBlocks(false);
      Filter rowkeyFilter = new FirstKeyOnlyFilter();
      scan.setFilter(rowkeyFilter);

      Job job = Job.getInstance(hconf, "ScanMapReduce");

      job.getConfiguration().set("zkConnectString", zkConnectString);
      job.getConfiguration().set("hbaseTablename", hbaseTableName);
      job.getConfiguration().set("solrCollection", solrCollection);
      job.getConfiguration().set("commitWithin", commitWithin + "");
      job.getConfiguration().set("numRows", numRows + "");
      job.getConfiguration().set("reindexAll", reindexAll + "");

      job.getConfiguration().addResource(FileUtils.openInputStream(new File("/etc/hbase/conf/hbase-site.xml")));

      TableMapReduceUtil.initTableMapperJob(
              hbaseTableName,        // input HBase table name
              scan,             // Scan instance to control CF and attribute selection
              ScanMapper.class,   // mapper
              NullWritable.class,             // mapper output key
              NullWritable.class,             // mapper output value
              job);


      job.setOutputFormatClass(NullOutputFormat.class);
      job.setNumReduceTasks(0);

      job.submit();
      boolean b = job.waitForCompletion(true);
      if (!b) {
        throw new IOException("error with job!");
      }
     }catch (ParseException e) {
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp("main", options);
      System.exit(0);
    }
  }

  private static void createData(HTable hTable) throws InterruptedIOException, RetriesExhaustedWithDetailsException {

    byte[] family = Bytes.toBytes("metadata");

    for (int i = 0; i < 21000000; i++ ) {
      Put put = new Put(Bytes.toBytes("" + i));
      put.add(family,Bytes.toBytes("to_s"), Bytes.toBytes(String.valueOf(i % 4)));
      put.add(family,Bytes.toBytes("from_s"), Bytes.toBytes(String.valueOf(i % 2)));
      put.add(family,Bytes.toBytes("subject_s"), Bytes.toBytes(String.valueOf((i % 2) * 3)));
      hTable.put(put);
    }

  }
}