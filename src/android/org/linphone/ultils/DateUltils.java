package org.linphone.ultils;

import java.util.Calendar;

/**
 * Created by QuocVietDang1 on 4/23/2018.
 */

public class DateUltils {
    public static final DateUltils instance = new DateUltils();

    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
