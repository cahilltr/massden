package com.cahill;

import com.cahill.containers.DayContainer;
import com.cahill.containers.MyStockContainer;
import com.cahill.fileHandling.DataFileReader;
import com.cahill.fileHandling.DataFileWriter;
import com.cahill.strategyCallables.RangeStrategyCallable;
import com.cahill.strategyCallables.TrendStrategyCallable;
import eu.verdelhan.ta4j.Strategy;
import org.apache.log4j.BasicConfigurator;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by cahillt on 3/30/16.
 * Main Method to start and control work
 * http://stackoverflow.com/questions/11517943/cancellationexception-when-using-executorservice
 * https://github.com/mdeverdelhan/ta4j
 * TODO Work on back testing
 */
public class StockStart {
  private final static Logger logger = LoggerFactory.getLogger(StockStart.class);

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    Properties properties = new Properties();
    if (args.length != 1) {
      throw new Exception("Needs an argument to the properties file");
    }
    properties.load(new FileInputStream(args[0]));

    String symbolsFile = properties.getProperty("symbolsFile");
    int newSymbolDaysBack = Integer.parseInt(properties.getProperty("new.symbol.days.back", "30"));
    String dataFile = properties.getProperty("dataFile");
    int numThreads = Integer.parseInt(properties.getProperty("numThreads", "5"));
    int flushCount = Integer.parseInt(properties.getProperty("flushCount", "15"));
    boolean getData = Boolean.parseBoolean(properties.getProperty("get.data", "true"));
    int maxCallableSize = Integer.parseInt(properties.getProperty("max.callables", "100"));

    StockStart stockStart = new StockStart();

