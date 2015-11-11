package com.avalon.coe.simulation;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Random;

/**
 * Simulates Creating/cancelling appointments
 */
public class AppointmentSimulator {

  private final String[] reasons = new String[]{"sick", "change time", "moved"};

  private long start;
  private long end;
  private int diff;
  private Random random = new Random();

  public AppointmentSimulator(Date start, Date end) {
    this.start = start.getTime();
    this.end = end.getTime();
    this.diff = (int)(this.end - this.start);
  }

  public Appointment createAppointment(){
    Appointment appointment = new Appointment();

    appointment.date = getRandomDate();
    appointment.isCancelled = (Math.random() >= .95);
    if (appointment.isCancelled) {
      appointment.notes = reasons[this.random.nextInt(3)];
    }

    return appointment;
  }

  private DateTime getRandomDate(){
//    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    int val = random.nextInt(this.diff + 1);
    return new DateTime(this.start + val).withMillisOfSecond(0);
  }

}
