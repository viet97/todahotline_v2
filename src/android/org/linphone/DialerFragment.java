package org.linphone;

/*
DialerFragment.java
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

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.linphone.core.LinphoneCore;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.network.models.ContactResponse;
import org.linphone.ui.AddressAware;
import org.linphone.ui.AddressText;
import org.linphone.ui.CallButton;
import org.linphone.ui.EraseButton;
import org.linphone.ultils.ContactUltils;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Inflater;


public class DialerFragment extends Fragment {
    private static DialerFragment instance;
    private static boolean isCallTransferOngoing = false;
    private static final String TAG = "DialerFragment";
    private AddressAware numpad;
    private ListView lvSuggestion;
    private AddressText mAddress;
    private CallButton mCall;
    private ImageView mAddContact;
    private OnClickListener addContactListener, cancelListener, transferListener;
    private boolean shouldEmptyAddressField = true;
    private ArrayList<SuggestionDialer> suggestionDialers;
    private LayoutInflater mInflater;
    private SuggestionAdapter suggestionAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mInflater = inflater;
        View view = inflater.inflate(R.layout.dialer, container, false);
        suggestionDialers = new ArrayList<>();
        suggestionAdapter = new SuggestionAdapter();
        lvSuggestion = view.findViewById(R.id.lv_suggestion);
        lvSuggestion.setAdapter(suggestionAdapter);
        mAddress = (AddressText) view.findViewById(R.id.address);
        android.util.Log.d(TAG, "onCreateView: " + mAddress);

        mAddress.setDialerFragment(this);
        mAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                suggesDialer(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        EraseButton erase = (EraseButton) view.findViewById(R.id.erase);
        erase.setAddressWidget(mAddress);

        mCall = (CallButton) view.findViewById(R.id.call);

        mCall.setAddressWidget(mAddress);
        if (LinphoneActivity.isInstanciated() && LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null && LinphoneManager.getLcIfManagerNotDestroyedOrNull().getCallsNb() > 0) {
            if (isCallTransferOngoing) {
                mCall.setImageResource(R.drawable.call_transfer);
            } else {
                mCall.setImageResource(R.drawable.call_add);
            }
        } else {
            if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null && LinphoneManager.getLcIfManagerNotDestroyedOrNull().getVideoAutoInitiatePolicy()) {
                mCall.setImageResource(R.drawable.call_video_start);
            } else {
                mCall.setImageResource(R.drawable.my_dialer_call);
            }
        }

        numpad = (AddressAware) view.findViewById(R.id.numpad);
        if (numpad != null) {
            android.util.Log.d(TAG, "setAddressWidget: 141" + mAddress);
            numpad.setAddressWidget(mAddress);
        }

        mAddContact = (ImageView) view.findViewById(R.id.add_contact);
        mAddContact.setEnabled(!(LinphoneActivity.isInstanciated() && LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null && LinphoneManager.getLc().getCallsNb() > 0));

        addContactListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneActivity.instance().displayContactsForEdition(mAddress.getText().toString());
            }
        };
        cancelListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        };
        transferListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneCore lc = LinphoneManager.getLc();
                if (lc.getCurrentCall() == null) {
                    return;
                }
                lc.transferCall(lc.getCurrentCall(), mAddress.getText().toString());
                isCallTransferOngoing = false;
                LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        };

        resetLayout(isCallTransferOngoing);

        if (getArguments() != null) {
            shouldEmptyAddressField = false;
            String number = getArguments().getString("SipUri");
            String displayName = getArguments().getString("DisplayName");
            String photo = getArguments().getString("PhotoUri");
            mAddress.setText(number);
            if (displayName != null) {
                mAddress.setDisplayedName(displayName);
            }
            if (photo != null) {
                mAddress.setPictureUri(Uri.parse(photo));
            }
        }

        instance = this;

        return view;
    }


    private void suggesDialer(String number) {

        int contacts = getActivity().getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS, getActivity().getPackageName());
        if (contacts != PackageManager.PERMISSION_GRANTED) {
            // check READ_CONTACTS permission
            ArrayList<String> permissionsList = new ArrayList<>();
            if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                Log.i("[Permission] Asking for contacts");
                permissionsList.add(Manifest.permission.READ_CONTACTS);
            }
            if (permissionsList.size() > 0) {
                String[] permissions = new String[permissionsList.size()];
                permissions = permissionsList.toArray(permissions);
                ActivityCompat.requestPermissions(getActivity(), permissions, LinphoneActivity.PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE);
            }
        } else {
            if (DbContext.getInstance().getPhoneContacts(getActivity()).size() == 0) {
                android.util.Log.d(TAG, "phoneContacts: ");
                LinphoneActivity.instance.phoneContacts.clear();
                LinphoneActivity.instance.phoneContacts = ContactUltils.instance.getContactsPhone(getActivity());
            }
        }
        suggestionDialers.clear();
        Context context = getActivity();
        if (number.equals("")) {
            ((BaseAdapter) lvSuggestion.getAdapter()).notifyDataSetChanged();
            return;
        }
        for (ContactResponse.DSDanhBa danhba : DbContext.getInstance().getContactResponse(context).getDsdanhba()) {
            if (danhba.getSodienthoai().contains(number)) {
                suggestionDialers.add(new SuggestionDialer(danhba.getTenlienhe(), danhba.getSodienthoai()));
            }
        }
        for (ContactResponse.DSDanhBa danhba : DbContext.getInstance().getCusContactResponse(context).getDsdanhba()) {
            if (danhba.getSodienthoai().contains(number)) {
                SuggestionDialer suggestionDialer = new SuggestionDialer(danhba.getTenlienhe(), danhba.getSodienthoai());
                if (!suggestionDialers.contains(suggestionDialer))
                    suggestionDialers.add(suggestionDialer);
            }
        }
        android.util.Log.d(TAG, "suggesDialer: " + DbContext.getInstance().getPhoneContacts(context).size());
        for (PhoneContact phoneContact : DbContext.getInstance().getPhoneContacts(context)) {
            if (phoneContact.getNumber().contains(number)) {
                SuggestionDialer suggestionDialer = new SuggestionDialer(phoneContact.getName(), phoneContact.getNumber());

                if (!suggestionDialers.contains(suggestionDialer))
                    suggestionDialers.add(suggestionDialer);
            }
        }
        for (MyCallLogs.CallLog callLog : DbContext.getInstance().getMyCallLogs(context).getCallLogs()) {
            if (callLog.getPhoneNumber().contains(number)) {
                SuggestionDialer suggestionDialer = new SuggestionDialer(ContactUltils.instance.getContactName(callLog.getPhoneNumber(), context), callLog.getPhoneNumber());
                android.util.Log.d(TAG, "suggesDialer: " + suggestionDialers.contains(suggestionDialer));
                if (!suggestionDialers.contains(suggestionDialer))
                    suggestionDialers.add(suggestionDialer);

            }
        }
        ((BaseAdapter) lvSuggestion.getAdapter()).notifyDataSetChanged();
    }
//        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CONTACTS);
//            org.linphone.mediastream.Log.i("[Permission] Asking for " + Manifest.permission.WRITE_CONTACTS);
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_CONTACTS}, 0);
//        } else {
//            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
//
//            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
//
//            String contactName = null;
//            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
//                    contactName = cursor.getString(0);
//                }
//                cursor.close();
//            }
//        }


    /**
     * @return null if not ready yet
     */

    public static DialerFragment instance() {
        return instance;
    }

    @Override
    public void onPause() {
        instance = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        instance = this;

        if (LinphoneActivity.isInstanciated()) {
//			LinphoneActivity.instance().selectMenu(FragmentsAvailable.DIALER);
            LinphoneActivity.instance().updateDialerFragment(this);
            LinphoneActivity.instance().showStatusBar();
            LinphoneActivity.instance().hideTabBar(false);
        }

        boolean isOrientationLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//		if(isOrientationLandscape && !getResources().getBoolean(R.bool.isTablet)) {
//			((LinearLayout) numpad).setVisibility(View.GONE);
//		} else {
//			((LinearLayout) numpad).setVisibility(View.VISIBLE);
//		}

        if (shouldEmptyAddressField) {
//			mAddress.setText("");
        } else {
            shouldEmptyAddressField = true;
        }
        resetLayout(isCallTransferOngoing);

        String addressWaitingToBeCalled = LinphoneActivity.instance().mAddressWaitingToBeCalled;
        if (addressWaitingToBeCalled != null) {
            mAddress.setText(addressWaitingToBeCalled);
            if (getResources().getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
                newOutgoingCall(addressWaitingToBeCalled);
            }
            LinphoneActivity.instance().mAddressWaitingToBeCalled = null;
        }
    }

    public void resetLayout(boolean callTransfer) {
        if (!LinphoneActivity.isInstanciated()) {
            return;
        }
        isCallTransferOngoing = LinphoneActivity.instance().isCallTransfer();
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return;
        }

        if (lc.getCallsNb() > 0) {
            if (isCallTransferOngoing) {
                mCall.setImageResource(R.drawable.call_transfer);
                mCall.setExternalClickListener(transferListener);
            } else {
                mCall.setImageResource(R.drawable.call_add);
                mCall.resetClickListener();
            }
            mAddContact.setEnabled(true);
            mAddContact.setImageResource(R.drawable.call_alt_back);
            mAddContact.setOnClickListener(cancelListener);
        } else {
            if (LinphoneManager.getLc().getVideoAutoInitiatePolicy()) {
                mCall.setImageResource(R.drawable.call_video_start);
            } else {
                mCall.setImageResource(R.drawable.my_dialer_call);
            }
            mAddContact.setEnabled(false);
            mAddContact.setImageResource(R.drawable.contact_add_button);
            mAddContact.setOnClickListener(addContactListener);
            enableDisableAddContact();
        }
    }

    public void enableDisableAddContact() {
        mAddContact.setEnabled(LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null && LinphoneManager.getLc().getCallsNb() > 0 || !mAddress.getText().toString().equals(""));
    }

    public void displayTextInAddressBar(String numberOrSipAddress) {
        shouldEmptyAddressField = false;
        mAddress.setText(numberOrSipAddress);
    }

    public void newOutgoingCall(String numberOrSipAddress) {
        displayTextInAddressBar(numberOrSipAddress);
        LinphoneManager.getInstance().newOutgoingCall(mAddress);
    }

    public void newOutgoingCall(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String scheme = intent.getData().getScheme();
            if (scheme.startsWith("imto")) {
                mAddress.setText("sip:" + intent.getData().getLastPathSegment());
            } else if (scheme.startsWith("call") || scheme.startsWith("sip")) {
                mAddress.setText(intent.getData().getSchemeSpecificPart());
            } else {
                Uri contactUri = intent.getData();
                String address = ContactsManager.getAddressOrNumberForAndroidContact(LinphoneService.instance().getContentResolver(), contactUri);
                if (address != null) {
                    mAddress.setText(address);
                } else {
                    Log.e("Unknown scheme: ", scheme);
                    mAddress.setText(intent.getData().getSchemeSpecificPart());
                }
            }

            mAddress.clearDisplayedName();
            intent.setData(null);

            LinphoneManager.getInstance().newOutgoingCall(mAddress);
        }
    }

    class SuggestionAdapter extends BaseAdapter implements SectionIndexer {
        private Context context = null;

        private class ViewHolder {
            public TextView tvName;
            public TextView tvExt;
            public LinearLayout llSuggestion;

            //public ImageView friendStatus;

            public ViewHolder(View view) {
                tvName = view.findViewById(R.id.tv_name);
                tvExt = view.findViewById(R.id.tv_ext);
                llSuggestion = view.findViewById(R.id.ll_suggestion);
            }
        }

        private List<LinphoneContact> contacts;
        String[] sections;
        ArrayList<String> sectionsList;
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();


        public int getCount() {
            return suggestionDialers.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            ViewHolder holder = null;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = mInflater.inflate(R.layout.suggestion_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            final SuggestionDialer suggestionDialer = suggestionDialers.get(position);

            holder.tvName.setText(suggestionDialer.getName());

            //boi mau vao nhung so trung voi so tim kiem
            holder.tvExt.setText(suggestionDialer.getExt(), TextView.BufferType.SPANNABLE);
            String searchNumber = mAddress.getText().toString();
            Spannable s = (Spannable) holder.tvExt.getText();
            int startBuffColor = holder.tvExt.getText().toString().indexOf(searchNumber);
            int endBuffColor = startBuffColor + mAddress.getText().toString().length();
            s.setSpan(new ForegroundColorSpan(Color.GREEN), startBuffColor, endBuffColor, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.llSuggestion.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAddress.setText(suggestionDialer.getExt());
                }
            });

            return view;
        }


        @Override
        public Object[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            if (sectionIndex >= sections.length || sectionIndex < 0) {
                return 0;
            }
            return map.get(sections[sectionIndex]);
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position >= contacts.size() || position < 0) {
                return 0;
            }
            LinphoneContact contact = contacts.get(position);
            String fullName = contact.getFullName();
            if (fullName == null || fullName.isEmpty()) {
                return 0;
            }
            String letter = fullName.substring(0, 1).toUpperCase(Locale.getDefault());
            return sectionsList.indexOf(letter);
        }
    }

}
