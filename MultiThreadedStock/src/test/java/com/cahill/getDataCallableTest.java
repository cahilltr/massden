package com.cahill;

import com.cahill.containers.DayContainer;
import com.cahill.containers.MyStockContainer;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Created by cahillt on 4/1/16.
 * Test for retrieving data
 * There is a bug if test is ran on a weekend.
 */
public class GetDataCallableTest {

  private final String SYMBOL = "IBM";
  private final int DAYS_BACK = 1;

  @Test
  public void testCall() throws Exception {

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, DAYS_BACK * -1);

    List<Callable<MyStockContainer>> callables = new ArrayList<>(1);
    callables.add(new GetDataCallable(SYMBOL, calendar));

    final ExecutorService executorService = Executors.newFixedThreadPool(1);
    final List<Future<MyStockContainer>> futures = executorService.invokeAll(callables);

    assertEquals(1, futures.size());
    for (Future<MyStockContainer> future : futures) {
      MyStockContainer myStockContainer = future.get();
      assertEquals(SYMBOL, myStockContainer.getSymbol());
      Set<DayContainer> dayContainers = myStockContainer.getDayData();
      assertEquals(DAYS_BACK, dayContainers.size());
      Calendar testCalendar = Calendar.getInstance();
      testCalendar.add(Calendar.DAY_OF_MONTH, DAYS_BACK * -1);
      //Tests order
      for (DayContainer dayContainer : dayContainers) {
        assertEquals(testCalendar.get(Calendar.DAY_OF_MONTH), dayContainer.getDate().get(Calendar.DAY_OF_MONTH));
        assertEquals(testCalendar.get(Calendar.MONTH), dayContainer.getDate().get(Calendar.MONTH));
        assertEquals(testCalendar.get(Calendar.YEAR), dayContainer.getDate().get(Calendar.YEAR));
        int dayOfWeek = testCalendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.MONDAY){
          testCalendar.add(Calendar.DAY_OF_MONTH, -2);
        } else {
          testCalendar.add(Calendar.DAY_OF_MONTH, -1);
        }
      }
    }
    executorService.shutdown();
  }
}