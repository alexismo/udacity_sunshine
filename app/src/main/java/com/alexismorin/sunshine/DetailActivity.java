package com.alexismorin.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexismorin.sunshine.data.WeatherContract;

import java.util.Date;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final int FORECAST_DETAIL_LOADER = 1;

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

        private String mForecastString;
        private String mForecastDate;

        private static final String[] FORECAST_COLUMNS = {
            //In this case the id needs to be fully qualified with a table name, since
            //the content provider joins the location & weather tables in the background
            // (both and an _id column)
            // On the one hand, that's annoying. On the other, you can search the weather table
            //using the location set by the user, which is only in the location table.
            //so the convenience is worth it

            WeatherContract.WeatherEntry.TABLE_NAME + "."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            getLoaderManager().initLoader(FORECAST_DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
            //inflate the menu. This adds the menu item to the ActionBar
            inflater.inflate(R.menu.detailfragment, menu);

            //Retrieve the share menu item
            MenuItem item = menu.findItem(R.id.menu_item_share);

            //fetch and store the ActionProvider
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            //Attach the intent to the ActionProvider. You can update this at any time.
            //like when users select a new piece of data they'd like to share
            if(mShareActionProvider != null){
                mShareActionProvider.setShareIntent(createShareForecastIntent());
                Log.d(LOG_TAG, "Setting share intent");
            }else{
                Log.d(LOG_TAG,"Share Action Provider is null");
            }

            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // Get the message from the intent
            Intent intent = getActivity().getIntent();
            String message = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                TextView forecastDetails = (TextView) rootView.findViewById(R.id.forecastDetail);
                //mForecastString = message;
                mForecastDate = message;
                forecastDetails.setText(mForecastString);

                //create an intent to share
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            }

            return rootView;
        }

        private Intent createShareForecastIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,mForecastString + FORECAST_SHARE_HASHTAG);

            return shareIntent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args){
            //This is called when a new loader gets created. This
            // fragment only uses one loader, so we don't care about checking the ID

            //Date startDate = WeatherContract.getDateFromDb(mForecastDate);
            String location = Utility.getPreferredLocation(getActivity());

            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, mForecastDate);

            //now create and return a CursorLoader that will take care of
            //creating a Cursor for the data being displayed.

            return new CursorLoader(
                    getActivity(),
                    weatherForLocationUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data){
            //mForecastAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader){
            //mForecastAdapter.swapCursor(null);
        }
    }
}
