package com.cahill.containers;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import org.joda.time.DateTime;
import yahoofinance.Utils;

import java.text.SimpleDateFormat;
import java.util.*;

import com.cahill.Constants;

/**
 * Created by cahillt on 3/31/16.
 * Container for Stock Info
 */
public class MyStockContainer {
  private String symbol;
  private String name;
  private String currency;
  private String stockExchange;

  //Stats fields
  private double marketCap;

  private double eps;
  private double pe;
  private double peg;

  private double epsEstimateCurrentYear;
  private double epsEstimateNextQuarter;
  private double epsEstimateNextYear;

  private double priceBook;
  private double priceSales;
  private double bookValuePerShare;

  private double revenue;
  private double EBITDA;
  private double oneYearTargetPrice;

  //Dividend Fields
  private Calendar payDate;
  private Calendar exDate;
  private double annualYield;
  private double annualYieldPercent;

  //Day Stock Data
  private boolean couldGetData = true;
  private Set<DayContainer> dayData = new TreeSet<>();

  private boolean middayData = false;

  public void couldNotGetData() {
    this.couldGetData = false;
  }

  public boolean gotData() { return this.couldGetData; }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public void setName(String name) { this.name = name; }

  public String getName() { return this.name; }

  public void setCurrency(String curr) { this.currency = curr; }

  public String getCurrency() { return this.currency; }

  public void setStockExchange(String se) { this.stockExchange = se; }

  public String getStockExchanged() { return this.stockExchange; }

  public Calendar getPayDate() {
    return payDate;
  }

  public void setPayDate(Calendar payDate) {
    this.payDate = payDate;
  }

  public Calendar getExDate() {
    return exDate;
  }

  public void setExDate(Calendar exDate) {
    this.exDate = exDate;
  }

  public double getAnnualYield() {
    return annualYield;
  }

  public void setAnnualYield(double annualYield) {
    this.annualYield = annualYield;
  }

  public double getAnnualYieldPercent() {
    return annualYieldPercent;
  }

  public void setAnnualYieldPercent(double annualYieldPercent) {
    this.annualYieldPercent = annualYieldPercent;
  }

  public double getROE() {
    return Utils.getPercent(this.EBITDA, this.marketCap);
  }

  public String getSymbol() {
    return symbol;
  }

  public double getMarketCap() {
    return marketCap;
  }

  public void setMarketCap(double marketCap) {
    this.marketCap = marketCap;
  }

  public double getEps() {
    return eps;
  }

  public void setEps(double eps) {
    this.eps = eps;
  }

  public double getPe() {
    return pe;
  }

  public void setPe(double pe) {
    this.pe = pe;
  }

  public double getPeg() {
    return peg;
  }

  public void setPeg(double peg) {
    this.peg = peg;
  }

  public double getEpsEstimateCurrentYear() {
    return epsEstimateCurrentYear;
  }

  public void setEpsEstimateCurrentYear(double epsEstimateCurrentYear) {
    this.epsEstimateCurrentYear = epsEstimateCurrentYear;
  }

  public double getEpsEstimateNextQuarter() {
    return epsEstimateNextQuarter;
  }

  public void setEpsEstimateNextQuarter(double epsEstimateNextQuarter) {
    this.epsEstimateNextQuarter = epsEstimateNextQuarter;
  }

  public double getEpsEstimateNextYear() {
    return epsEstimateNextYear;
  }

  public void setEpsEstimateNextYear(double epsEstimateNextYear) {
    this.epsEstimateNextYear = epsEstimateNextYear;
  }

  public double getPriceBook() {
    return priceBook;
  }

  public void setPriceBook(double priceBook) {
    this.priceBook = priceBook;
  }

  public double getPriceSales() {
    return priceSales;
  }

  public void setPriceSales(double priceSales) {
    this.priceSales = priceSales;
  }

  public double getBookValuePerShare() {
    return bookValuePerShare;
  }

  public void setBookValuePerShare(double bookValuePerShare) {
    this.bookValuePerShare = bookValuePerShare;
  }

  public double getRevenue() {
    return revenue;
  }

  public void setRevenue(double revenue) {
    this.revenue = revenue;
  }

  public double getEBITDA() {
    return EBITDA;
  }

  public void setEBITDA(double EBITDA) {
    this.EBITDA = EBITDA;
  }

