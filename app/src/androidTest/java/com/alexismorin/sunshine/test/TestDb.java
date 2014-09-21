package com.alexismorin.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.alexismorin.sunshine.data.WeatherDbHelper;

import com.alexismorin.sunshine.data.WeatherContract.LocationEntry;
import com.alexismorin.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by alexis on 20/09/14.
 */
public class TestDb extends AndroidTestCase {

    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public String TEST_CITY_NAME = "North Pole";

    ContentValues getLocationContentValues(){
        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        values.put(LocationEntry.COLUMN_COORD_LATITUDE, 64.772);
        values.put(LocationEntry.COLUMN_COORD_LONGITUDE, -147.355);

        return values;
    }

    public static ContentValues getWeatherContentValues(long locationRowId){
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }

    public void testInsertReadDb(){
        //if there's an error in those massive SQL table creation Strings
        //errors will be thrown here and when you try to get a writable db
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues locationContentValues = getLocationContentValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationContentValues);

        //Verify we got a row back
        assertTrue(locationRowId != -1);

        Log.d(LOG_TAG, "New row id: "+ locationRowId);

        //A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
            LocationEntry.TABLE_NAME, //Table to Query
            null, //null queries all the columns
            null, //columns for the "where" clause
            null, //values for the "where" clause
            null, // columns to group by
            null, //columns to filter by row groups
            null // sort order
        );

        if(cursor.moveToFirst()) {
            validateCursor(locationContentValues, cursor);
        }else {
            fail("No location values returned");
        }

        //Fantastic, Now that we have a location, add some weather!

        ContentValues weatherValues = getWeatherContentValues(locationRowId);

        long weatherRowId;
        weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        //Verify we got a row back
        assertTrue(weatherRowId != -1);

        Log.d(LOG_TAG, "New row id: "+ weatherRowId);

        //A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME, //Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, //columns for the "where" clause
                null, //values for the "where" clause
                null, // columns to group by
                null, //columns to filter by row groups
                null // sort order
        );

        if(weatherCursor.moveToFirst()){
            //Get the value in each column by finding the appropriate column index
            validateCursor(locationContentValues, cursor);

            //close ALL THE THINGS
            cursor.close();
            weatherCursor.close();
            dbHelper.close();

        }else{
            fail("No weather values returned :(");
        }
    }

    static public void validateCursor(ContentValues expectedValues, Cursor valueCursor){
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx); // way to make things complicated...passing an if statement as an assertion. fair enough

            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }
}