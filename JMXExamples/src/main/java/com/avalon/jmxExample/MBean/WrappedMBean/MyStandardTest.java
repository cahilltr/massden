package com.avalon.jmxExample.MBean.WrappedMBean;

import com.avalon.jmxExample.PackageNotForUse.NonRelatedInterfaceName;

/**
 * Created by cahillt on 3/29/16.
 * Class to exemplify shit
 */
public class MyStandardTest implements NonRelatedInterfaceName{
  @Override
  public String getClassString() {
    return this.getClass().toString();
  }

  @Override
  public void printMilliTime() {
    System.out.println(System.currentTimeMillis());
  }
}
