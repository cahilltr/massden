package com.cahill;

import com.cahill.containers.MyStockContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by cahillt on 4/1/16.
 * Test for WriteDataCallable
 * TODO Finish with existing example for help
 */
public class writeDataCallableTest {

  private static File temp;

  @Before
  public void setUp() throws Exception {
    temp = File.createTempFile("MultithreadedStockTest", ".tmp");
  }

  @After
  public void tearDown() throws Exception {
    assertTrue(temp.delete());
  }

  @Test
  public void call() throws Exception {




  }
}