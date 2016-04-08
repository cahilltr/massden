package com.cahill.fileHandling;

import com.cahill.Constants;
import com.cahill.containers.DayContainer;
import com.cahill.containers.MyStockContainer;
import eu.verdelhan.ta4j.Tick;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cahillt on 3/31/16.
 * Class to read files into Stock Class
 */
public class DataFileReader {

  private final String filePath;
  private final boolean fileExists;

  public DataFileReader(String filePath) throws Exception {
    this.filePath = filePath;
    File f = new File(this.filePath);
    this.fileExists = f.exists();
  }

  public Map<String, MyStockContainer> readFileToStocks() {
    Map<String, MyStockContainer> stocks = new HashMap<>();
    if (!fileExists) {
      return stocks;
    }
    boolean lastLineStocks = false;
    try(BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {
      String line = br.readLine();
      MyStockContainer myStockContainer = null;
      while (line != null) {
        if (lastLineStocks && !line.isEmpty()) {
          lastLineStocks = false;
          myStockContainer = parseStockDataLine(line);
        } else if (!line.equals(Constants.STOCK_BREAK) && !line.isEmpty()) {
          DayContainer dayContainer = parseDayLine(line);
          DateTime dateTime = new DateTime(dayContainer.getDate().getTimeInMillis());
          Tick tick = new Tick(dateTime,dayContainer.getOpen(), dayContainer.getHigh(), dayContainer.getLow(), dayContainer.getClose(), dayContainer.getVolume());
          if (myStockContainer == null) {
            throw new Exception("Illegal File Format");
          }
          myStockContainer.addDayData(dayContainer);
        } else {
          if (myStockContainer != null) {
            stocks.put(myStockContainer.getSymbol(), myStockContainer);
            myStockContainer = null;
          }
          lastLineStocks = true;
        }
        line = br.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return stocks;
  }

  private DayContainer parseDayLine(String line) throws ParseException {
    String[] splits = line.split("\\" + Constants.FIELD_BREAK);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();

    Calendar date = null;
    double open = 0;
    double low = 0;
    double high = 0;
    double close = 0;
    double adjClose = 0;
    long volume = 0;
    for (String info : splits) {
      String[] keyValues = info.split(Constants.KEY_VALUE_BREAK);
      switch (keyValues[0]) {
        case "date":
          cal.setTime(dateFormat.parse(keyValues[1]));
          date = cal;
          break;
        case "low":
          low = Double.parseDouble(keyValues[1]);
          break;
        case "high":
          high = Double.parseDouble(keyValues[1]);
          break;
        case "open":
          high = Double.parseDouble(keyValues[1]);
          break;
        case "close":
          close = Double.parseDouble(keyValues[1]);
          break;
        case "adjClose":
          adjClose = Double.parseDouble(keyValues[1]);
          break;
        case "volume":
          volume = Long.parseLong(keyValues[1]);
          break;
        default:
          throw new IllegalArgumentException("Invalid field: " + keyValues[0]);
      }
    }

    return new DayContainer(date, open, low, high, close, adjClose, volume);
  }

  private MyStockContainer parseStockDataLine(String line) throws ParseException {
    String[] splits = line.split("\\" + Constants.FIELD_BREAK);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    MyStockContainer myStockContainer = new MyStockContainer();
    Calendar cal = Calendar.getInstance();
    for (String info : splits) {
      String[] keyValues = info.split(Constants.KEY_VALUE_BREAK);
      switch (keyValues[0]) {
        case "marketCap":
          myStockContainer.setMarketCap(Double.parseDouble(keyValues[1]));
          break;
        case "eps":
          myStockContainer.setEps(Double.parseDouble(keyValues[1]));
          break;
        case "pe":
          myStockContainer.setPe(Double.parseDouble(keyValues[1]));
          break;
        case "peg":
          myStockContainer.setPeg(Double.parseDouble(keyValues[1]));
          break;
        case "epsEstimateCurrentYear":
          myStockContainer.setEpsEstimateCurrentYear(Double.parseDouble(keyValues[1]));
          break;
        case "epsEstimateNextQuarter":
          myStockContainer.setEpsEstimateNextQuarter(Double.parseDouble(keyValues[1]));
          break;
        case "epsEstimateNextYear":
          myStockContainer.setEpsEstimateNextYear(Double.parseDouble(keyValues[1]));
          break;
        case "priceToBook":
          myStockContainer.setPriceBook(Double.parseDouble(keyValues[1]));
          break;
        case "priceToSales":
          myStockContainer.setPriceSales(Double.parseDouble(keyValues[1]));
          break;
        case "bookValuePerShare":
          myStockContainer.setBookValuePerShare(Double.parseDouble(keyValues[1]));
          break;
        case "revenue":
          myStockContainer.setRevenue(Double.parseDouble(keyValues[1]));
          break;
        case "EBITDA":
          myStockContainer.setEBITDA(Double.parseDouble(keyValues[1]));
          break;
        case "oneYearTargetPrice":
          myStockContainer.setOneYearTargetPrice(Double.parseDouble(keyValues[1]));
          break;
        case "payDate":
          cal.setTime(dateFormat.parse(keyValues[1]));
          myStockContainer.setPayDate(cal);
          break;
        case "exDate":
          cal.setTime(dateFormat.parse(keyValues[1]));
          myStockContainer.setExDate(cal);
          break;
        case "annualYield":
          myStockContainer.setAnnualYield(Double.parseDouble(keyValues[1]));
          break;
        case "annualYieldPercent":
          myStockContainer.setAnnualYieldPercent(Double.parseDouble(keyValues[1]));
          break;
        case "name":
          myStockContainer.setName(keyValues[1]);
          break;
        case "currency":
          myStockContainer.setCurrency(keyValues[1]);
          break;
        case "stockExchange":
          myStockContainer.setStockExchange(keyValues[1]);
          break;
        case "symbol":
          myStockContainer.setSymbol(keyValues[1]);
          break;
        case "isMiddayData":
          myStockContainer.setIsMiddayData(Boolean.parseBoolean(keyValues[1]));
          break;
        default:
          throw new IllegalArgumentException("Invalid field: " + keyValues[0]);
      }
    }
    return myStockContainer;
  }
}
