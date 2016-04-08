package com.cahill;

import com.cahill.containers.DayContainer;
import com.cahill.containers.MyStockContainer;
import com.cahill.fileHandling.DataFileWriter;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by cahillt on 4/1/16.
 * Callable for writing containers
 */
public class WriteDataCallable implements Callable<Void> {

  private final MyStockContainer myStockContainer;
  private final DataFileWriter dataFileWriter;

  public WriteDataCallable(MyStockContainer myStockContainer, DataFileWriter dataFileWriter) {
    this.myStockContainer = myStockContainer;
    this.dataFileWriter = dataFileWriter;
  }

  @Override
  public Void call() {
    String stockString = myStockContainer.getString() + Constants.NEW_LINE;

    Set<DayContainer> dayContainerList = myStockContainer.getDayData();
    for (DayContainer dc : dayContainerList) {
      stockString += dc.getString() + Constants.NEW_LINE;
    }
    stockString += Constants.STOCK_BREAK + Constants.NEW_LINE;

    this.dataFileWriter.enqueueData(stockString);
    return null;
  }
}
