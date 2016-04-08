package com.cahill.fileHandling;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Created by cahillt on 3/31/16.
 * http://www.mkyong.com/java/how-to-create-temporary-file-in-java/
 */
public class DataFileWriterTest {

  private final int callablesCount = 10;
  private static File temp;

  @Before
  public void createFile() throws IOException {
    temp = File.createTempFile("MultithreadedStockTest", ".tmp");
  }

  @After
  public void deleteFile() {
    assertTrue(temp.delete());
  }

  @Test
  public void testRun() throws Exception {
    DataFileWriter dataFileWriter = new DataFileWriter(temp.getPath(), 10);
    final String[] sentenceArray = new String[]{"Hello There", "Goodbye", "World There", "Tomorrow is a day"};

    List<Callable<Object>> callables = new ArrayList<>(callablesCount);
    //create callables
    for (int i = 0; i < callablesCount; i++) {
      callables.add(new writeCallable(sentenceArray, dataFileWriter));
    }

    Thread t = new Thread(dataFileWriter);
    t.start();

    final ExecutorService executorService = Executors.newFixedThreadPool(callablesCount);
    executorService.invokeAll(callables, 10000, TimeUnit.SECONDS);

    executorService.shutdown();
    dataFileWriter.stop();

    //Check that everything is written the correct amount of times
    BufferedReader br = new BufferedReader(new FileReader(temp));
    String line = br.readLine();
    Map<String, Integer> countMap = new HashMap<>();
    while (line != null) {
      if (countMap.containsKey(line)) {
        countMap.put(line, countMap.get(line) + 1);
      } else {
        countMap.put(line, 1);
      }
      line = br.readLine();
    }
    assertEquals(sentenceArray.length, countMap.size());
    for (String key : countMap.keySet()) {
      assertEquals(callablesCount, countMap.get(key).intValue());
    }
  }
}

class writeCallable implements Callable<Object> {

  private String[] stringArray;
  private DataFileWriter dataFileWriter;

  public writeCallable(String[] array, DataFileWriter dfw) {
    this.stringArray = array;
    this.dataFileWriter = dfw;
  }


  @Override
  public Object call() throws Exception {
    for (String s : this.stringArray) {
      this.dataFileWriter.enqueueData(s);
    }
    return null;
  }
}