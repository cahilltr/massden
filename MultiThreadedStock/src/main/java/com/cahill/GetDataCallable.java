package com.cahill;

import com.cahill.containers.DayContainer;
import com.cahill.containers.MyStockContainer;
import com.cahill.yahoo.QuotesProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * Created by cahillt on 3/31/16.
 * Callable gets data and returns MyStockContainer
 * http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
 * //http://ichart.finance.yahoo.com/table.csv?s=ivo.ax&a=31&b=12&c=2015&d=06&e=20&f=2016&g=d
 * http://finance.yahoo.com/d/quotes.csv?s=IBM&f=nsc4xab2sa5sbb3sb6sl1sk3sd1t1opghva2kjm3m4sj2sss1sj1sf6sr1qdyee7e9e8rr5p6p5b4s6j4t8s7&e=.csv
 * http://finance.yahoo.com/d/quotes.csv?s=IBM&nsc4xab2sa5sbb3sb6sl1sk3sd1t1opghva2kjm3m4sj2sss1sj1sf6r1qdyee7e9e8rr5p6p5b4s6j4t8s7&e=.csv
 */
public class GetDataCallable implements Callable<MyStockContainer> {
  private final static Logger logger = LoggerFactory.getLogger(GetDataCallable.class);

  private final String symbol;
  private final Calendar fromDate;

  public GetDataCallable(String symbol, Calendar fromDate) {
    this.symbol = symbol;
    this.fromDate = fromDate;
  }

  private DayContainer parseLine(String line) throws ParseException {
    String[] splits = line.split(",");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();
    cal.setTime(dateFormat.parse(splits[0]));
    return new DayContainer(cal,Double.parseDouble(splits[1]), Double.parseDouble(splits[2]), Double.parseDouble(splits[3]),
            Double.parseDouble(splits[4]), Double.parseDouble(splits[6]),Long.parseLong(splits[5]));

  }

