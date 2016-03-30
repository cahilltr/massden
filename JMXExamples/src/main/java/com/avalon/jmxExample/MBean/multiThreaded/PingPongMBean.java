package com.avalon.jmxExample.MBean.multiThreaded;

/**
 * Created by cahillt on 3/29/16.
 * MBean for PingPong
 */
public interface PingPongMBean {

  String getWord();
  int getDelay();
  int getCount();
  int getCountAndDelay();
  void reduce();

}
