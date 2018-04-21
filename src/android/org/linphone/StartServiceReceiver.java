package org.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.content.Intent.ACTION_MAIN;
import static java.lang.Thread.sleep;

/**
 * Created by QuocVietDang1 on 3/18/2018.
 */

public class StartServiceReceiver extends BroadcastReceiver {

    private String TAG="StartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "2312321", Toast.LENGTH_SHORT).show();

        if (!LinphoneService.isReady()) {
            context.startService(new Intent(ACTION_MAIN).setClass(context, LinphoneService.class));
//            }
//            Intent intent = new Intent(this, KeepAliveReceiver.class);
//            PendingIntent keepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            AlarmManager alarmManager = ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE));
//            Compatibility.scheduleAlarm(alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 600000, keepAlivePendingIntent);
//                startActivity(new Intent()
//                        .setClass(this, CallIncomingActivity.class)
//                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }

}
