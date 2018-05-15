package org.linphone.notice;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.linphone.R;
import org.linphone.network.connectivity.Connectivity;

/**
 * Created by QuocVietDang1 on 5/15/2018.
 */

public class DisplayNotice {
    private static String TAG = "DisplayNotice";

    public static void displayOnFailure(Context context) {
        try {
            if (Connectivity.isConnected(context)) {
                Toast.makeText(context,
                        context.getString(R.string.adminstrator_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        context.getString(R.string.network_error),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }
}
