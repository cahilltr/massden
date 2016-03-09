package com.avalonconsult;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

/**
 * Created by cahillt on 2/26/16.
 * Simple Producer For Testing
 */
public class RandomCharacterProducer {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Not enough arguments");
      System.exit(1);
    }

    String topic = args[0];
    String[] ports = args[1].split(",");

    int numMessages = -1;
    if (args.length >= 3) {
      numMessages = Integer.parseInt(args[2]);
    }

    Properties props = new Properties();
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    String brokerList = "";
    for (String port1 : ports) {
      brokerList += "localhost:" + port1 + ",";
    }

    brokerList = brokerList.substring(0, brokerList.length() - 1);
    System.out.println("Broker List: " + brokerList);
    props.put("metadata.broker.list", brokerList);
//    New Producer API needs
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("bootstrap.servers", brokerList); //This is the same as the metadata.broker.list
    ProducerConfig config = new ProducerConfig(props);
    //Old Producer API
//    Producer<String, String> producer = new Producer<>(config);

    //New producer API
    KafkaProducer<String, String> producer = new KafkaProducer<>(props);

    int amountProduced = 0;
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    while (numMessages >= 0 || numMessages == -1) {
//      Old Producer API
//      String dataString = RandomStringUtils.randomAlphabetic(250);
//      KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataString);
//      producer.send(data);
      producer.send(new ProducerRecord(topic, RandomStringUtils.randomAlphabetic(250)));
      if (numMessages != -1) {
        numMessages--;
      }
      amountProduced++;
      if (amountProduced % 1000 == 0 ) {
        stopWatch.stop();
        System.out.println("Messages Produced:" + amountProduced + " Time To Produce: " + stopWatch.getTime());
        stopWatch.reset();
        stopWatch.start();
      }
    }
    stopWatch.stop();
  }

}
