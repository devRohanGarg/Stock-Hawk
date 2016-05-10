package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private Context mContext;
    private boolean isUpdate;
    private boolean history;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        Map<String, Stock> stocks = null; // single request

        if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if (initQueryCursor == null || initQueryCursor.getCount() == 0) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext.getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    String[] symbols = new String[]{"INTC", "BABA", "TSLA", "AIR.PA", "YHOO"};
                    stocks = YahooFinance.get(symbols);
                } catch (SocketTimeoutException e) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext.getApplicationContext(), "Socket timed out! Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (initQueryCursor.getCount() > 0) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                ArrayList<String> symbolList = new ArrayList<>();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    symbolList.add(initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")));
                    initQueryCursor.moveToNext();
                }
                try {
                    String[] symbols = symbolList.toArray(new String[symbolList.size()]);
                    stocks = YahooFinance.get(symbols);
                } catch (SocketTimeoutException e) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext.getApplicationContext(), "Socket timed out! Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (initQueryCursor != null)
                initQueryCursor.close();
        } else if (params.getTag().equals("add")) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try {
                String[] symbols = {stockInput};
                stocks = YahooFinance.get(symbols);
            } catch (SocketTimeoutException e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext.getApplicationContext(), "Socket timed out! Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (FileNotFoundException e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext.getApplicationContext(), "Non-existent stock", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.getTag().equals("graph")) {
            String stockInput = params.getExtras().getString("symbol");
            String[] symbols = {stockInput};
            try {
                stocks = YahooFinance.get(symbols, true);
            } catch (SocketTimeoutException e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext.getApplicationContext(), "Socket timed out! Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            history = true;
        }

        int result = GcmNetworkManager.RESULT_FAILURE;

        if (stocks != null) {
            result = GcmNetworkManager.RESULT_SUCCESS;
            try {
                ContentValues contentValues = new ContentValues();
                // update ISCURRENT to 0 (false) so new data is current
                if (isUpdate) {
                    contentValues.put(QuoteColumns.ISCURRENT, 0);
                    mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                            null, null);
                }
                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.stocksToContentVals(stocks, history));
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);
            } catch (SQLiteException e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext.getApplicationContext(), "Non-existent stock", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return result;
    }
}
