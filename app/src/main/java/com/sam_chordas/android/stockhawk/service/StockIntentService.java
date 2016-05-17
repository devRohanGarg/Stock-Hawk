package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    public static final String APPWIDGET_UPDATE = "com.sam_chordas.android.stockhawk.APPWIDGET_UPDATE";

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        Bundle args = new Bundle();
        args.putString("symbol", intent.getStringExtra("symbol"));
        String tag = intent.getStringExtra("tag");
        if (tag.equals("add") || tag.equals("init") || tag.equals("periodic") || tag.equals("graph")) {
            // We can call OnRunTask from the intent service to force it to run immediately instead of
            // scheduling a task.
            int result = new StockTaskService(this).onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
            if (result == GcmNetworkManager.RESULT_SUCCESS) {
                sendBroadcast(new Intent(APPWIDGET_UPDATE));
            }
        }
    }
}