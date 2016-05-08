package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockGraphService extends GcmTaskService {
    private String LOG_TAG = StockGraphService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;

    public StockGraphService() {
    }

    public StockGraphService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Log.d(LOG_TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onRunTask(TaskParams params) {
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        // get symbol from params.getExtra and build query
        String stockInput = params.getExtras().getString("symbol");
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\"", "UTF-8"));
            // finalize the URL for the API query.
            urlStringBuilder.append(URLEncoder.encode(" and startDate = \"2016-04-08\" and endDate = \"2016-05-08\"", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;
        String jsonResponse;

        urlString = urlStringBuilder.toString();
        try {
            getResponse = fetchData(urlString);
            result = GcmNetworkManager.RESULT_SUCCESS;
            Utils.quoteJsonToGraphVals(getResponse);
        } catch (NumberFormatException e) {
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

        return result;
    }

}