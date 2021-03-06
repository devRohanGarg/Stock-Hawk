package com.sam_chordas.android.stockhawk.rest;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.widget.WidgetProvider;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static Context mContext;
    private static Typeface robotoLight;
    private static Typeface robotoMedium;
    private boolean isPercent;

    public QuoteCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        robotoMedium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Medium.ttf");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_quote, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
        viewHolder.name.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME)));
        viewHolder.currency.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.CURRENCY)));
        viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
        int sdk = Build.VERSION.SDK_INT;
        if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.percent_change_pill_green));
            } else {
                viewHolder.change.setBackground(ContextCompat.getDrawable(mContext, R.drawable.percent_change_pill_green));
            }
        } else {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.percent_change_pill_red));
            } else {
                viewHolder.change.setBackground(ContextCompat.getDrawable(mContext, R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent) {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        } else {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
        c.close();
        Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class));
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        mContext.sendBroadcast(i);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView name;
        public final TextView currency;
        public final TextView bidPrice;
        public final TextView change;

        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            symbol.setTypeface(robotoMedium);
            name = (TextView) itemView.findViewById(R.id.stock_name);
            name.setTypeface(robotoLight);
            currency = (TextView) itemView.findViewById(R.id.currency);
            currency.setTypeface(robotoMedium);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            bidPrice.setTypeface(robotoMedium);
            change = (TextView) itemView.findViewById(R.id.change);
            change.setTypeface(robotoMedium);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {

        }
    }
}