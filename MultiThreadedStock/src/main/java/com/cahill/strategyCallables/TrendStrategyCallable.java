package com.cahill.strategyCallables;

import com.cahill.CustomIndicators.MarketCapIndicator;
import com.cahill.containers.MyStockContainer;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.VolumeIndicator;
import eu.verdelhan.ta4j.indicators.trackers.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.MACDIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;

import java.util.concurrent.Callable;

/**
 * Created by cahillt on 4/6/16.
 * Callable for strategy
 */
public class TrendStrategyCallable implements Callable<Object[]> {

  private final MyStockContainer myStockContainer;

  public TrendStrategyCallable(MyStockContainer myStockContainer) {
    this.myStockContainer = myStockContainer;
  }

  @Override
  public Object[] call() throws Exception {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(myStockContainer.getTimeSeries());

    VolumeIndicator volumeIndicator = new VolumeIndicator(myStockContainer.getTimeSeries());
    Decimal volumeOver = Decimal.valueOf(100000);

    MarketCapIndicator marketCapIndicator = new MarketCapIndicator(myStockContainer.getTimeSeries(), myStockContainer.getMarketCap());

    AverageDirectionalMovementIndicator averageDirectionalMovementIndicator =
            new AverageDirectionalMovementIndicator(myStockContainer.getTimeSeries(),20);

    MACDIndicator macdIndicator = new MACDIndicator(closePrice, 4, 7);

    EMAIndicator shortEMAIndicator = new EMAIndicator(closePrice, 4);

    EMAIndicator longEMAIndicator = new EMAIndicator(closePrice, 7);

    Rule entryRule = new OverIndicatorRule(volumeIndicator, volumeOver)
            .and(new OverIndicatorRule(closePrice, Decimal.valueOf(5)))
            .and(new OverIndicatorRule(marketCapIndicator,Decimal.valueOf(250000000)))
            .and(new OverIndicatorRule(averageDirectionalMovementIndicator, Decimal.valueOf(30)))
            .and(new CrossedUpIndicatorRule(macdIndicator, Decimal.valueOf(0.01)))
            .and(new CrossedUpIndicatorRule(shortEMAIndicator, longEMAIndicator));


    Rule exitRule = new CrossedDownIndicatorRule(macdIndicator, Decimal.valueOf(0.01));

    return new Object[]{myStockContainer.getSymbol(),
            new Strategy(entryRule, exitRule)};
  }
}
