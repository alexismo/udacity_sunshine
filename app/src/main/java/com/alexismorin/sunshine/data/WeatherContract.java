package com.alexismorin.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexis on 19/09/14.
 */
public class WeatherContract {
    // The "Content Authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website. A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device
    public static final String CONTENT_AUTHORITY = "com.alexismorin.sunshine.app";

    // Use CONTENT_AUTHORITY to create the base of all URIs which apps will use to contact
    // the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    // Possible paths (appended to the base content URI for possible URIs
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    /* Inner class that defines the table contents of the weather table*/
    public static final class WeatherEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_WEATHER;

        public static final String TABLE_NAME = "weather";

        //Column with the foreign key into the location table
        public static final String COLUMN_LOC_KEY = "location_id";
        //Date stored as text with the format yyyy-MM-dd
        public static final String COLUMN_DATETEXT = "date";
        //Weather id as returned by the API, to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        //Short description and long description of the weather, as provided by the API
        //e.g. "clear" vs. "sky is clear"
        public static final String COLUMN_SHORT_DESC = "short_desc";

        //Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";


        //Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        //Pressure is stored as a number
        public static final String COLUMN_PRESSURE = "pressure";

        //wind speed is stored as a float representing wind speed kph
        public static final String COLUMN_WIND_SPEED = "wind";

        //Degrees are meteorological degrees (e.g. 0 is north, 180 is south), stored as floats
        public static final String COLUMN_DEGREES = "degrees";

        public static Uri buildWeatherUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, String startDate){
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATETEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, String date){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri){
            return uri.getQueryParameter(COLUMN_DATETEXT);
        }

    }

    public static final class LocationEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_LOCATION;

        //Table name
        public static final String TABLE_NAME = "location";

        //name of the city
        public static final String COLUMN_CITY_NAME = "city_name";

        //location setting used against the OpenWeatherMap API
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        //longitude for the location
        public static final String COLUMN_COORD_LONG = "coord_long";

        //latitude for the location
        public static final String COLUMN_COORD_LAT = "coord_lat";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts Date class to a string representation, used for easy comparison and database
     * @param date the input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT
     */
    public static String getDbDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
}
