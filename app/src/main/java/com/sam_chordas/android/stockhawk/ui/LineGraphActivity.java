package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by Rohan Garg on 23-04-2016.
 */
public class LineGraphActivity extends AppCompatActivity {

    Map<String, Stock> stocks;
    String data;
    boolean isConnected;
    ConnectivityManager cm;
    private int position;
    private Context mContext;
    private String symbol;
    private Intent mServiceIntent;
    private String TAG = LineGraphActivity.class.getSimpleName();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mContext = this;
        Intent i = getIntent();
        symbol = i.getStringExtra("symbol").toUpperCase();
        position = i.getIntExtra("position", 0);
        setActionBarTitle(symbol);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mServiceIntent = new Intent(this, StockIntentService.class);

        if (isConnected) {
            mServiceIntent.putExtra("tag", "graph");
            mServiceIntent.putExtra("symbol", symbol);
            startService(mServiceIntent);
        }

        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL, QuoteColumns.HISTORICAL_QUOTE}, null,
                null, null);

        List<HistoricalQuote> historicalQuotes = null;

        if (c != null) {
            c.moveToPosition(position);
            historicalQuotes = Utils.JSONToHistoricalQuote(c.getString(c.getColumnIndex(QuoteColumns.HISTORICAL_QUOTE)));
            c.close();
        } else {
            networkToast();
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

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }
}
