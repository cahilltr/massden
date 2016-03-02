package com.avalonconsult;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cahillt on 3/2/16.
 * Runnable for Random Character producer
 */
public class RandomCharacterProducerWorker implements Runnable {

  private Boolean shutdown = true;
  private final String topic;
  private final Properties props;
  private final int threadNum;
  private AtomicInteger atomicInteger;

  public RandomCharacterProducerWorker(String topic, Properties producerprops, int threadNum, AtomicInteger atomicInteger) {
    this.topic = topic;
    this.props = producerprops;
    this.threadNum = threadNum;
    this.atomicInteger = atomicInteger;
  }

  public void shutDown() {
    this.shutdown = false;
  }

  @Override
  public void run() {
    ProducerConfig config = new ProducerConfig(props);
    Producer<String, String> producer = new Producer<>(config);
    int amountProduced = 0;
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    while (this.shutdown) {
      String dataString = RandomStringUtils.randomAlphabetic(250);
      KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataString);
      producer.send(data);
      amountProduced++;
      if (amountProduced % 1000 == 0 ) {
        stopWatch.stop();
        this.atomicInteger.getAndAdd(1000);
        System.out.println("Messages Produced for " +threadNum + " - " + amountProduced + " Time To Produce: " + stopWatch.getTime());
        stopWatch.reset();
        stopWatch.start();
      }
    }
    stopWatch.stop();
  }


}
