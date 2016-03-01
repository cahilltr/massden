package com.avalonconsult;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cahillt on 2/26/16.
 * High Level Consumer
 */
public class HighLevelKafkaConsumerSingleThread {


  public static void main(String[] args) {

    if (args.length < 2) {
      System.out.println("Not enough arguments");
      System.exit(1);
    }

    String topic = args[0];
    int port = Integer.parseInt(args[1]);

    Properties props = new Properties();
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props.put("metadata.broker.list", "localhost:" + port);
    props.put("zookeeper.connect", "localhost:2181");
    props.put("group.id", "my_group");
    ConsumerConfig consumerConfig = new ConsumerConfig(props);
    ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, 1);

    System.out.println("Create Stream");

    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
    KafkaStream m_stream = consumerMap.get(topic).get(0); //hard code 0 as there is only 1.
    ConsumerIterator it = m_stream.iterator();
    System.out.println("Start Stream");
    StringDeserializer stringDeserializer = new StringDeserializer();
    while(it.hasNext()) {
      System.out.println(stringDeserializer.deserialize(topic, (byte[]) it.next().message()));
    }
    System.out.println("Done.");
  }
}
