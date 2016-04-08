package com.cahill.CustomIndicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.AbstractIndicator;

/**
 * Created by cahillt on 4/6/16.
 *
 */
public class MarketCapIndicator extends AbstractIndicator<Decimal> {

  private final double marketCap;

  public MarketCapIndicator(TimeSeries series, double MarketCap) {
    super(series);
    this.marketCap = MarketCap;
  }

  @Override
  public Decimal getValue(int i) {
    return Decimal.valueOf(this.marketCap);
  }
}
