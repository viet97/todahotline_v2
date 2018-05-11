package org.linphone.ultils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.linphone.LinphoneUtils;
import org.linphone.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by QuocVietDang1 on 4/23/2018.
 */

public class DateUltils {
    public static final DateUltils instance = new DateUltils();

    private boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    private boolean isYesterday(Calendar cal) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.roll(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal, yesterday);
    }

    public static String timestampToHumanDate(Context context, long timestamp, String format) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat(format, Locale.getDefault());

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }
    @SuppressLint("SimpleDateFormat")
    public String timestampToHumanDate(long time, Context context) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        SimpleDateFormat dateFormat;
        String datetime = LinphoneUtils.timestampToHumanDate(context, time, context.getString(R.string.history_detail_date_format));
        if (isToday(cal)) {
            return context.getString(R.string.today) + " " + datetime;
        } else if (isYesterday(cal)) {
            return context.getString(R.string.yesterday) + " " + datetime;
        } else {
            dateFormat = new SimpleDateFormat(context.getString(R.string.history_date_format));
        }

        return dateFormat.format(cal.getTime()) + " " + datetime;
    }
    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public long getLongTimeFromDateString(String date, String pattern) {
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        Date dateTime;
        try {
            dateTime = df.parse(date);
            return dateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
