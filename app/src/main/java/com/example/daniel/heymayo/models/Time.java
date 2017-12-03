package com.example.daniel.heymayo.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.Math.abs;

/**
 * Created by jsayler on 11/19/17.
 *
 * POJO to generate and format unix timestamps
 */

@IgnoreExtraProperties
public class Time {

    public void Time() {}

    public static String formatDateTime(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        sdf.setTimeZone(tz);
        Date date = new Date(time);
        String localtime = sdf.format(date);
        return localtime;
    }

    // returns current time in milliseconds (unix time)
    public static long getUnixTime() {
        return System.currentTimeMillis();
    }

    // compares 2 unix timestamps and returns True if they are less than 60 minutes apart
    public static Boolean withinTimeFrame(long currentTime, long requestTime) {
        return (abs(currentTime - requestTime) / 60000) < 60;
    }
}
