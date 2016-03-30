package com.avalon.jmxExample.MBean.WrappedMBean;

import com.avalon.jmxExample.PackageNotForUse.NonRelatedInterfaceName;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Created by cahillt on 3/29/16.
 * JMX Example
 */
public class TestStandardBeanJMX {
  private MBeanServer mbs = null;

  public TestStandardBeanJMX() {

    // Get the platform MBeanServer
    mbs = ManagementFactory.getPlatformMBeanServer();

    // Unique identification of MBeans
    MyStandardTest myStandardTest = new MyStandardTest();
    ObjectName wrappedTestName = null;

    try {
      // Uniquely identify the MBeans and register them with the platform MBeanServer
      wrappedTestName = new ObjectName("WrappedMBean:name=myWrappedTestBean");
      StandardMBean mbean = new StandardMBean(myStandardTest,NonRelatedInterfaceName.class);
      mbs.registerMBean(mbean, wrappedTestName);
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
    TestStandardBeanJMX agent = new TestStandardBeanJMX();
    System.out.println("SimpleAgent is running...");
    TestStandardBeanJMX.waitForEnterPressed();
  }
}
