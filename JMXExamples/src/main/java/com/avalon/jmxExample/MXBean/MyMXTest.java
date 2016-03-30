package com.avalon.jmxExample.MXBean;

import com.avalon.jmxExample.PackageNotForUse.NotRelatedMXBean;

/**
 * Created by cahillt on 3/29/16.
 * Used for example of MXBean
 */
public class MyMXTest implements NotRelatedMXBean {
  @Override
  public String getClassString() {
    return this.getClass().toString();
  }

  @Override
  public void printMilliTime() {
    System.out.println(System.currentTimeMillis());
  }
}
