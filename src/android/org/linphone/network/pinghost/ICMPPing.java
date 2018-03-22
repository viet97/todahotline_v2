package org.linphone.network.pinghost;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by QuocVietDang1 on 1/12/2018.
 */

public class ICMPPing {
    public static boolean isCanPingLocalHost(final String host) {
        final boolean[] b = {false};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress in = InetAddress.getByName(host);
                    try {
                        if (in.isReachable(5000)) {
                            b[0] = true;
                            Log.d("PingHost", "run:OK ");
                        } else {
                            b[0] = false;
                            Log.d("PingHost", "run:failed ");
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                        Log.d("ICMPPing", "run:Exception " + e);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException i) {

        }

        return b[0];
    }
}
