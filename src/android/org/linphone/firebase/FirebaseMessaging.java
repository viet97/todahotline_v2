package org.linphone.firebase;

/*
FirebaseMessaging.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.linphone.CallIncomingActivity;
import org.linphone.KeepAliveReceiver;
import org.linphone.LinphoneManager;
import org.linphone.LinphoneService;
import org.linphone.UIThreadDispatcher;
import org.linphone.compatibility.Compatibility;
import org.linphone.ui.AddressText;

import java.io.IOException;

import static android.content.Intent.ACTION_MAIN;
import static java.lang.Thread.sleep;


public class FirebaseMessaging extends FirebaseMessagingService {
    private String TAG="FirebaseMessaging";

    public FirebaseMessaging() {


    }

//    @Override
//    public void handleIntent(Intent intent) {
////        super.handleIntent(intent);
//        Log.d(TAG, "handleIntent: "+intent.getExtras().getString("type"));
//        AddressText add = new AddressText(this,null) ;
//        add.setText("0967092691");
////        Intent intent1 = new Intent("ServiceReady");
////        sendBroadcast(intent);
////        try {
////            FirebaseInstanceId.getInstance().deleteInstanceId();
////        } catch (IOException e) {
////            Log.d(TAG, "handleIntent: "+e.toString());
////        }
////        LinphoneManager.getInstance().newOutgoingCall(add);
//        if (!LinphoneService.isReady()) {
//        startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
//
//            while (!LinphoneService.isReady()) {
//                try {
//                    Log.d(TAG, "handleIntent: " + LinphoneService.isReady());
//                    sleep(30);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException("waiting thread sleep() has been interrupted");
//                }
////            }
////            Intent intent = new Intent(this, KeepAliveReceiver.class);
////            PendingIntent keepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
////            AlarmManager alarmManager = ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE));
////            Compatibility.scheduleAlarm(alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 600000, keepAlivePendingIntent);
////                startActivity(new Intent()
////                        .setClass(this, CallIncomingActivity.class)
////                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//            }
//        }
//    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        android.util.Log.d("FirebaseMessaging","[Push Notification] Received");

        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
        }
    }

}
