package org.linphone;

/*
CallOutgoingActivity.java
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

import java.util.ArrayList;
import java.util.List;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.Reason;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.network.NetworkStateReceiver;
import org.linphone.ultils.ContactUltils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telecom.Call;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CallOutgoingActivity extends LinphoneGenericActivity implements OnClickListener,NetworkStateReceiver.NetworkStateReceiverListener{
    private static CallOutgoingActivity instance;
    public String TAG = "CallOutgoingActivity";
    private TextView name, number;
    private ImageView contactPicture, micro, speaker, hangUp;
    private LinphoneCall mCall;
    private LinphoneCoreListenerBase mListener;
    public NetworkStateReceiver networkStateReceiver;
    private boolean isMicMuted, isSpeakerEnabled;
    public static final int BUSY_CODE=6;
    public MyCallLogs.CallLog callLog;
    public static CallOutgoingActivity instance() {
        return instance;
    }

    public boolean isDeclinedAlready = false;
    public static boolean isInstanciated() {
        return instance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.call_outgoing);

        name = (TextView) findViewById(R.id.contact_name);
        number = (TextView) findViewById(R.id.contact_number);
        contactPicture = (ImageView) findViewById(R.id.contact_picture);

        isMicMuted = false;
        isSpeakerEnabled = false;

        micro = (ImageView) findViewById(R.id.micro);
        micro.setOnClickListener(this);
        speaker = (ImageView) findViewById(R.id.speaker);
        speaker.setOnClickListener(this);

        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);

        hangUp = (ImageView) findViewById(R.id.outgoing_hang_up);
        hangUp.setOnClickListener(this);

        mListener = new LinphoneCoreListenerBase(){
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (call == mCall && State.Connected == state) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, 10));
                    } else {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
                    }
                    android.util.Log.d(TAG, "callState12345: connected ");
                    if (!LinphoneActivity.isInstanciated()) {
                        return;
                    }
                    LinphoneActivity.instance().startIncallActivity(mCall);
                    finish();
                    return;
                } else if (state == State.Error) {
                    // Convert LinphoneCore message 	for internalization
                    if (call.getErrorInfo().getReason() == Reason.Declined) {
                        displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
                        android.util.Log.d(TAG, "call.getErrorInfo().getReason(): " + call.getState());
                        decline();
                    } else if (call.getErrorInfo().getReason() == Reason.NotFound) {
                        displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_SHORT);
                        decline();
                    } else if (call.getErrorInfo().getReason() == Reason.Media) {
                        displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_SHORT);
                        decline();
                    } else if (call.getErrorInfo().getReason() == Reason.Busy) {
                        android.util.Log.d(TAG, "callState: Busy");
                        displayCustomToast(getString(R.string.error_user_busy), Toast.LENGTH_SHORT);
                        addOutGoingLog(call, MyCallLogs.CallLog.MAY_BAN);
//                        Intent busyIntent = new Intent();
//                        setResult(BUSY_CODE, busyIntent);
                        decline();
                    } else if (message != null) {
                        LinphoneAddress address = mCall.getRemoteAddress();
                        displayCustomToast(address.getUserName() + " không kết nối tới tổng đài", Toast.LENGTH_SHORT);
                        addOutGoingLog(call, MyCallLogs.CallLog.OFFLINE);
                        decline();
//                        LinphoneManager.getLc().declineCall(mCall,Reason.Busy);
                    }

                }else if (state == State.CallEnd) {
                    // Convert LinphoneCore message for internalization
                    if (isDeclinedAlready) addOutGoingLog(call, MyCallLogs.CallLog.CUOC_GOI_DI);

                    android.util.Log.d(TAG, "callStateCallOutGoingActivity: "+call.getDuration());
                    if (call.getErrorInfo().getReason() == Reason.Declined) {
                        displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
                        android.util.Log.d(TAG, "callStateCallOutGoingActivity: ");
                        decline();
                    }
                }

                if (LinphoneManager.getLc().getCallsNb() == 0) {
                    finish();
                    return;
                }
            }
        };
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        instance = this;
    }

    public void addOutGoingLog(LinphoneCall linphoneCall, int status) {
        LinphoneCallLog linphoneCallLog = linphoneCall.getCallLog();
        ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(this).getCallLogs();

        int id = 0;
        if (currentCallLogs.size() > 0)
            id = currentCallLogs.get(0).getId() + 1;


        callLog = new MyCallLogs.CallLog(
                id,
                ContactUltils.instance.getContactName(linphoneCallLog.getTo().getUserName(), this),
                linphoneCallLog.getTo().getUserName(),
                linphoneCallLog.getTimestamp(),
                linphoneCallLog.getCallDuration(),
                status);
        MyCallLogs myCallLogs = DbContext.getInstance().getMyCallLogs(CallOutgoingActivity.this);
        ArrayList<MyCallLogs.CallLog> callLogs = myCallLogs.getCallLogs();
        callLogs.add(0, callLog);
        myCallLogs.setCallLogs(callLogs);
        DbContext.getInstance().setMyCallLogs(myCallLogs, CallOutgoingActivity.this);
    }
    public String getContactName(final String phoneNumber, Context context) {
        android.util.Log.d(TAG, "getContactName: " + DbContext.getInstance().getListContactTodaName(context).toString());
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = null;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        if (contactName == null) {
            try {
                if (ContactsListFragment.onlyDisplayLinphoneContacts == 1) {
                    contactName = DbContext.getInstance().getListContactTodaName(context).get(phoneNumber);
                } else if (ContactsListFragment.onlyDisplayLinphoneContacts == 2) {
                    contactName = DbContext.getInstance().getListCusContactTodaName(context).get(phoneNumber);
                }
            } catch (Exception e) {
                android.util.Log.d(TAG, "getContactName: ");
            }
        }

        return contactName;
    }

    @Override
    protected void onResume() {
        isDeclinedAlready = false;
        android.util.Log.d(TAG, "onResume: ");
        super.onResume();
        instance = this;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

        mCall = null;
        try {
            networkStateReceiver.addListener(this);
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }catch (Exception e){

        }
        // Only one call ringing at a time is allowed
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());
            for (LinphoneCall call : calls) {
                State cstate = call.getState();
                if (State.OutgoingInit == cstate || State.OutgoingProgress == cstate
                        || State.OutgoingRinging == cstate || State.OutgoingEarlyMedia == cstate) {
                    mCall = call;
                    break;
                }
                if (State.StreamsRunning == cstate) {
                    if (!LinphoneActivity.isInstanciated()) {
                        return;
                    }
                    LinphoneActivity.instance().startIncallActivity(mCall);
                    finish();
                    return;
                }
            }
        }
        if (mCall == null) {
            Log.e("Couldn't find outgoing call");
            finish();
            return;
        }

        LinphoneAddress address = mCall.getRemoteAddress();
        LinphoneContact contact = ContactsManager.getInstance().findContactFromAddress(address);
        String displayName = ContactUltils.instance.getContactName(address.getUserName(), this);
        if (contact != null) {
            LinphoneUtils.setImagePictureFromUri(this, contactPicture, contact.getPhotoUri(), contact.getThumbnailUri());
            name.setText(displayName == null ? "" : displayName);
        } else {
            name.setText(displayName == null ? "" : displayName);
        }
        if (address.getUserName().equalsIgnoreCase(displayName)) number.setVisibility(View.GONE);
        else number.setText(address.getUserName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestCallPermissions();
    }

    @Override
    protected void onPause() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try{
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
        }catch (Exception e){

        }
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.micro) {
            isMicMuted = !isMicMuted;
            if (isMicMuted) {
                micro.setImageResource(R.drawable.my_mic_off);
            } else {
                micro.setImageResource(R.drawable.my_mic);
            }
            LinphoneManager.getLc().muteMic(isMicMuted);
        }
        if (id == R.id.speaker) {
            isSpeakerEnabled = !isSpeakerEnabled;
            if(isSpeakerEnabled) {
                speaker.setImageResource(R.drawable.my_speaker_on);
            } else {
                speaker.setImageResource(R.drawable.my_speaker);
            }
            LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
        }
        if (id == R.id.outgoing_hang_up) {
            isDeclinedAlready = true;
            decline();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LinphoneManager.isInstanciated() && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            LinphoneManager.getLc().terminateCall(mCall);

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void displayCustomToast(final String message, final int duration) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

        TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
        toastText.setText(message);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    private void decline() {
        LinphoneManager.getLc().terminateCall(mCall);
        finish();
    }



    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<String>();

        int recordAudio = getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        Log.i("[Permission] Record audio permission is " + (recordAudio == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        int camera = getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());
        Log.i("[Permission] Camera permission is " + (camera == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Log.i("[Permission] Asking for record audio");
                permissionsList.add(Manifest.permission.RECORD_AUDIO);
            }
        }
        if (LinphonePreferences.instance().shouldInitiateVideoCall() || LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
            if (camera != PackageManager.PERMISSION_GRANTED) {
                if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Log.i("[Permission] Asking for camera");
                    permissionsList.add(Manifest.permission.CAMERA);
                }
            }
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i("[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        }
    }

    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {
        android.util.Log.d(TAG, "networkUnavailable: ");
        try {
            decline();
            Toast.makeText(CallOutgoingActivity.this, "Mất kết nối đến tổng đài", Toast.LENGTH_SHORT).show();
        }catch (Exception e){

        }
    }
}
