package com.cahill.fileHandling;

import com.cahill.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cahillt on 3/31/16.
 * Data File Writer to collect and write to a single file
 */
public class DataFileWriter implements Runnable {
  private final static Logger logger = LoggerFactory.getLogger(DataFileWriter.class);

  private boolean running = true;
  private Queue<String> dataQueue = new ConcurrentLinkedQueue<>();
  private String filePath;
  private final int flushCount;

  public DataFileWriter(String filepath, int flushCount) throws IOException {
    this.filePath = filepath;
    this.flushCount = flushCount;
  }

  @Override
  public void run() {
    int countToFlush = 0;
    try (Writer fileWriter = new BufferedWriter(new FileWriter(new File(this.filePath)))) {
      //Empty File's contents and write the Stock_Break
      fileWriter.write(Constants.STOCK_BREAK + Constants.NEW_LINE);
      fileWriter.flush();
      while (this.running) {
        String dataToWrite = this.dataQueue.poll();
        if (dataToWrite != null) {
          dataToWrite += Constants.STOCK_BREAK + Constants.NEW_LINE;
          fileWriter.append(dataToWrite);
        }
        countToFlush++;
        if (countToFlush >= flushCount) {
          fileWriter.flush();
          countToFlush = 0;
        }
      }
      //Purge the queue
      while (!this.dataQueue.isEmpty()) {
        String dataToWrite = this.dataQueue.poll();
        if (dataToWrite != null) {
          dataToWrite += Constants.NEW_LINE;
          fileWriter.append(dataToWrite);
        }
        fileWriter.append(Constants.NEW_LINE + Constants.STOCK_BREAK);
      }
      fileWriter.flush();
    } catch (IOException e) {
      logger.error("IOException", e);
    }
  }

  public void enqueueData(String data) {
    this.dataQueue.add(data);
  }

  public void stop() {
    this.running = false;
  }
}
