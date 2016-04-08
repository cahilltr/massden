package com.cahill.containers;

import com.cahill.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by cahillt on 3/31/16.
 * Container for Historical Day Data
 * http://stackoverflow.com/questions/5927109/sort-objects-in-arraylist-by-date
 */
public class DayContainer implements Comparable<DayContainer> {

  private Calendar date;

  private double open;
  private double low;
  private double high;
  private double close;

  private double adjClose;

  private long volume;

  public DayContainer(Calendar date, double open, double low, double high,
                      double close, double adjClose, long volume){
    this.date = date;
    this.open = open;
    this.low = low;
    this.high = high;
    this.close = close;
    this.adjClose = adjClose;
    this.volume = volume;
  }

  public Calendar getDate() {
    return date;
  }

  public void setDate(Calendar date) {
    this.date = date;
  }

  public double getOpen() {
    return open;
  }

  public void setOpen(double open) {
    this.open = open;
  }

  public double getLow() {
    return low;
  }

  public void setLow(double low) {
    this.low = low;
  }

  public double getHigh() {
    return high;
  }

  public void setHigh(double high) {
    this.high = high;
  }

  public double getClose() {
    return close;
  }

  public void setClose(double close) {
    this.close = close;
  }

  /**
   * The adjusted closing price on a specific date
   * reflects all of the dividends and splits since that day.
   * The adjusted closing price from a date in history can be used to
   * calculate a close estimate of the total return, including dividends,
   * that an investor earned if shares were purchased on that date.
   * @return      the adjusted close price
   */
  public double getAdjClose() {
    return adjClose;
  }

  public void setAdjClose(double adjClose) {
    this.adjClose = adjClose;
  }

  public long getVolume() {
    return volume;
  }

  public void setVolume(long volume) {
    this.volume = volume;
  }

  public String getString() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = dateFormat.format(this.date.getTime());
    return "date" + Constants.KEY_VALUE_BREAK + dateStr + Constants.FIELD_BREAK +
            "low" + Constants.KEY_VALUE_BREAK + this.low + Constants.FIELD_BREAK +
            "high" + Constants.KEY_VALUE_BREAK + this.high + Constants.FIELD_BREAK +
            "open" + Constants.KEY_VALUE_BREAK + this.open + Constants.FIELD_BREAK +
            "close" + Constants.KEY_VALUE_BREAK + this.close + Constants.FIELD_BREAK +
            "adjClose" + Constants.KEY_VALUE_BREAK + this.adjClose + Constants.FIELD_BREAK +
            "volume" + Constants.KEY_VALUE_BREAK + this.volume + Constants.FIELD_BREAK;
  }

  @Override
  public int compareTo(DayContainer dc) {
    return getDate().compareTo(dc.getDate());
  }
}
