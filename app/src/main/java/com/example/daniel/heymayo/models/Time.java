package com.example.daniel.heymayo.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jsayler on 11/19/17.
 *
 * POJO to generate and format unix timestamps
 */

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

    public static long getUnixTime() {
        return System.currentTimeMillis();
    }
}
