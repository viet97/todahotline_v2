package org.linphone;

import android.app.Application;

import com.todahotline.DetailMessageListActivity;

/**
 * Created by QuocVietDang1 on 5/12/2018.
 */
public class MyApplication extends Application {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static boolean isDetailMessageVisible() {
        return DetailMessageactivityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void detailMessageactivityResumed() {
        DetailMessageactivityVisible = true;
    }

    public static void detailMessageactivityPaused() {
        DetailMessageactivityVisible = false;
    }


    private static boolean activityVisible;
    private static boolean DetailMessageactivityVisible;
}