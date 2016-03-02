package com.avalonconsult;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.concurrent.CountDownLatch;

/**
 * Created by cahillt on 2/29/16.
 * Per Thread runner
 */
public class MultiRunnable implements Runnable {

  private final KafkaStream kafkaStream;
  private final int threadNumber;
  private final StringDeserializer stringDeserializer = new StringDeserializer();
  private final String topic;
  private final CountDownLatch latch;

  public MultiRunnable(KafkaStream kafkaStream, int threadNumber, String topic, CountDownLatch latch) {
    this.kafkaStream = kafkaStream;
    this.threadNumber = threadNumber;
    this.topic = topic;
    this.latch = latch;
    System.out.println("Created Thread " + threadNumber);
  }

  @Override
  public void run() {
    System.out.println("Running " + threadNumber);
    try {
      ConsumerIterator it = kafkaStream.iterator();

      System.out.println("Created iterator " + it.toString() + " thread number " + threadNumber);
      if (!it.isEmpty()) {
        long i = 0;
        System.out.println(threadNumber + " i is " + i);
        while (it.hasNext()) {
          if (i == 0) {
            System.out.println("Thread " + threadNumber + ": " + stringDeserializer.deserialize(topic, (byte[]) it.next().message()));
//            i++;
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e.getCause().getLocalizedMessage());
    } finally {
      System.out.println("Shutting down Thread: " + threadNumber);
      latch.countDown();
    }
  }
}
