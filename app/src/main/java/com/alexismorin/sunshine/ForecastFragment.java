package com.alexismorin.sunshine;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.alexismorin.sunshine.data.WeatherContract;
import com.alexismorin.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String[] FORECAST_COLUMNS = {
            //In this case the id needs to be fully qualified with a table name, since
            //the content provider joins the location & weather tables in the background
            //(both have an _id column)
            //On the one hand, that's annoying. On the other, you can search the weather table
            //using the location set by the user, which is only in the Location table
            //so the convenience is worth it

            WeatherEntry.TABLE_NAME+"."+WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    //These indices are tied to FORECAST_COLUMNS.
    //If FORECAST_COLUMNS changes, these must change
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private ForecastAdapter mForecastAdapter;
    private ListView mForecastListView;

    private final static String SCROLL_LIST_POSITION = "LIST_POSITION";
    private int mScrollPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final int FORECAST_LOADER_ID = 0;

    private static final String Log_TAG = ForecastFragment.class.getSimpleName();

    public interface Callback{
        /**
         * DetailFragmentCallback for when an item has been selected
         */
        void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle){

        //first, get the data
        String locationSetting = Utility.getPreferredLocation(getActivity());

        //Sort order: Ascending, by date
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherForLocationWithStartDate = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        //Cursor cur = getActivity().getContentResolver().query(weatherForLocationWithStartDate,null,null,null,sortOrder);

        return new CursorLoader(getActivity(), weatherForLocationWithStartDate,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        //Swap the new cursor in (the framework takes care of closing the
        //old cursor once we return.
        mForecastAdapter.swapCursor(cursor);

        if(mScrollPosition != ListView.INVALID_POSITION){
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mForecastListView.smoothScrollToPosition(mScrollPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        //This is called when the last Cursor provided to onLoadFinished()
        //above is about to be closed. We need to make sure we are no longer using it.
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView
        mForecastAdapter = new ForecastAdapter(getActivity(), null,0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);

        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null && cursor.moveToPosition(position)) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                mScrollPosition = position;
            }
        });

        // If there's an instance state, mine it for useful information
        // The end-goal here is that the user never knows that turning their device sidways
        // does crazy lifecycle related things. It should feel like some stuff streched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*
        if(savedInstanceState != null && savedInstanceState.containsKey(SCROLL_LIST_POSITION)){
            // The listview probably hasn't even been populated yet. Actually perform the
            // swapout in onLoadFinished.
            mScrollPosition = savedInstanceState.getInt(SCROLL_LIST_POSITION);
        }

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mScrollPosition will be set to ListView.INVALID_POSITION
        // so check for that before storing
        if(mScrollPosition != ListView.INVALID_POSITION){
            outState.putInt(SCROLL_LIST_POSITION, mScrollPosition);
        }

        super.onSaveInstanceState(outState);
    }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}