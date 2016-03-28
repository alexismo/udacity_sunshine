package com.alexismorin.sunshine;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alexismorin.sunshine.data.WeatherContract;
import com.alexismorin.sunshine.data.WeatherContract.WeatherEntry;
import com.alexismorin.sunshine.data.WeatherContract.LocationEntry;
import com.alexismorin.sunshine.sync.SunshineSyncAdapter;

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

    private static final String [] LOCATION_COLUMNS = {
            LocationEntry.TABLE_NAME+"."+ LocationEntry._ID,
            LocationEntry.COLUMN_CITY_NAME,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };

    //indices tied to the LOCATION_COLUMNS

    static final int COL_LOCATION_ID = 0;
    static final int COL_LOCATION_NAME = 1;
    static final int COL_LOCATION_LAT = 2;
    static final int COL_LOCATION_LONG = 3;

    private AlarmManager alarmManager;
    private PendingIntent pi;

    private ForecastAdapter mForecastAdapter;
    private ListView mForecastListView;

    private final static String SCROLL_LIST_POSITION = "LIST_POSITION";
    private final static String CLICKED_LIST_ITEM_POSITION = "CLICKED_LIST_ITEM";

    private int mScrollPosition = ListView.INVALID_POSITION;
    private int mClickedPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout; //this can tell us if we're using dual pane mode

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
        if(cursor.moveToFirst()){//cursor.moveToFirst();
            //Swap the new cursor in (the framework takes care of closing the
            //old cursor once we return.
            mForecastAdapter.swapCursor(cursor);
        }else{
            updateWeather();
        }

        if(mScrollPosition != ListView.INVALID_POSITION){
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mForecastListView.smoothScrollToPosition(mScrollPosition);
        }

        if(!mUseTodayLayout && mClickedPosition == ListView.INVALID_POSITION){
            mForecastListView.post(new Runnable() {
                @Override
                public void run() {
                    mForecastListView.performItemClick(mForecastListView, 0, mForecastListView.getItemIdAtPosition(0));
                }
            });
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
        if(id==R.id.action_view_preferred_location){
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String location = prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );

        Cursor locationCursor = getActivity().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                LOCATION_COLUMNS,
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{location},
                LocationEntry.COLUMN_CITY_NAME + " ASC");

        if(locationCursor.moveToFirst()){
            //String cityName = locationCursor.getString(COL_LOCATION_NAME);
            String cityLat = locationCursor.getString(COL_LOCATION_LAT);
            String cityLon = locationCursor.getString(COL_LOCATION_LONG);

            Uri geoUri= Uri.parse("geo:"+cityLat+","+cityLon+"?q="+cityLat+","+cityLon)
                    .buildUpon().build();

            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            mapIntent.setData(geoUri);
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }else{

            Toast toast = Toast.makeText(getActivity(),"Couldn't find that city on the map.", Toast.LENGTH_LONG);
            toast.show();
        }

        locationCursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView
        mForecastAdapter = new ForecastAdapter(getActivity(), null,0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);

        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {

                mClickedPosition = position;

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
        if(savedInstanceState != null ){

            if(savedInstanceState.containsKey(SCROLL_LIST_POSITION)){
                // The listview probably hasn't even been populated yet. Actually perform the
                // swapout in onLoadFinished.
                mScrollPosition = savedInstanceState.getInt(SCROLL_LIST_POSITION);
            }

            if(savedInstanceState.containsKey(CLICKED_LIST_ITEM_POSITION)){
                // This is used on landscape 7-inch tablets to perform a click on the
                // first weather entry when the application first loads.
                mClickedPosition = savedInstanceState.getInt(CLICKED_LIST_ITEM_POSITION);
            }
        }

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

        if(mClickedPosition != ListView.INVALID_POSITION){
            outState.putInt(CLICKED_LIST_ITEM_POSITION, mClickedPosition);
        }

        super.onSaveInstanceState(outState);
    }

    public void onLocationChanged(){
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    private void updateWeather() {
        /*Intent sunshineIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        sunshineIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                Utility.getPreferredLocation(getActivity()));

        pi = PendingIntent.getBroadcast(getActivity(), 0, sunshineIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, pi);
        */
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}