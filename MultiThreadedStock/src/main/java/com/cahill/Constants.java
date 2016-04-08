package com.cahill;

import com.cahill.yahoo.QuotesProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cahillt on 3/31/16.
 * Constants within All Code
 */
public final class Constants {

  public static final String FIELD_BREAK = "|";
  public static final String STOCK_BREAK = "--------------------------------------------";
  public static final String KEY_VALUE_BREAK = ":";
  public static final String NEW_LINE = "\r\n";

  public static final List<String> DEFAULT_PROPERTIES_STRING = new ArrayList<>();
  public static final List<QuotesProperty> DEFAULT_PROPERTIES = new ArrayList<>();

  static {
    // Always keep the name and symbol in first and second place respectively!
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Name.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Currency.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.StockExchange.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Ask.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.AskRealtime.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.AskSize.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Bid.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.BidRealtime.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.BidSize.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.LastTradePriceOnly.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.LastTradeSize.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.LastTradeDate.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.LastTradeTime.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Open.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.PreviousClose.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.DaysLow.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.DaysHigh.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Volume.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.AverageDailyVolume.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.YearHigh.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.YearLow.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.FiftydayMovingAverage.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.TwoHundreddayMovingAverage.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.SharesOutstanding.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.SharesOwned.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.MarketCapitalization.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.SharesFloat.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Symbol.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.DividendPayDate.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.ExDividendDate.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.TrailingAnnualDividendYield.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.TrailingAnnualDividendYieldInPercent.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.DilutedEPS.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.EPSEstimateCurrentYear.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.EPSEstimateNextQuarter.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.EPSEstimateNextYear.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.PERatio.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.PEGRatio.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.PriceBook.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.PriceSales.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.BookValuePerShare.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.Revenue.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.EBITDA.getTag());
    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.OneyrTargetPrice.getTag());

    DEFAULT_PROPERTIES_STRING.add(QuotesProperty.ShortRatio.getTag());

    DEFAULT_PROPERTIES.add(QuotesProperty.Name);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

    DEFAULT_PROPERTIES.add(QuotesProperty.Currency);
    DEFAULT_PROPERTIES.add(QuotesProperty.StockExchange);

    DEFAULT_PROPERTIES.add(QuotesProperty.Ask);
    DEFAULT_PROPERTIES.add(QuotesProperty.AskRealtime);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.AskSize);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.Bid);
    DEFAULT_PROPERTIES.add(QuotesProperty.BidRealtime);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.BidSize);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

    DEFAULT_PROPERTIES.add(QuotesProperty.LastTradePriceOnly);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeSize);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeDate);
    DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeTime);

    DEFAULT_PROPERTIES.add(QuotesProperty.Open);
    DEFAULT_PROPERTIES.add(QuotesProperty.PreviousClose);
    DEFAULT_PROPERTIES.add(QuotesProperty.DaysLow);
    DEFAULT_PROPERTIES.add(QuotesProperty.DaysHigh);

    DEFAULT_PROPERTIES.add(QuotesProperty.Volume);
    DEFAULT_PROPERTIES.add(QuotesProperty.AverageDailyVolume);

    DEFAULT_PROPERTIES.add(QuotesProperty.YearHigh);
    DEFAULT_PROPERTIES.add(QuotesProperty.YearLow);

    DEFAULT_PROPERTIES.add(QuotesProperty.FiftydayMovingAverage);
    DEFAULT_PROPERTIES.add(QuotesProperty.TwoHundreddayMovingAverage);

    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.SharesOutstanding);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.SharesOwned);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.MarketCapitalization);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
    DEFAULT_PROPERTIES.add(QuotesProperty.SharesFloat);
    DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

    DEFAULT_PROPERTIES.add(QuotesProperty.DividendPayDate);
    DEFAULT_PROPERTIES.add(QuotesProperty.ExDividendDate);
    DEFAULT_PROPERTIES.add(QuotesProperty.TrailingAnnualDividendYield);
    DEFAULT_PROPERTIES.add(QuotesProperty.TrailingAnnualDividendYieldInPercent);

    DEFAULT_PROPERTIES.add(QuotesProperty.DilutedEPS);
    DEFAULT_PROPERTIES.add(QuotesProperty.EPSEstimateCurrentYear);
    DEFAULT_PROPERTIES.add(QuotesProperty.EPSEstimateNextQuarter);
    DEFAULT_PROPERTIES.add(QuotesProperty.EPSEstimateNextYear);
    DEFAULT_PROPERTIES.add(QuotesProperty.PERatio);
    DEFAULT_PROPERTIES.add(QuotesProperty.PEGRatio);

    DEFAULT_PROPERTIES.add(QuotesProperty.PriceBook);
    DEFAULT_PROPERTIES.add(QuotesProperty.PriceSales);
    DEFAULT_PROPERTIES.add(QuotesProperty.BookValuePerShare);

    DEFAULT_PROPERTIES.add(QuotesProperty.Revenue);
    DEFAULT_PROPERTIES.add(QuotesProperty.EBITDA);
    DEFAULT_PROPERTIES.add(QuotesProperty.OneyrTargetPrice);

    DEFAULT_PROPERTIES.add(QuotesProperty.ShortRatio);
  }


}
