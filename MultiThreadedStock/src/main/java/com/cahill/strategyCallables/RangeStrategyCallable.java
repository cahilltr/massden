package com.cahill.strategyCallables;

import com.cahill.CustomIndicators.MarketCapIndicator;
import com.cahill.containers.MyStockContainer;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.VolumeIndicator;
import eu.verdelhan.ta4j.indicators.trackers.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;

import java.util.concurrent.Callable;

/**
 * Created by cahillt on 4/8/16.
 *
 */
public class RangeStrategyCallable implements Callable<Object[]> {

  private final MyStockContainer myStockContainer;

  public RangeStrategyCallable(MyStockContainer myStockContainer) {
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

    StochasticOscillatorKIndicator stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(myStockContainer.getTimeSeries(), 20);
    StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);

    Rule entryRule = new OverIndicatorRule(volumeIndicator, volumeOver)
            .and(new OverIndicatorRule(closePrice, Decimal.valueOf(5)))
            .and(new OverIndicatorRule(marketCapIndicator,Decimal.valueOf(250000000)))
            .and(new OverIndicatorRule(averageDirectionalMovementIndicator, Decimal.valueOf(20)))
            .and(new CrossedUpIndicatorRule(stochasticOscillatorKIndicator, stochasticOscillatorDIndicator));

    Rule exitRule = new OverIndicatorRule(volumeIndicator, volumeOver);

    return new Object[]{myStockContainer.getSymbol(),
            new Strategy(entryRule, exitRule)};
  }
}
