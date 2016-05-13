package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by Rohan Garg on 23-04-2016.
 */
public class LineGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    private static Typeface robotoLight;
    boolean isConnected;
    ConnectivityManager cm;
    List<HistoricalQuote> historicalQuotes;
    private int position;
    private Context mContext;
    private String LOG_TAG = LineGraphActivity.class.getSimpleName();
    private Cursor mCursor;
    private MaterialDialog mDialog;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mContext = this;
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        Intent i = getIntent();
        String symbol = i.getStringExtra("symbol").toUpperCase();
        position = i.getIntExtra("position", 0);
        setActionBarTitle(symbol);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Intent mServiceIntent = new Intent(this, StockIntentService.class);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .progress(true, 0);
        mDialog = builder.build();

        if (isConnected) {
            mServiceIntent.putExtra("tag", "graph");
            mServiceIntent.putExtra("symbol", symbol);
            startService(mServiceIntent);
            mDialog.show();
        }

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        getContentResolver().registerContentObserver(QuoteProvider.Quotes.CONTENT_URI, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                restartLoader();
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    public void networkToast() {
        Toast toast = Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showGraph() {
        TextView name = (TextView) findViewById(R.id.stock_symbol);
        assert name != null;

        if (mCursor != null) {
            mCursor.moveToPosition(position);
            historicalQuotes = Utils.JSONToHistoricalQuote(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.HISTORICAL_QUOTE)));
            name.setText(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.NAME)));
        } else {
            Log.w(LOG_TAG, "Cursor is null");
        }

        if (historicalQuotes != null && historicalQuotes.size() > 0) {
            for (HistoricalQuote quote : historicalQuotes) {
                Log.d(LOG_TAG, String.valueOf(quote.getDate()) + " : " + String.valueOf(quote.getHigh()));
            }
        } else {
            if (!isConnected)
                networkToast();
            name.setText(getString(R.string.no_data));
            Log.w(LOG_TAG, "Cursor is null");
        }
        name.setTypeface(robotoLight);

        float y_axis_start;
        float x_axis_start;

        if (historicalQuotes != null && historicalQuotes.size() > 0) {
            LineSet dataset = new LineSet();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy", Locale.ENGLISH);

            x_axis_start = historicalQuotes.get(0).getHigh().floatValue();
            y_axis_start = historicalQuotes.get(0).getHigh().floatValue();

            int k = 0;
            for (int i = historicalQuotes.size() - 1; i >= 0; i--) {
                HistoricalQuote quote = historicalQuotes.get(i);
                float high = quote.getHigh().floatValue();
                if (k % 2 == 0)
                    dataset.addPoint(dateFormat.format(quote.getDate().getTime()), high);
                else
                    dataset.addPoint("", high);
                k++;
                if (high < x_axis_start)
                    x_axis_start = high;
                if (high > y_axis_start)
                    y_axis_start = high;
            }


            LineChartView mChart = (LineChartView) findViewById(R.id.linechart);

            dataset.setColor(Color.parseColor("#fff0f0"))
                    .setDotsColor(Color.parseColor("#FF4081"))
//                    .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null)
                    .setThickness(4)
                    .beginAt(0);

            if (mChart != null) {
                mChart.addData(dataset);
                // Chart
                mChart
                        .setTypeface(robotoLight)
//                        .setBorderSpacing(Tools.fromDpToPx(8))
                        .setAxisBorderValues((int) x_axis_start - (int) x_axis_start / 8, (int) y_axis_start + (int) y_axis_start / 8)
                        .setFontSize((int) Tools.fromDpToPx(14))
                        .setYLabels(AxisController.LabelPosition.OUTSIDE)
                        .setYLabels(AxisController.LabelPosition.NONE)
                        .setLabelsColor(Color.parseColor("#fff0f0"))
                        .setXAxis(false)
                        .setYAxis(false);

                mChart.show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.NAME, QuoteColumns.HISTORICAL_QUOTE},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        showGraph();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
}
