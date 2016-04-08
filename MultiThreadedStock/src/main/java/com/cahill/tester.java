package com.cahill;

import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by cahillt on 3/31/16.
 * https://www.youtube.com/watch?v=puleXDhW34s
 * months are zero based
 * http://ichart.finance.yahoo.com/table.csv?s=ivo.ax&a=02&b=31&c=2016&d=03&e=01&f=2016&g=d
 * http://ichart.finance.yahoo.com/table.csv?s=ivo.ax&a=31&b=12&c=2015&d=06&e=20&f=2016&g=d
 * a
 * b
 * c
 * d
 * e
 * f
 * g=d for day
 *
 *
 *  http://finance.yahoo.com/d/quotes.csv?s=IBM&f=nsc4xab2sa5sbb3sb6sl1sk3sd1t1opghva2kjm3m4sj2sss1sj1sf6sr1qdyee7e9e8rr5p6p5b4s6j4t8s7&e=.csv
 * s=stock
 * e=.csv
 * f=query
 */
public class tester {

  public static void main(String[] args) throws IOException {
    Stock stock = YahooFinance.get("IBM");
    System.out.println("asdf");

//    URL yahoo = new URL("http://www.yahoo.com/");
//    URL yahoo = new URL("http://ichart.finance.yahoo.com/table.csv?s=ivo.ax&a=31&b=12&c=2015&d=06&e=20&f=2016&g=d");
//    URLConnection yc = yahoo.openConnection();
//    BufferedReader in = new BufferedReader(
//            new InputStreamReader(
//                    yc.getInputStream()));
//    String inputLine;
//
//    while ((inputLine = in.readLine()) != null)
//      System.out.println(inputLine);
//
//    in.close();


    URL yahoo = new URL("http://finance.yahoo.com/d/quotes.csv?s=IBM&f=nsc4xab2sa5sbb3sb6sl1sk3sd1t1opghva2kjm3m4sj2sss1sj1sf6sr1qdyee7e9e8rr5p6p5b4s6j4t8s7&e=.csv");
    URLConnection yc = yahoo.openConnection();
    BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    yc.getInputStream()));
    String inputLine;

    while ((inputLine = in.readLine()) != null)
      System.out.println(inputLine);

    in.close();
  }
}
