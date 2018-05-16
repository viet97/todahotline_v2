package org.linphone.firebase;

import android.app.LauncherActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.*;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.messaging.*;
import com.todahotline.DetailMessageListActivity;
import com.todahotline.MessageListFragment;

import org.linphone.*;
import org.linphone.R;
import org.linphone.core.LinphoneCore;
import org.linphone.database.DbContext;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.DetailMessageListResponse;
import org.linphone.network.models.MessagesListResponse;
import org.linphone.network.models.VoidRespon;
import org.linphone.notice.DisplayNotice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by QuocVietDang1 on 3/24/2018.
 */

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {
    private final String TAG = "FirebaseDataReceiver";
    private final String LOGOUT_TYPE = "DangXuat";
    private final String MESSAGE_TYPE = "TinNhan";
    private final String ID_TinNhan = "ID_TinNhan";
    private final String SoTinNhanChuaDoc = "SoTinNhanChuaDoc";
    public static final int MESSAGE_ID_NOTI = 50;

    public void onReceive(Context context, Intent intent) {
        try {
            int soTinNhanChuaDoc = Integer.parseInt(intent.getExtras().get(SoTinNhanChuaDoc).toString());
            if (intent.getExtras().get("type").toString().equals(LOGOUT_TYPE)) {
                SharedPreferences.Editor autoLoginEditor = context.getSharedPreferences("AutoLogin", context.MODE_PRIVATE).edit();
                autoLoginEditor.putBoolean("AutoLogin", false);
                autoLoginEditor.commit();
//                if (LinphonePreferences.instance().getAccountCount() > 0) {
//                    int accountNumber = LinphonePreferences.instance().getAccountCount();
//                    while (accountNumber >= 0) {
//                        LinphonePreferences.instance().deleteAccount(accountNumber);
//                        accountNumber--;
//                    }
//                }
                if (LinphoneActivity.instance != null) {
                    LinphoneActivity.instance().logoutAct(false);
                } else {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic("TodaPhone");
                    context.stopService(new Intent(Intent.ACTION_MAIN).setClass(context, LinphoneService.class));
                }
            } else if (intent.getExtras().get("type").toString().equals(MESSAGE_TYPE)) {
                if (LinphoneActivity.instance != null) {
                    if (!MyApplication.isActivityVisible()) {
                        if (!MyApplication.isDetailMessageVisible()) {
                            createOwnMessageNoti(context, soTinNhanChuaDoc);
                            if (DetailMessageListActivity.instance != null) {
                                DetailMessageListActivity.instance.IDTinNhanMoi = Integer.parseInt(intent.getExtras().get(ID_TinNhan).toString());
                            }
                        } else {
                            if (Integer.parseInt(intent.getExtras().get(ID_TinNhan).toString()) != DetailMessageListActivity.instance.IDTinNhan) {
                                createOwnMessageNoti(context, soTinNhanChuaDoc);

                            } else {
                                readMessage(context, Integer.parseInt(intent.getExtras().get(ID_TinNhan).toString()));
                            }
                        }
                    } else {
                        if (LinphoneActivity.instance.getCurrentFragment() != FragmentsAvailable.MESSAGE) {
                            createOwnMessageNoti(context, soTinNhanChuaDoc);
                        }
                    }
                } else {
                    createOwnMessageNoti(context, soTinNhanChuaDoc);
                }


                if (LinphoneActivity.instance != null) {

                    Log.d(TAG, "TinNhan: " + soTinNhanChuaDoc);

                    if (LinphoneActivity.instance.getCurrentFragment() != FragmentsAvailable.MESSAGE) {
//                        LinphoneActivity.instance().messageIcon.setImageResource(R.drawable.new_message_noti);
                        DbContext.getInstance().getLoginRespon(context).getData().setSoTinNhanChuaDoc(soTinNhanChuaDoc);
                        LinphoneActivity.instance.newMessages.setText(String.valueOf(soTinNhanChuaDoc));
                        LinphoneActivity.instance.newMessages.setVisibility(View.VISIBLE);
                    } else {
                        if (LinphoneActivity.instance.getFragment() != null) {
                            ((MessageListFragment) LinphoneActivity.instance().getFragment()).getMessagesList();
                        }
                    }
                    if (DetailMessageListActivity.instance != null) {
                        DetailMessageListActivity.instance.refreshData();
                    }
                }
            } else {
                if (!LinphoneService.isReady()) {
                    context.startService(new Intent(ACTION_MAIN).setClass(context, LinphoneService.class));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    private void readMessage(final Context context, int IDTinNhan) {
        final String url = "AppDocTinNhan.aspx?idnhanvien=" + DbContext.getInstance().getLoginRespon(context).getData().getIdnhanvien() + "&idtinnhan=" + IDTinNhan;
        Log.d(TAG, "DADOCTINNHAN: ");

        Service service = NetContext.getInstance().create(Service.class);
        service.docTinNhan(url).enqueue(new Callback<VoidRespon>() {
            @Override
            public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                try {
                    if (response == null) {
                        Toast.makeText(context, context.getString(R.string.adminstrator_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, context.getString(R.string.adminstrator_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoidRespon> call, Throwable t) {

                DisplayNotice.displayOnFailure(context);

            }
        });
    }

    private void createOwnMessageNoti(Context context, int soTinNhanChuaDoc) {
        if (LinphoneActivity.instance == null) {
            Intent intent;
            PendingIntent pendingIntent;
            intent = new Intent(context, LinphoneLauncherActivity.class);
            intent.putExtra("type", "TinNhan");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);


            NotificationCompat.Builder notificationBuilder = null;

            notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.message_noti_title))
                    .setSmallIcon(R.drawable.new_email_notification)
                    .setContentText(String.format(context.getString(R.string.message_noti_content), String.valueOf(soTinNhanChuaDoc)))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

            if (notificationBuilder != null) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(MESSAGE_ID_NOTI, notificationBuilder.build());
            } else {
                Log.d(TAG, "error NotificationManager");
            }
        } else {
            Intent intent;
            PendingIntent pendingIntent;
            intent = new Intent(context, LinphoneActivity.class);
            intent.putExtra(LinphoneActivity.HAS_NEW_MESSAGE, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);


            NotificationCompat.Builder notificationBuilder = null;

            notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.message_noti_title))
                    .setSmallIcon(R.drawable.new_email_notification)
                    .setContentText(String.format(context.getString(R.string.message_noti_content), String.valueOf(soTinNhanChuaDoc)))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

            if (notificationBuilder != null) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(MESSAGE_ID_NOTI, notificationBuilder.build());
            } else {
                Log.d(TAG, "error NotificationManager");
            }
        }

    }
}
