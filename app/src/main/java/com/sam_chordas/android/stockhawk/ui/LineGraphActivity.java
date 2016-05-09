package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.List;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by Rohan Garg on 23-04-2016.
 */
public class LineGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    Stock stock;
    int position;
    private Context mContext;
    private String symbol;
    private String TAG = LineGraphActivity.class.getSimpleName();
    private Cursor mCursor;

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.NAME, QuoteColumns.HISTORICAL_DATA, QuoteColumns.CURRENCY,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.BIDPRICE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        Intent i = getIntent();
        symbol = i.getStringExtra("symbol").toUpperCase();
        position = i.getIntExtra("position", 0);
        Log.d(TAG, String.valueOf(position));
        setActionBarTitle(symbol);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        //        mCursor.moveToPosition(position);
//        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
//                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
//                new String[]{symbol}, null);
        List<HistoricalQuote> historicalQuotes = null;
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            historicalQuotes = Utils.JSONToHistoricalQuote(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.HISTORICAL_DATA)));
            mCursor.close();
        }

        if (historicalQuotes != null) {
            for (HistoricalQuote quote : historicalQuotes) {
                Log.d(TAG, String.valueOf(quote.getDate()) + " : " + String.valueOf(quote.getHigh()));
            }
        }
//        LineChartView mChart = (LineChartView) findViewById(R.id.linechart);
//        LineSet dataset = new LineSet(mLabels, mValues[1]);
//        dataset.setColor(Color.parseColor("#758cbb"))
////                .setFill(Color.parseColor("#2d374c"))
//                .setDotsColor(Color.parseColor("#758cbb"))
//                .setThickness(4)
//                .setDashed(new float[]{10f, 10f});
////                .beginAt(3);
//        mChart.addData(dataset);
//
//        dataset = new LineSet(mLabels, mValues[0]);
//        dataset.setColor(Color.parseColor("#b3b5bb"))
////                .setFill(Color.parseColor("#2d374c"))
//                .setDotsColor(Color.parseColor("#ffc755"))
//                .setThickness(4);
////                .beginAt(3)
////                .endAt(6);
//        mChart.addData(dataset);
//
//        // Chart
//        mChart
//                .setBorderSpacing(Tools.fromDpToPx(8))
//                .setAxisBorderValues(0, 20)
//                .setFontSize((int) Tools.fromDpToPx(16))
//                .setYLabels(AxisController.LabelPosition.NONE)
//                .setLabelsColor(Color.parseColor("#6a84c3"))
//                .setXAxis(false)
//                .setYAxis(false);
//
//        mChart.show();
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }
}
