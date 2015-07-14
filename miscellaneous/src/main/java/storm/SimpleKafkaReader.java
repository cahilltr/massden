package storm;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import storm.kafka.*;

/**
 * Created by cahillt on 7/14/15.
 */
public class SimpleKafkaReader {

  public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {

    String zkhost = args[0];
    String topic = args[1];
    BrokerHosts zk = new ZkHosts(zkhost);
    SpoutConfig spoutConfig = new SpoutConfig(zk, topic, "/consumers", "SimpleKafkaReader");
    spoutConfig.forceFromStart = true;
    spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

    spoutConfig.forceFromStart = true;
    TopologyBuilder builder = new TopologyBuilder();

    KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

    builder.setSpout("spout", kafkaSpout);
    builder.setBolt("output", new OutputBolt()).shuffleGrouping("spout");

    Config conf = new Config();
    conf.setDebug(false);

    StormSubmitter.submitTopology("SimpleKafkaReader", conf, builder.createTopology());
  }
}

class OutputBolt extends BaseBasicBolt {

  @Override
  public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
    System.out.println(tuple.getString(0));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

  }
}
