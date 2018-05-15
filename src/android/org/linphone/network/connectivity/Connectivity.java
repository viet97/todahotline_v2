package org.linphone.network.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.linphone.R;

/**
 * Created by QuocVietDang1 on 4/20/2018.
 */

public class Connectivity {
    public static String WEAK = "kém";
    public static String NORMAL = "trung bình";
    public static String GOOD = "tốt";
    public static String EXCELENT = "rất tốt";
    private static String TAG = "ConnectivityConnection";

    /**
     * Get the network info
     *
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static String getNetworkSignal(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        try {
            if (info != null) {
                return Connectivity.getConnectionFast(info.getType(), info.getSubtype(), context);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return WEAK;
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static String getConnectionFast(int type, int subType, Context context) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            Log.d(TAG, "getConnectionFast: " + level);
            switch (level) {
                case 1:
                    return context.getString(R.string.weak_network);
                case 2:
                    return context.getString(R.string.weak_network);
                case 3:
                    return context.getString(R.string.weak_network);
                case 4:
                    return context.getString(R.string.good_network);
                default:
                    return context.getString(R.string.good_network);
            }
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return context.getString(R.string.weak_network); // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return context.getString(R.string.weak_network); // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return context.getString(R.string.weak_network); // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return context.getString(R.string.good_network); // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return context.getString(R.string.good_network); // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return context.getString(R.string.weak_network); // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return context.getString(R.string.very_good_network); // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return context.getString(R.string.very_good_network); // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return context.getString(R.string.very_good_network); // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return context.getString(R.string.good_network); // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return context.getString(R.string.very_good_network); // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return context.getString(R.string.very_good_network); // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return context.getString(R.string.very_good_network); // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return context.getString(R.string.weak_network); // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return context.getString(R.string.very_good_network);// ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return context.getString(R.string.weak_network);
            }
        }
        return context.getString(R.string.weak_network);
    }

}
