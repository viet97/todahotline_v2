package org.linphone.ultils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import org.linphone.PhoneContact;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by QuocVietDang1 on 3/26/2018.
 */

public class ContactUltils {
    public static final ContactUltils instance = new ContactUltils();
    private String TAG = "ContactUltils";

    public String getContactName(final String phoneNumber, Context context) {
        try {
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
                if (contactName == null) {
                    try {
                        contactName = DbContext.getInstance().getListCusContactTodaName(context).get(phoneNumber);
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                }
                if (contactName != null)
                    return contactName;
            }
        } catch (Exception e) {
            android.util.Log.d(TAG, "getContactName: ");
        }
        return phoneNumber;
    }

    public String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public String removeAccents(String s) {
        try {
            s = s.toLowerCase(Locale.getDefault());
            StringBuilder stringBuilder = new StringBuilder(s);
            for (int i = 0; i < stringBuilder.length(); i++) {
                if (stringBuilder.charAt(i) == 'đ') {
                    stringBuilder.setCharAt(i, 'd');
                }
            }

            s = stringBuilder.toString();
            s = removeDiacriticalMarks(s);

            return s;
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e.toString());
            return "";
        }
    }
    public ArrayList<PhoneContact> getContactsPhone(Context context) {
        ArrayList<PhoneContact> phoneContacts = new ArrayList<>();
        phoneContacts.clear();

        int permissionGranted = context.getPackageManager().checkPermission(Manifest.permission.WRITE_CONTACTS, context.getPackageName());

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CONTACTS);
            org.linphone.mediastream.Log.i("[Permission] Asking for " + Manifest.permission.WRITE_CONTACTS);
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);
        } else {
            try {
                ContentResolver cr = context.getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);

                if ((cur != null ? cur.getCount() : 0) > 0) {
                    while (cur != null && cur.moveToNext()) {
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME));

                        if (cur.getInt(cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneContacts.add(new PhoneContact(name, phoneNo));

                            }
                            pCur.close();
                        }
                    }
                }
                if (cur != null) {
                    cur.close();
                }
                DbContext.getInstance().setPhoneContacts(phoneContacts, context);
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception: " + e.toString());
            }

        }
        return phoneContacts;
    }

}
