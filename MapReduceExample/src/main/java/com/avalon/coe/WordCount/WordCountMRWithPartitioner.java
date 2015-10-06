package com.avalon.coe.WordCount;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Word count MR example with a Partitioner that partitions on word length
 * Word count Map and Reduce are based off of the 2 following links
 * https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html#Example:_WordCount_v2.0
 * http://hadooptutorial.wikispaces.com/Custom+partitioner
 */
public class WordCountMRWithPartitioner {
  private final static Logger logger = LoggerFactory.getLogger(WordCountMRWithPartitioner.class);

  public static class TokenizerMapper
          extends Mapper<Object, Text, Text, IntWritable> {

    static enum CountersEnum { INPUT_WORDS }

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();


    private Configuration conf;
    private BufferedReader fis;

    @Override
    public void setup(Context context) throws IOException,
            InterruptedException {
      conf = context.getConfiguration();
    }

    @Override
    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString().toLowerCase());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
        Counter counter = context.getCounter(CountersEnum.class.getName(),
                CountersEnum.INPUT_WORDS.toString());
        counter.increment(1);
      }
    }
  }

  public static class IntSumReducer
          extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
    ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static class LengthPartitioner extends Partitioner<Text, IntWritable> {

    @Override
    public int getPartition(Text text, IntWritable intWritable, int i) {

      if (i == 0) {
        return 0;
      }

      if (text.getLength() <= 4) {
        return 0;
      } else if (text.getLength() > 4 && text.getLength() <= 7) {
        return 1 % i;
      } else {
        return 2 % i;
      }
    }
  }

  public static void main(String[] args) {

    if (args.length != 3) {
      logger.error("Arguments required are input, output, and number of reducers");
      System.exit(255);
    }
    Configuration conf = new Configuration();
    try {
      Job job = Job.getInstance(conf, "word count");
      job.setJarByClass(WordCountMRWithPartitioner.class);
      job.setMapperClass(TokenizerMapper.class);
      job.setCombinerClass(IntSumReducer.class);
      job.setReducerClass(IntSumReducer.class);
      job.setPartitionerClass(LengthPartitioner.class);
      job.setNumReduceTasks(Integer.parseInt(args[2]));
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(IntWritable.class);

      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

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
    }

  }
}
