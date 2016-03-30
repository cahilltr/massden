package com.avalon.jmxExample.MBean.singleThreaded;

import com.avalon.jmxExample.MBean.Hello;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Created by cahillt on 3/29/16.
 * JMX Example
 */
public class TestJMX {
  private MBeanServer mbs = null;

  public TestJMX() {

    // Get the platform MBeanServer
    mbs = ManagementFactory.getPlatformMBeanServer();

    // Unique identification of MBeans
    Hello helloBean = new Hello();
    ObjectName helloName = null;

    try {
      // Uniquely identify the MBeans and register them with the platform MBeanServer
      helloName = new ObjectName("MBeanSingleThread:name=HelloBean");
      mbs.registerMBean(helloBean, helloName);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  // Utility method: so that the application continues to run
  private static void waitForEnterPressed() {
    try {
      System.out.println("Press  to continue...");
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException, InterruptedException {
    TestJMX agent = new TestJMX();
    System.out.println("SimpleAgent is running...");
    TestJMX.waitForEnterPressed();
  }
}
