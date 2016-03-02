package com.avalonconsult;

import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by cahillt on 2/29/16.
 * High Level Consumer with multiple threads
 */
public class HighLevelConsumerMulti {

  private final ConsumerConnector consumer;
  private final String topic;
  private ExecutorService executor;

  public HighLevelConsumerMulti(String a_zookeeper, String a_groupId, String a_topic) {
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
            createConsumerConfig(a_zookeeper, a_groupId));
    this.topic = a_topic;
  }

  public void shutdown() {
    if (consumer != null) consumer.shutdown();
    if (executor != null) executor.shutdown();
    try {
      assert executor != null;
      if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
        System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
      }
    } catch (InterruptedException e) {
      System.out.println("Interrupted during shutdown, exiting uncleanly");
    }
  }

  public void run(int a_numThreads, CountDownLatch latch) {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, a_numThreads);
    TopicFilter topicFilter = new Whitelist(topic);
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

    // now launch all the threads
    executor = Executors.newFixedThreadPool(a_numThreads);

    // now create an object to consume the messages
    int threadNumber = 0;
    for (final KafkaStream stream : streams) {
      System.out.println("Thread Number: " + threadNumber);
      executor.submit(new MultiRunnable(stream, threadNumber, topic, latch));
      threadNumber++;
    }
  }

  private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
    Properties props = new Properties();
    props.put("zookeeper.connect", a_zookeeper);
    props.put("group.id", a_groupId);
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");
    props.put("auto.offset.reset", "largest");
    props.put("consumer.timeout.ms", "1000");

    return new ConsumerConfig(props);
  }

  public static void main(String[] args) {
    String zooKeeper = args[0];
    String groupId = args[1];
    String topic = args[2];
    int threads = Integer.parseInt(args[3]);

    CountDownLatch latch = new CountDownLatch(threads);
    HighLevelConsumerMulti example = new HighLevelConsumerMulti(zooKeeper, groupId, topic);
    example.run(threads, latch);

    try {
      latch.await();
    } catch (InterruptedException ie) {
      System.out.println(ie.getMessage());
    }
    example.shutdown();
  }

}
