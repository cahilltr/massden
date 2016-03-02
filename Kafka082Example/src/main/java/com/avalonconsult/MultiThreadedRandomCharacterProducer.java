package com.avalonconsult;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cahillt on 3/2/16.
 * Multithreaded Producer
 */
public class MultiThreadedRandomCharacterProducer {

  static AtomicInteger integer = new AtomicInteger();

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Not enough arguments");
      System.exit(1);
    }

    String topic = args[0];
    String[] ports = args[1].split(",");
    int threads = Integer.parseInt(args[2]);

    Properties props = new Properties();
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    String brokerList = "";
    for (String port1 : ports) {
//      brokerList += "localhost:" + port1 + ",";
      brokerList += "cluster.master:" + port1 + ",";
    }

    brokerList = brokerList.substring(0, brokerList.length() - 1);
    System.out.println("Broker List: " + brokerList);
    props.put("metadata.broker.list", brokerList);



    ExecutorService executor = Executors.newFixedThreadPool(threads);
    for (int i = 0 ; i < threads; i++) {
      executor.submit(new RandomCharacterProducerWorker(topic, props, i, integer));
    }

    int prevValue = integer.get();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    while (true) {
      if (prevValue != integer.get()) {
        stopWatch.stop();
        System.out.println("Produced messaged: " + integer.get() + " in time: " + stopWatch.getTime());
        stopWatch.reset();
        prevValue = integer.get();
        stopWatch.start();
      }
    }
  }
}
