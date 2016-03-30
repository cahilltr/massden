package com.avalon.jmxExample.MBean.multiThreaded;

/**
 * Created by cahillt on 3/29/16.
 * Impelments the PingPongMBean
 */
public class PingPong extends Thread implements PingPongMBean {
  private String word;                 // what word to print
  private int delay;                   // how long to pause
  private int count;                   // number of iterations
  private int something;

  PingPong(String What, int Time, int number) {
    word = What;
    delay = Time;
    count = number;
    something = number + delay;
    setName(What);
  }

  public void run() {
    try {
      for(int i=0;i < count;i++) {
        System.out.println(i+": "+word+":"+activeCount());
        sleep(delay);    // wait until next time
      }
    }  catch (InterruptedException e) {
      //Do nothing, end this thread
    }
  }

  public String getWord() {
    return word;
  }

  public int getDelay() {
    return delay;
  }

  public int getCount() {
    return count;
  }

  public int getCountAndDelay() {
    return something;
  }

  public void reduce() {
    this.count -= 10;
  }
}
