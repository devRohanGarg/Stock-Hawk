package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by Rohan Garg on 18-05-2016.
 */

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter
 * with few changes
 */
public class ListProvider implements RemoteViewsFactory {
    private Context context = null;
    private Cursor mCursor;
    private int appWidgetId;

    public ListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        populateListItem();
    }

    private void populateListItem() {
        mCursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String[]{QuoteColumns.SYMBOL, QuoteColumns.NAME, QuoteColumns.BIDPRICE, QuoteColumns.CURRENCY,
                        QuoteColumns.CHANGE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.ISUP}, QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        mCursor.registerContentObserver(new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.d("TAG", "CHANGE");
            }
        });
    }

    @Override
    public int getCount() {
        if (mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);

        if (mCursor != null) {
            mCursor.moveToPosition(position);
            remoteView.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
            remoteView.setTextViewText(R.id.stock_name, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.NAME)));
            remoteView.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            remoteView.setTextViewText(R.id.currency, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CURRENCY)));

            if (mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            if (Utils.showPercent) {
                remoteView.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
            } else {
                remoteView.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
            }
        }
        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}