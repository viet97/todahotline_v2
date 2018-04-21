package org.linphone.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.linphone.LinphoneService;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by QuocVietDang1 on 3/24/2018.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {

    private final String TAG = "FirebaseDataReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        if (!LinphoneService.isReady()) {
            context.startService(new Intent(ACTION_MAIN).setClass(context, LinphoneService.class));
        }

    }
}
