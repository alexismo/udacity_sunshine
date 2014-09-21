package com.alexismorin.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.alexismorin.sunshine.data.*;

/**
 * Created by alexis on 19/09/14.
 */
public class WeatherDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE =
                "CREATE TABLE "+ WeatherContract.WeatherEntry.TABLE_NAME + " ("+
                        WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        //the ID of the location associated with the data
                        WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +

                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                        WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                        WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                        //set up the location column as the foreign key to the location table

                        " FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                        WeatherContract.LocationEntry.TABLE_NAME + " (" + WeatherContract.LocationEntry._ID + "), " +

                        // to ensure the application has just one weather entry per day
                        //per location, it's created a UNIQUE constraint with REPLACE strategy
                        " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATETEXT + ", " +
                        WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_LOCATION_TABLE =
                "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME +" (" +
                        WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                        WeatherContract.LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                        WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE + " REAL NOT NULL, " +
                        WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE + " REAL NOT NULL, " +
                        "UNIQUE (" + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE" +
                        ");";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.WeatherEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
