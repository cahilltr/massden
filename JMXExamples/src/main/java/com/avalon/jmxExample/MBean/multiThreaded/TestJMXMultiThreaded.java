package com.avalon.jmxExample.MBean.multiThreaded;

import com.avalon.jmxExample.MBean.Hello;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Created by cahillt on 3/29/16.
 * JMX Example
 */
public class TestJMXMultiThreaded {
  private MBeanServer mbs = null;

  public TestJMXMultiThreaded() {

    // Get the platform MBeanServer
    mbs = ManagementFactory.getPlatformMBeanServer();

    // Unique identification of MBeans
    Hello helloBean = new Hello();
    ObjectName helloName = null;

    try {
      // Uniquely identify the MBeans and register them with the platform MBeanServer
      helloName = new ObjectName("MBeanMultiThread:name=HelloBean");
      mbs.registerMBean(helloBean, helloName);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException, InterruptedException {
    TestJMXMultiThreaded agent = new TestJMXMultiThreaded();
    System.out.println("SimpleAgent is running...");
    PingPong ping = new PingPong("ping",    2000, 1000);
    PingPong pong = new PingPong("  pong",  5000,  500);
    PingPong ding = new PingPong(" ding",   4000,  500);
    PingPong dong = new PingPong("   dong", 3000,  500);
    PingPong PONG = new PingPong("PONG",    2800,  500);
    agent.mbs.registerMBean(ping, new ObjectName("PingPong:name=ping"));
    agent.mbs.registerMBean(pong, new ObjectName("PingPong:name=pong"));
    agent.mbs.registerMBean(ding, new ObjectName("PingPong:name=ding"));
    agent.mbs.registerMBean(dong, new ObjectName("PingPong:name=dong"));
    agent.mbs.registerMBean(PONG, new ObjectName("PingPong:name=PONG"));
    ping.start();
    pong.start();
    ding.start();
    dong.start();
    PONG.start();
  }
}