    List<List<String>> symbols = getSymbols(symbolsFile, maxCallableSize);
    logger.info("Got Symbols");
    stockStart.doRun(symbols, newSymbolDaysBack, dataFile, numThreads, flushCount, getData);
  }

  private void doRun(List<List<String>> symbols, int newSymbolDaysBack, String dataFile, int numThreads,
                     int flushCount, boolean getData) throws Exception {

    logger.info("Starting doRun");
    Map<String, MyStockContainer> cleanStockContainers = new HashMap<>(symbols.size());

    for (List<String> symbolsSet : symbols) {
      cleanStockContainers.putAll(getFreshStockContainers(symbolsSet, dataFile, newSymbolDaysBack, numThreads, getData));
    }
//---------------------------------


    List<TrendStrategyCallable> trendStrategyCallables = new ArrayList<>();
    for (String symbol : cleanStockContainers.keySet()) {
      trendStrategyCallables.add(new TrendStrategyCallable(cleanStockContainers.get(symbol)));
    }

    final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    //Throws java.util.concurrent.CancellationException if callable does not finish before timeout
    final List<Future<Object[]>> futures = executorService.invokeAll(trendStrategyCallables, 10, TimeUnit.MINUTES);

    Map<String, Strategy> strategies = new HashMap<>(cleanStockContainers.size());
    for (Future<Object[]> future : futures) {
      Object[] objects = future.get();
      String symbol = (String)objects[0];
      Strategy strategy = (Strategy) objects[1];
      strategies.put(symbol, strategy);
    }
    executorService.shutdown();

    List<String> trueSymbols = new ArrayList<>();
    List<String> falseSymbols = new ArrayList<>();

    for (String symbol : cleanStockContainers.keySet()) {
      Strategy strategy = strategies.get(symbol);
      int last = cleanStockContainers.get(symbol).getDayData().size();
      boolean enter = strategy.shouldEnter(last - 1);
      if (enter) {
        System.out.println(symbol + ":" + enter);
        trueSymbols.add(symbol);
      } else {
        falseSymbols.add(symbol);
      }
    }

    System.out.println("Trend Falses: " + falseSymbols.size());
    System.out.println("Trend Trues: " + trueSymbols.size());

    //------------------------------

    final ExecutorService executorServiceRange = Executors.newFixedThreadPool(numThreads);

    List<RangeStrategyCallable> rangeStrategyCallables = new ArrayList<>();
    for (String symbol : cleanStockContainers.keySet()) {
      rangeStrategyCallables.add(new RangeStrategyCallable(cleanStockContainers.get(symbol)));
    }

    //Throws java.util.concurrent.CancellationException if callable does not finish before timeout
    final List<Future<Object[]>> rangeFutures = executorServiceRange.invokeAll(rangeStrategyCallables, 10, TimeUnit.MINUTES);

    Map<String, Strategy> rangeStrategies = new HashMap<>(cleanStockContainers.size());
    for (Future<Object[]> future : rangeFutures) {
      Object[] objects = future.get();
      String symbol = (String)objects[0];
      Strategy strategy = (Strategy) objects[1];
      rangeStrategies.put(symbol, strategy);
    }
    executorServiceRange.shutdown();

    List<String> trueSymbolsRange = new ArrayList<>();
    List<String> falseSymbolsRange = new ArrayList<>();

    for (String symbol : cleanStockContainers.keySet()) {
      Strategy strategy = rangeStrategies.get(symbol);
      int last = cleanStockContainers.get(symbol).getDayData().size();
      boolean enter = strategy.shouldEnter(last - 1);
      if (enter) {
        System.out.println(symbol + ":" + enter);
        trueSymbolsRange.add(symbol);
      } else {
        falseSymbolsRange.add(symbol);
      }
    }

    System.out.println("Range Falses: " + falseSymbolsRange.size());
    System.out.println("Range Trues: " + trueSymbolsRange.size());

//---------------------------------
    writeDataToFile(cleanStockContainers, dataFile, flushCount);
  }

  private void writeDataToFile(Map<String, MyStockContainer> cleanStockContainers, String dataFile,
                               int flushCount) throws InterruptedException, IOException {
    DataFileWriter dataFileWriter = new DataFileWriter(dataFile, flushCount);

    Thread t = new Thread(dataFileWriter);
    t.start();

    for (String symbol : cleanStockContainers.keySet()) {
      MyStockContainer myStockContainer = cleanStockContainers.get(symbol);
      String stockString = myStockContainer.getString();
      Set<DayContainer> dayContainerList = myStockContainer.getDayData();
      for (DayContainer dc : dayContainerList) {
        stockString += dc.getString() + Constants.NEW_LINE;
      }
      dataFileWriter.enqueueData(stockString);
    }

    dataFileWriter.stop();
  }

  private Map<String, MyStockContainer> getFreshStockContainers(List<String> symbols, String dataFile,
                 int newSymbolDaysBack, int numThreads, boolean getData) throws Exception {
    //Read in old data and get new data. Merge existing.
    DataFileReader dataFileReader = new DataFileReader(dataFile);
    Map<String, MyStockContainer> stockContainers = dataFileReader.readFileToStocks();

    int symbolSize = symbols.size();
    Set<String> existingStockSymbols = stockContainers.keySet();

    List<String> newStocks = new ArrayList<>();
    if (stockContainers.size() == 0) {
      newStocks.addAll(symbols);
      if (!getData) {
        logger.warn("get.data is true and there are no existing stocks");
      }
    } else {
      for (String symbol : symbols) {
        if (!stockContainers.containsKey(symbol)) {
          newStocks.add(symbol);
        }
      }
    }

    List<GetDataCallable> getDataCallables = new ArrayList<>(symbolSize);

    if (getData) {
      //Brings existing Stock symbols up to date
      for (String symbol : existingStockSymbols) {
        Iterator dayContainerIter = stockContainers.get(symbol).getDayData().iterator();
        int days = newSymbolDaysBack;
        Calendar existingSymbolsCalendar = Calendar.getInstance();
        if (dayContainerIter.hasNext()) {
          MyStockContainer myStockContainer = stockContainers.get(symbol);
          boolean isMiddayData = myStockContainer.getIsMiddayData();
          DayContainer dayContainer = myStockContainer.getDayData().iterator().next();
          days = Days.daysBetween(new DateTime(dayContainer.getDate().getTimeInMillis()),
                  new DateTime(existingSymbolsCalendar.getTimeInMillis())).getDays();
          if (isMiddayData) {
            days++;
          }
        }
        existingSymbolsCalendar.add(Calendar.DAY_OF_MONTH, -1 * days);
        getDataCallables.add(new GetDataCallable(symbol, existingSymbolsCalendar));
      }

      Calendar newSymbolsCalendar = Calendar.getInstance();
      newSymbolsCalendar.add(Calendar.DAY_OF_MONTH, -1 * newSymbolDaysBack);

      for (String symbol : newStocks) {
        getDataCallables.add(new GetDataCallable(symbol, newSymbolsCalendar));
      }

      final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
      //Throws java.util.concurrent.CancellationException if callable does not finish before timeout
      final List<Future<MyStockContainer>> futures = executorService.invokeAll(getDataCallables, 10, TimeUnit.MINUTES);

      Map<String, MyStockContainer> freshStockContainers = new HashMap<>(symbolSize);
      for (Future<MyStockContainer> future : futures) {
        MyStockContainer myStockContainer = future.get();
        if (myStockContainer == null) {
          continue;
        }
        String symbol = myStockContainer.getSymbol();

        if (stockContainers.containsKey(symbol)) {
          MyStockContainer oldStockContainer = stockContainers.get(symbol);
          myStockContainer.addMultipleDaysData(oldStockContainer.getDayData());
        }
        freshStockContainers.put(symbol, myStockContainer);
      }
      executorService.shutdown();
      return freshStockContainers;
    }
    return stockContainers;
  }

  private static List<List<String>> getSymbols(String file, int maxCallableSize) throws IOException {
    List<List<String>> listsList = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      List<String> symbolList = new ArrayList<>(maxCallableSize);
      int i = 0;
      while ((line = br.readLine()) != null) {
        if (i == 100) {
          listsList.add(symbolList);
          symbolList = new ArrayList<>(maxCallableSize);
          i = 0;
        }
        i++;
        if (!line.isEmpty()) {
          symbolList.add(line.toUpperCase());
        }
      }
      listsList.add(symbolList);
    }
    return listsList;
  }
}