  @Override
  public MyStockContainer call() {
    MyStockContainer myStockContainer = null;
    try {
      myStockContainer = getStockMeta();
      Calendar calendar = Calendar.getInstance();
      if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
              calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ||
          ((calendar.get(Calendar.HOUR_OF_DAY) <= 9 && calendar.get(Calendar.MINUTE) < 30
                      && calendar.get(Calendar.AM_PM) == Calendar.AM) ||
                      (calendar.get(Calendar.HOUR_OF_DAY) >= 4 && calendar.get(Calendar.AM_PM) == Calendar.PM))) {
        myStockContainer.setIsMiddayData(false);
      } else {
        myStockContainer.setIsMiddayData(true);
      }

      getHistoricalData(myStockContainer);
    } catch (Exception e) {
      logger.error("Exception for " + this.symbol, e);
    }
    return myStockContainer;
  }

  private void getHistoricalData(MyStockContainer myStockContainer) throws IOException, ParseException {
    //Historical Data
    String historyURL = "http://ichart.finance.yahoo.com/table.csv?";
    Calendar nowCal = Calendar.getInstance();

    CharSequence[] historicalParams = new String[]{"s=" + this.symbol, "a=" + fromDate.get(Calendar.MONTH),
            "b=" + fromDate.get(Calendar.DAY_OF_MONTH), "c=" + fromDate.get(Calendar.YEAR), "d=" + nowCal.get(Calendar.MONTH),
            "e=" + nowCal.get(Calendar.DAY_OF_MONTH), "f=" + nowCal.get(Calendar.YEAR), "g=d"};

    String historicalParamsString = String.join("&", historicalParams);

    String usableHistoryURL = historyURL + historicalParamsString;

    URL yahoo = new URL(usableHistoryURL);
    URLConnection yc = yahoo.openConnection();
    BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    yc.getInputStream()));

    String inputLine;
    boolean isFirst = true;
    while ((inputLine = in.readLine()) != null) {
      if (!isFirst) {
        DayContainer dayContainer = parseLine(inputLine);
        myStockContainer.addDayData(dayContainer);
      }
      isFirst = false;
    }
    in.close();

  }

  private MyStockContainer getStockMeta() throws Exception {
    MyStockContainer myStockContainer = new MyStockContainer();
    myStockContainer.setSymbol(this.symbol);

    //Get ticker Metadata
    String metaURL = "http://finance.yahoo.com/d/quotes.csv?";
    CharSequence[] metaParams = new String[Constants.DEFAULT_PROPERTIES_STRING.size()];
    Constants.DEFAULT_PROPERTIES_STRING.toArray(metaParams);

    String metaParamsString = "s=" + this.symbol + "&f=" + String.join("", metaParams) + "&e=.csv";

    String usableMetaURL = metaURL + metaParamsString;

    logger.info(this.symbol + " URL: " + usableMetaURL);

    URL yahoo = new URL(usableMetaURL);
    URLConnection yc = yahoo.openConnection();
    BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    yc.getInputStream()));
    String inputLine;

    while ((inputLine = in.readLine()) != null) {
      addMetadata(myStockContainer, inputLine);
    }
    in.close();
    return myStockContainer;
  }

  private void addMetadata(MyStockContainer myStockContainer, String inputLine) throws Exception {
    String[] splits = inputLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    if (splits.length != Constants.DEFAULT_PROPERTIES.size()) {
      return;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    Calendar cal = Calendar.getInstance();
    for (int i = 0; i < Constants.DEFAULT_PROPERTIES.size(); i++) {
      if (splits[i].toUpperCase().equals("N/A")) { continue; }
      QuotesProperty qp = Constants.DEFAULT_PROPERTIES.get(i);
      switch (qp) {
        case Ask:
        case AskRealtime:
        case AskSize:
        case AverageDailyVolume:
        case Bid:
        case BidRealtime:
        case BidSize:
        case DaysHigh:
        case DaysLow:
        case DilutedEPS:
        case FiftydayMovingAverage:
        case SharesFloat:
        case LastTradeDate:
        case LastTradePriceOnly:
        case LastTradeSize:
        case LastTradeTime:
        case OneyrTargetPrice:
        case PreviousClose:
        case SharesOwned:
        case SharesOutstanding:
        case ShortRatio:
        case TwoHundreddayMovingAverage:
        case YearHigh:
        case YearLow:
        case Open:
        case Volume:
          break;
        case BookValuePerShare:
          myStockContainer.setBookValuePerShare(Double.parseDouble(splits[i]));
          break;
        case Currency:
          myStockContainer.setCurrency(splits[i].replace("\"",""));
          break;
        case DividendPayDate:
            cal.setTime(dateFormat.parse(splits[i].replace("\"", "")));
            myStockContainer.setPayDate(cal);
          break;
        case TrailingAnnualDividendYield:
          myStockContainer.setAnnualYield(Double.parseDouble(splits[i]));
          break;
        case TrailingAnnualDividendYieldInPercent:
          myStockContainer.setAnnualYieldPercent(Double.parseDouble(splits[i]));
          break;
        case EBITDA:
          myStockContainer.setEBITDA(getBigNumbers(splits[i]));
          break;
        case EPSEstimateCurrentYear:
          myStockContainer.setEpsEstimateCurrentYear(Double.parseDouble(splits[i]));
          break;
        case EPSEstimateNextQuarter:
          myStockContainer.setEpsEstimateNextQuarter(Double.parseDouble(splits[i]));
          break;
        case EPSEstimateNextYear:
          myStockContainer.setEpsEstimateNextYear(Double.parseDouble(splits[i]));
          break;
        case ExDividendDate:
            cal.setTime(dateFormat.parse(splits[i].replace("\"", "")));
            myStockContainer.setExDate(cal);
          break;
        case MarketCapitalization:
          myStockContainer.setMarketCap(getBigNumbers(splits[i]));
          break;
        case Name:
          myStockContainer.setName(splits[i].replace("\"",""));
          break;
        case PEGRatio:
          myStockContainer.setPeg(Double.parseDouble(splits[i]));
          break;
        case PERatio:
          myStockContainer.setPe(Double.parseDouble(splits[i]));
          break;
        case PriceSales:
          myStockContainer.setPriceSales(Double.parseDouble(splits[i]));
          break;
        case PriceBook:
          myStockContainer.setPriceBook(Double.parseDouble(splits[i]));
          break;
        case Revenue:
          myStockContainer.setRevenue(getBigNumbers(splits[i]));
          break;
        case StockExchange:
          myStockContainer.setStockExchange(splits[i].replace("\"",""));
          break;
        case Symbol:
          myStockContainer.setSymbol(splits[i].replace("\"", ""));
          break;
        default:
          throw new Exception("Field Unknown: " + qp.name());
      }
    }
  }


  private static double getBigNumbers(String data) {
    double result = Double.MIN_VALUE;
    if (!isParseable(data)) {
      return result;
    }
    data = cleanNumberString(data);
    char lastChar = data.charAt(data.length() - 1);
    int multiplier = 1;
    switch (lastChar) {
      case 'B':
        data = data.substring(0, data.length() - 1);
        multiplier = 1000000000;
        break;
      case 'M':
        data = data.substring(0, data.length() - 1);
        multiplier = 1000000;
        break;
      case 'K':
        data = data.substring(0, data.length() - 1);
        multiplier = 1000;
        break;
    }
    try {
      result = Double.parseDouble(data) * multiplier;
    } catch (NumberFormatException nfe) {
      result = 0;
    }
    return result;
  }


  private static boolean isParseable(String data) {
    return !(data == null || data.equals("N/A") || data.equals("-")
            || data.equals("") || data.equals("nan"));
  }

  private static String cleanNumberString(String data) {
    return join(data.trim().split(","), "");
  }

  private static String join(String[] data, String d) {
    if (data.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    int i;

    for (i = 0; i < (data.length - 1); i++) {
      sb.append(data[i]).append(d);
    }
    return sb.append(data[i]).toString();
  }

}
