package org.linphone.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.firebase.messaging.*;

import org.linphone.*;
import org.linphone.R;
import org.linphone.core.LinphoneCore;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by QuocVietDang1 on 3/24/2018.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {

    private final String TAG = "FirebaseDataReceiver";
    private final String LOGOUT_TYPE = "DangXuat";
    private final String MESSAGE_TYPE = "TinNhan";
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getExtras().get("type").toString());
        if (intent.getExtras().get("type").toString().equals(LOGOUT_TYPE)) {
            Log.d(TAG, "onReceive123112: " + LinphonePreferences.instance().getAccountCount());
            SharedPreferences.Editor autoLoginEditor = context.getSharedPreferences("AutoLogin", context.MODE_PRIVATE).edit();
            autoLoginEditor.putBoolean("AutoLogin", false);
            autoLoginEditor.commit();
            if (LinphonePreferences.instance().getAccountCount() > 0) {
                int accountNumber = LinphonePreferences.instance().getAccountCount();
                while (accountNumber >= 0) {
                    LinphonePreferences.instance().deleteAccount(accountNumber);
                    accountNumber--;
                }
            }
            if (LinphoneActivity.instance != null) {
                LinphoneActivity.instance().logoutAct();
            } else {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic("TodaPhone");
                context.stopService(new Intent(Intent.ACTION_MAIN).setClass(context, LinphoneService.class));
            }
        }
        if (intent.getExtras().get("type").toString().equals(MESSAGE_TYPE)) {
            if (LinphoneActivity.instance != null) {
                LinphoneActivity.instance().messageIcon.setImageResource(R.drawable.new_message_noti);
            }
        } else {
            if (!LinphoneService.isReady()) {
                context.startService(new Intent(ACTION_MAIN).setClass(context, LinphoneService.class));
            }
        }
    }
}
