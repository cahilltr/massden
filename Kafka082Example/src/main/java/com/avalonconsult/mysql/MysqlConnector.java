package com.avalonconsult.mysql;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Created by cahillt on 2/29/16.
 * Mysql Conenctor Per Thread
 */
public class MysqlConnector implements Runnable {

  private final KafkaStream kafkaStream;
  private final int threadNumber;
  private final StringDeserializer stringDeserializer = new StringDeserializer();
  private final String topic;

  public MysqlConnector(KafkaStream kafkaStream, int threadNumber, String topic) {
    this.kafkaStream = kafkaStream;
    this.threadNumber = threadNumber;
    this.topic = topic;
  }

  @Override
  public void run() {
    ConsumerIterator it = kafkaStream.iterator();
    it.next().message();
    while (it.hasNext())
      System.out.println("Thread " + threadNumber + ": " + stringDeserializer.deserialize(topic, (byte[]) it.next().message()));
    System.out.println("Shutting down Thread: " + threadNumber);
  }
}
