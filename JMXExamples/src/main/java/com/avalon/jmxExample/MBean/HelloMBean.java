package com.avalon.jmxExample.MBean;

/**
 * Created by cahillt on 3/29/16.
 * test interface
 */
public interface HelloMBean {

  void setMessage(String message);
  String getMessage();
  void sayHello();
}
