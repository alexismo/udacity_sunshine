package com.alexismorin.sunshine;

import android.content.Intent;
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

public class DetailActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;

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
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

        private String mForecastString;

        public DetailFragment() {
            setHasOptionsMenu(true);
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
                mForecastString = message;
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
    }
}
