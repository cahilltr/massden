package kafka.producer;

import org.apache.commons.lang.RandomStringUtils;
import kafka.javaapi.producer.Producer;

import java.util.Properties;

/**
 * Created by cahillt on 7/1/15.
 */
public class RandomCharacterProducer {

  public static void main(String[] args) {

    if (args.length < 2) {
      System.out.println("Not enough arguments");
      System.exit(1);
    }

    String topic = args[0];
    int port = Integer.parseInt(args[1]);

    int numMessages = -1;
    if (args.length >= 3) {
      numMessages = Integer.parseInt(args[2]);
    }

    Properties props = new Properties();
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    System.out.println("127.0.0.1:" + port);
    props.put("metadata.broker.list", "sandbox.hortonworks.com:" + port);
    ProducerConfig config = new ProducerConfig(props);
    Producer<String, String> producer = new Producer<>(config);

    int amountProduced = 0;

    while (numMessages >= 0 || numMessages == -1) {
      String dataString = RandomStringUtils.random(250);
      KeyedMessage<String, String> data = new KeyedMessage<>(topic, dataString);
      producer.send(data);
      if (numMessages != -1) {
        numMessages--;
      }
      amountProduced++;
      if (amountProduced % 1000 == 0 ) {
        System.out.println("Messages Produced:" + amountProduced);
      }
    }
  }
}
