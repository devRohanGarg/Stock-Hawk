package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;
    private static String LOG_TAG = Utils.class.getSimpleName();

    public static ArrayList stocksToContentVals(Map<String, Stock> stocks, boolean history) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (Map.Entry<String, Stock> entry : stocks.entrySet()) {
            batchOperations.add(buildBatchOperation(entry.getValue(), history));
        }
        return batchOperations;
    }

    public static String HistoricalQuoteToJSON(List<HistoricalQuote> historicalQuotes) {
        return new Gson().toJson(historicalQuotes);
    }

    public static List<HistoricalQuote> JSONToHistoricalQuote(String historicalQuotes) {
        return new Gson().fromJson(historicalQuotes, new TypeToken<List<HistoricalQuote>>() {
        }.getType());
    }

    public static ContentProviderOperation buildBatchOperation(Stock stock, boolean history) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = String.valueOf(stock.getQuote().getChange());
            String percentage_change = String.valueOf(stock.getQuote().getChangeInPercent()) + "%";
            builder.withValue(QuoteColumns.SYMBOL, stock.getQuote().getSymbol());
            builder.withValue(QuoteColumns.NAME, stock.getName());
            builder.withValue(QuoteColumns.CURRENCY, stock.getCurrency());
            builder.withValue(QuoteColumns.BIDPRICE, String.valueOf(stock.getQuote().getBid()));
            builder.withValue(QuoteColumns.ISCURRENT, 1);

            if (history)
                builder.withValue(QuoteColumns.HISTORICAL_DATA, HistoricalQuoteToJSON(stock.getHistory()));

            if (percentage_change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.PERCENT_CHANGE, percentage_change);
            } else {
                builder.withValue(QuoteColumns.PERCENT_CHANGE, "+" + percentage_change);
            }

            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.CHANGE, change);
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.CHANGE, "+" + change);
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}