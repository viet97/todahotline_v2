package org.linphone.ultils;

import android.annotation.SuppressLint;

import org.linphone.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    @SuppressLint("SimpleDateFormat")
    private String timestampToHumanDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        SimpleDateFormat dateFormat;
        if (isToday(cal)) {
            return String.valueOf(R.string.today);
        } else if (isYesterday(cal)) {
            return String.valueOf(R.string.yesterday);
        } else {
            dateFormat = new SimpleDateFormat(String.valueOf(R.string.history_date_format));
        }

        return dateFormat.format(cal.getTime());
    }
    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
