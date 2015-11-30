package com.alexismorin.sunshine;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private final static String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position){
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType){
            case VIEW_TYPE_TODAY:{
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ForecastViewHolder viewHolder = new ForecastViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ForecastViewHolder viewHolder = (ForecastViewHolder) view.getTag();

        //Read weather icon ID from the cursor
        int viewType = getItemViewType(cursor.getPosition());
        int weatherConditionArtResourceId = -1;
        int conditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        weatherConditionArtResourceId = (viewType == 0)? Utility.getArtResourceForWeatherCondition(conditionId):
                Utility.getIconResourceForWeatherCondition(conditionId);

        viewHolder.iconView.setImageResource(weatherConditionArtResourceId);
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        viewHolder.iconView.setContentDescription(description);

        //Read the date from the cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
        viewHolder.dateView.setContentDescription(context.getString(R.string.access_word_for) +" "+ Utility.getFriendlyDayString(context, dateInMillis));

        //Read the weather forecast from the cursor
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(description);

        //Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        //Read the high temperature from the cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high,isMetric));
        viewHolder.highTempView.setContentDescription(context.getString(R.string.access_word_high_of) + Utility.formatTemperature(context, high, isMetric));

        //Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low,isMetric));
        viewHolder.lowTempView.setContentDescription(context.getString(R.string.access_word_low_of)+ Utility.formatTemperature(context, low, isMetric));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ForecastViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ForecastViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}