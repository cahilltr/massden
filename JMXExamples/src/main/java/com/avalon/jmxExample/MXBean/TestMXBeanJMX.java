package com.avalon.jmxExample.MXBean;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Created by cahillt on 3/29/16.
 * JMX Example
 */
public class TestMXBeanJMX {
  private MBeanServer mbs = null;

  private TestMXBeanJMX() {

    // Get the platform MBeanServer
    mbs = ManagementFactory.getPlatformMBeanServer();

    // Unique identification of MBeans
    MyMXTest myMXTest = new MyMXTest();
    ObjectName mxTestName = null;

    try {
      // Uniquely identify the MBeans and register them with the platform MBeanServer
      mxTestName = new ObjectName("MXBean:name=myMXTestBean");
      mbs.registerMBean(myMXTest, mxTestName);
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
    TestMXBeanJMX agent = new TestMXBeanJMX();
    System.out.println("SimpleAgent is running...");
    TestMXBeanJMX.waitForEnterPressed();
  }
}
