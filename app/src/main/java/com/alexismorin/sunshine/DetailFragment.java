package com.alexismorin.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexismorin.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String[] FORECAST_COLUMNS = {
            //In this case the id needs to be fully qualified with a table name, since
            //the content provider joins the location & weather tables in the background
            //(both have an _id column)
            //On the one hand, that's annoying. On the other, you can search the weather table
            //using the location set by the user, which is only in the Location table
            //so the convenience is worth it

            WeatherContract.WeatherEntry.TABLE_NAME+"."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    //These indices are tied to FORECAST_COLUMNS.
    //If FORECAST_COLUMNS changes, these must change
    static final int COL_WEATHER_ENTRY_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_WIND_DIRECTION = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mDayNameView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private AQIView mAQIView;

    private String mForecast;
    private Uri mUri;
    private ShareActionProvider mShareActionProvider;

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private static final int DETAIL_LOADER = 0;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged(String newLocation){
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if(null != uri){
            long date = Long.parseLong(WeatherContract.WeatherEntry.getDateFromUri(uri));
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation,date);
            mUri = updatedUri;

            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        View rootView =inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDayNameView = (TextView) rootView.findViewById(R.id.detail_dayname_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_description_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        mAQIView = (AQIView) rootView.findViewById(R.id.detail_aqi_aqiview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        //if onLoadFinished happens before this, we can go ahead and set the share intent
        if(mForecast != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null != mUri){
            //now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed

            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );//no sort order
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //data has finished loading, deal with it.
        if(!data.moveToFirst()){return;}

        long weatherDateInMillis = data.getLong(COL_WEATHER_DATE);

        //set the day name
        mDayNameView.setText(Utility.getDayName(getActivity(), weatherDateInMillis));

        //set the date
        String dateText = Utility.getFormattedMonthDay(getActivity(), weatherDateInMillis);
        mDateView.setText(dateText);

        //Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(getActivity());

        //set the high temp
        double high = data.getDouble(COL_WEATHER_MAX_TEMP);
        mHighTempView.setText(Utility.formatTemperature(getActivity(),high,isMetric));

        //set the low temp
        double low = data.getDouble(COL_WEATHER_MIN_TEMP);
        mLowTempView.setText(Utility.formatTemperature(getActivity(), low, isMetric));

        //set the short description
        String description = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(description);

        //set the icon
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));
        mIconView.setContentDescription(description);

        //set the humidity
        mHumidityView.setText(String.format(getActivity().getString(R.string.format_humidity), data.getFloat(COL_WEATHER_HUMIDITY)));

        //set the wind speed + direction
        int windSpeed = data.getInt(COL_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(COL_WEATHER_WIND_DIRECTION);
        mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));

        //set the pressure
        mPressureView.setText(String.format(getActivity().getString(R.string.format_pressure), data.getFloat(COL_WEATHER_PRESSURE)));

        mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

        //set the AQI @TODO actually implement this
        mAQIView.setAQI(80);

        //If OnCreateOptionsMenu has already happened, we need to update the share intent now
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}