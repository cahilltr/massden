package com.avalon.coe;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.Double.parseDouble;

/**
 * This MR job recreates the query "select * from products where price > 10000 order by salary desc"
 * The product data table is from Cloudera's MySQL table 'products' that is on their Sandbox
 * Uses the TotalOrderPartitioner to do ordering.
 */
public class PriceSortMR {
  private final static Logger logger = LoggerFactory.getLogger(PriceSortMR.class);

  public static class PriceSortMapper
          extends Mapper<Object, Text, DoubleWritable, Text> {

    static enum CountersEnum { BAD_LINES }

    private double price;

    @Override
    public void setup(Context context) throws IOException,
            InterruptedException {
      price = context.getConfiguration().getDouble("price.lessThan", -1.0);
    }

    @Override
    public void map(Object key, Text value, Context context) {
      String[] splits = value.toString().split(",");
      //4
      Text outputKey = new Text(splits[0] + " - " + splits[2]);
      try {
        Double d = parseDouble(splits[4]);
        if (price > -1 && price < d) {
            logger.info(outputKey + " - " + d);
            DoubleWritable doubleWritable = new DoubleWritable(d);
            context.write(doubleWritable, outputKey);
        }
      } catch (InterruptedException | IOException | java.lang.NumberFormatException e) {
        Counter counter = context.getCounter(CountersEnum.class.getName(),
                CountersEnum.BAD_LINES.toString());
        counter.increment(1);
        logger.error("ERROR", e);
      }
    }
  }

  public static class PriceSortReducer extends Reducer<DoubleWritable, Text, Text, DoubleWritable> {

    @Override
    public void reduce(DoubleWritable inKey, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

      for (Text value : values) {
        context.write(value, inKey);
      }
    }
  }

  public static void main(String[] args) {

    if (args.length != 3) {
      logger.error("Arguments required are input, output, and price");
      System.exit(255);
    }
    Configuration conf = new Configuration();
    try {
      conf.setDouble("price.lessThan", Double.parseDouble(args[2]));
      Job job = Job.getInstance(conf, "PriceSort");
      job.setJarByClass(PriceSortMR.class);
      job.setMapperClass(PriceSortMapper.class);
      job.setReducerClass(PriceSortReducer.class);
      job.setOutputKeyClass(DoubleWritable.class);
      job.setOutputValueClass(Text.class);
      job.setPartitionerClass(TotalOrderPartitioner.class);

      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

      InputSampler.Sampler<DoubleWritable, Text> sampler =
              new InputSampler.RandomSampler<>(0.1, 10000, 10);

      InputSampler.writePartitionFile(job, sampler);

      Configuration conf1 = job.getConfiguration();
      String partitionFile = TotalOrderPartitioner.getPartitionFile(conf1);
      URI partitionUri = new URI(partitionFile);
      DistributedCache.addCacheFile(partitionUri, conf1);
      DistributedCache.createSymlink(conf1);

      System.exit(job.waitForCompletion(true) ? 0 : 1);
    } catch (IOException e) {
      logger.error("IOException", e);
      System.exit(2);
    } catch (InterruptedException e) {
      logger.error("InterruptedException", e);
      System.exit(3);
    } catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException", e);
      System.exit(4);
    } catch (URISyntaxException e) {
      logger.error("URISyntaxException", e);
      System.exit(5);
    }

  }
}
