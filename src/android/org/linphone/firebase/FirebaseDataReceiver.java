package org.linphone.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by QuocVietDang1 on 3/24/2018.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {

    private final String TAG = "FirebaseDataReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "I'm in!!!" + intent.getExtras().get("type"));
        try {
            Intent startService = new Intent("com.example.helloandroid.alarms");
            context.sendBroadcast(startService);
            Bundle dataBundle = intent.getBundleExtra("data");
            Log.d(TAG, dataBundle.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
