package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.widget.WidgetProvider;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

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
                Log.d("StockIntentService", "SYNCED");
                Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                // since it seems the onUpdate() is only fired on that:
                int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
                i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(i);
            }
        }
    }
}