  public double getOneYearTargetPrice() {
    return oneYearTargetPrice;
  }

  public void setOneYearTargetPrice(double oneYearTargetPrice) {
    this.oneYearTargetPrice = oneYearTargetPrice;
  }

  public void setIsMiddayData(Boolean middayData) { this.middayData = middayData; }

  public boolean getIsMiddayData() { return this.middayData; }

  public void addDayData(DayContainer dayContainer) {
    this.dayData.add(dayContainer);
  }

  public void addMultipleDaysData(Set<DayContainer> dayContainers) { this.dayData.addAll(dayContainers); }

  public Set<DayContainer> getDayData() {
    return this.dayData;
  }

  public TimeSeries getTimeSeries() {
    //preserves oldest first order
    Iterator<DayContainer> dayContainerIter = this.dayData.iterator();
    List<Tick> ticks = new ArrayList<>();
    while(dayContainerIter.hasNext()) {
      DayContainer dayContainer = dayContainerIter.next();
      DateTime dateTime = new DateTime(dayContainer.getDate().getTimeInMillis());
      Tick tick = new Tick(dateTime,dayContainer.getOpen(), dayContainer.getHigh(), dayContainer.getLow(), dayContainer.getClose(), dayContainer.getVolume());
      ticks.add(tick);
    }
    return new TimeSeries(ticks);
  }

  private String getStats() {
    return "marketCap" + Constants.KEY_VALUE_BREAK + marketCap + Constants.FIELD_BREAK +
            "eps" + Constants.KEY_VALUE_BREAK + eps + Constants.FIELD_BREAK +
            "pe" + Constants.KEY_VALUE_BREAK + pe + Constants.FIELD_BREAK +
            "peg" + Constants.KEY_VALUE_BREAK + peg + Constants.FIELD_BREAK +
            "epsEstimateCurrentYear" + Constants.KEY_VALUE_BREAK + epsEstimateCurrentYear + Constants.FIELD_BREAK +
            "epsEstimateNextQuarter" + Constants.KEY_VALUE_BREAK + epsEstimateNextQuarter + Constants.FIELD_BREAK +
            "epsEstimateNextYear" + Constants.KEY_VALUE_BREAK + epsEstimateNextYear + Constants.FIELD_BREAK +
            "priceToBook" + Constants.KEY_VALUE_BREAK + priceBook + Constants.FIELD_BREAK +
            "priceToSales" + Constants.KEY_VALUE_BREAK + priceSales + Constants.FIELD_BREAK +
            "bookValuePerShare" + Constants.KEY_VALUE_BREAK + bookValuePerShare + Constants.FIELD_BREAK +
            "revenue" + Constants.KEY_VALUE_BREAK + revenue + Constants.FIELD_BREAK +
            "EBITDA" + Constants.KEY_VALUE_BREAK + EBITDA + Constants.FIELD_BREAK +
            "oneYearTargetPrice" + Constants.KEY_VALUE_BREAK + oneYearTargetPrice + Constants.FIELD_BREAK;
  }

  private String getDividend() {
    if (payDate != null) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return "payDate" + Constants.KEY_VALUE_BREAK + dateFormat.format(payDate.getTime()) + Constants.FIELD_BREAK +
              "exDate" + Constants.KEY_VALUE_BREAK + dateFormat.format(exDate.getTime()) + Constants.FIELD_BREAK +
              "annualYield" + Constants.KEY_VALUE_BREAK + annualYield + Constants.FIELD_BREAK +
              "annualYieldPercent" + Constants.KEY_VALUE_BREAK + annualYieldPercent + Constants.FIELD_BREAK;
    }
    return "";
  }

  private String getBasics() {
    return "symbol" + Constants.KEY_VALUE_BREAK + symbol + Constants.FIELD_BREAK +
            "name" + Constants.KEY_VALUE_BREAK + name + Constants.FIELD_BREAK +
            "currency" + Constants.KEY_VALUE_BREAK + currency + Constants.FIELD_BREAK +
            "stockExchange" + Constants.KEY_VALUE_BREAK + stockExchange + Constants.FIELD_BREAK +
            "isMiddayData" + Constants.KEY_VALUE_BREAK + middayData + Constants.FIELD_BREAK;
  }

  public String getString() {
    return getBasics() +  getDividend() + getStats() + Constants.NEW_LINE;
  }

}
