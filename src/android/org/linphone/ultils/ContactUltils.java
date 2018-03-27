package org.linphone.ultils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;

/**
 * Created by QuocVietDang1 on 3/26/2018.
 */

public class ContactUltils {
    public static final ContactUltils instance = new ContactUltils();
    private Object TAG = "ContactUltils";

    public String getContactName(final String phoneNumber, Context context) {
        int permissionGranted = context.getPackageManager().checkPermission(Manifest.permission.WRITE_CONTACTS, context.getPackageName());
        org.linphone.mediastream.Log.i("[Permission] " + Manifest.permission.WRITE_CONTACTS + " is " + (permissionGranted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CONTACTS);
            org.linphone.mediastream.Log.i("[Permission] Asking for " + Manifest.permission.WRITE_CONTACTS);
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);
        } else {
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
                    contactName = DbContext.getInstance().getListContactTodaName(context).get(phoneNumber);
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }

            }
            if (contactName != null)
                return contactName;
        }
        return phoneNumber;
    }

}
