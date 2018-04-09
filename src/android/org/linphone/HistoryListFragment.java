package org.linphone;

/*
HistoryListFragment.java
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.linphone.core.LinphoneAddress;
import org.linphone.database.DbContext;
import org.linphone.layoutXML.ExtendedEditText;
import org.linphone.ultils.ContactUltils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static org.linphone.FragmentsAvailable.HISTORY_LIST;

public class HistoryListFragment extends Fragment implements OnClickListener, OnItemClickListener, ContactsUpdatedListener {
    private ListView historyList;
    private LayoutInflater mInflater;
    private CheckBox deleteAll;

    private RelativeLayout deleteBar;
    private ArrayList<Integer> listPositionDelete = new ArrayList<>();
    private boolean isDeleteMode = false;
    private boolean isDeleteAll = false;
    private TextView missedCalls, allCalls, noCallHistory, noMissedCallHistory;
    private ImageView edit, selectAll, deselectAll, delete, cancel, backDeleteMode, deleteContact;
    private View allCallsSelected, missedCallsSelected;
    private LinearLayout editList, topBar;
    private boolean onlyDisplayMissedCalls, isEditMode;
    private List<MyCallLogs.CallLog> mLogs;
    private String TAG = "HistoryListFragment";
    private ImageView clearSearchField;
    private ExtendedEditText searchField;
    TextWatcher twAllCall = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            searchHistory(editable.toString());
        }
    };
    TextWatcher twMissedCall = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(final Editable editable) {
            searchHistory(editable.toString());
        }
    };


    private void searchHistory(String s) {

        mLogs.clear();
        ArrayList<MyCallLogs.CallLog> searchmLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();
        for (MyCallLogs.CallLog callLog : searchmLogs) {
            if (callLog.getPhoneNumber().toLowerCase(Locale.getDefault()).contains(s) || callLog.getName().toLowerCase(Locale.getDefault()).contains(s) ||
                    callLog.getPhoneNumber().toLowerCase(Locale.getDefault()).startsWith(s) || callLog.getName().toLowerCase(Locale.getDefault()).startsWith(s)) {
                mLogs.add(callLog);
            }
        }
        hideHistoryListAndDisplayMessageIfEmpty();
        if (onlyDisplayMissedCalls) {
            removeNotMissedCallsFromLogs();
            if (mLogs.size() == 0) noMissedCallHistory.setVisibility(View.VISIBLE);
        }
        try {
            ((BaseAdapter) historyList.getAdapter()).notifyDataSetChanged();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.history, container, false);

        searchField = view.findViewById(R.id.searchField);
        searchField.clearTextChangedListeners();
        searchField.addTextChangedListener(twAllCall);
        clearSearchField = (ImageView) view.findViewById(R.id.clearSearchField);
        clearSearchField.setOnClickListener(this);
        noCallHistory = (TextView) view.findViewById(R.id.no_call_history);
        noMissedCallHistory = (TextView) view.findViewById(R.id.no_missed_call_history);

        deleteBar = view.findViewById(R.id.delete_bar);
        deleteBar.setOnClickListener(this);

        backDeleteMode = view.findViewById(R.id.back_delete_mode);
        backDeleteMode.setOnClickListener(this);

        deleteContact = view.findViewById(R.id.delete_contact);
        deleteContact.setOnClickListener(this);

        deleteAll = view.findViewById(R.id.delete_all);
        deleteAll.setOnClickListener(this);

        historyList = (ListView) view.findViewById(R.id.history_list);
        historyList.setOnItemClickListener(this);

        delete = (ImageView) view.findViewById(R.id.delete);
        delete.setOnClickListener(this);

        editList = (LinearLayout) view.findViewById(R.id.edit_list);
        topBar = (LinearLayout) view.findViewById(R.id.top_bar);

        cancel = (ImageView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        allCalls = view.findViewById(R.id.all_calls);
        allCalls.setOnClickListener(this);

        allCallsSelected = view.findViewById(R.id.all_calls_select);

        missedCalls = view.findViewById(R.id.missed_calls);
        missedCalls.setOnClickListener(this);

        missedCallsSelected = view.findViewById(R.id.missed_calls_select);

        selectAll = (ImageView) view.findViewById(R.id.select_all);
        selectAll.setOnClickListener(this);

        deselectAll = (ImageView) view.findViewById(R.id.deselect_all);
        deselectAll.setOnClickListener(this);

        allCalls.setEnabled(false);
        onlyDisplayMissedCalls = false;

        edit = (ImageView) view.findViewById(R.id.edit);
        edit.setOnClickListener(this);

        return view;
    }

    public void refresh() {
        mLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();
    }

    private void selectAllList(boolean isSelectAll) {
        int size = historyList.getAdapter().getCount();
        for (int i = 0; i < size; i++) {
            historyList.setItemChecked(i, isSelectAll);
        }
    }

    public void displayFirstLog() {
//		if (mLogs != null && mLogs.size() > 0) {
//			LinphoneCallLog log = mLogs.get(0);
//			if (log.getDirection() == CallDirection.Incoming) {
//				LinphoneActivity.instance().displayHistoryDetail(mLogs.get(0).getFrom().toString(), mLogs.get(0));
//			} else {
//				LinphoneActivity.instance().displayHistoryDetail(mLogs.get(0).getTo().toString(), mLogs.get(0));
//			}
//		} else {
//			LinphoneActivity.instance().displayEmptyFragment();
//		}
    }


    public int getNbItemsChecked() {
        int size = historyList.getAdapter().getCount();
        int nb = 0;
        for (int i = 0; i < size; i++) {
            if (historyList.isItemChecked(i)) {
                nb++;
            }
        }
        return nb;
    }

    public void enabledDeleteButton(Boolean enabled) {
        if (enabled) {
            delete.setEnabled(true);
        } else {
            if (getNbItemsChecked() == 0) {
                delete.setEnabled(false);
            }
        }
    }

    private void removeNotMissedCallsFromLogs() {
        if (onlyDisplayMissedCalls) {
            List<MyCallLogs.CallLog> missedCalls = new ArrayList<MyCallLogs.CallLog>();
            for (MyCallLogs.CallLog log : mLogs) {
                if (log.getStatus() == MyCallLogs.CallLog.CUOC_GOI_NHO) {
                    missedCalls.add(log);
                }
            }
            mLogs = missedCalls;
        }
    }

    private boolean hideHistoryListAndDisplayMessageIfEmpty() {
        removeNotMissedCallsFromLogs();
        if (mLogs.isEmpty()) {
            if (onlyDisplayMissedCalls) {
                noMissedCallHistory.setVisibility(View.VISIBLE);
            } else {
                noCallHistory.setVisibility(View.VISIBLE);
            }
            historyList.setVisibility(View.GONE);
            edit.setEnabled(false);
            return true;
        } else {
            noCallHistory.setVisibility(View.GONE);
            noMissedCallHistory.setVisibility(View.GONE);
            historyList.setVisibility(View.VISIBLE);
            edit.setEnabled(true);
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactsManager.addContactsListener(this);
        // xoa notifi cuoc goi nho moi vao man hinh lich su cuoc goi
        try {
            LinphoneService.instance().mNM.cancel(LinphoneService.MISSED_NOTIF_ID);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.HISTORY_LIST);
            LinphoneActivity.instance().hideTabBar(false);
            LinphoneActivity.instance().displayMissedCalls(0);
        }

        mLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();
        try {
            searchHistory(searchField.getText().toString());
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        if (!hideHistoryListAndDisplayMessageIfEmpty()) {
            historyList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            historyList.setAdapter(new CallHistoryAdapter(getActivity().getApplicationContext()));
        }
    }

    @Override
    public void onPause() {
        ContactsManager.removeContactsListener(this);
        super.onPause();
    }

    @Override
    public void onContactsUpdated() {
        if (!LinphoneActivity.isInstanciated() || LinphoneActivity.instance().getCurrentFragment() != HISTORY_LIST)
            return;
        CallHistoryAdapter adapter = (CallHistoryAdapter) historyList.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.select_all) {
            deselectAll.setVisibility(View.VISIBLE);
            selectAll.setVisibility(View.GONE);
            enabledDeleteButton(true);
            selectAllList(true);
            return;
        }
        if (id == R.id.deselect_all) {
            deselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
            enabledDeleteButton(false);
            selectAllList(false);
            return;
        }

        if (id == R.id.back_delete_mode) {
            deleteBar.setVisibility(View.GONE);
            topBar.setVisibility(View.VISIBLE);
            backDeleteMode.setVisibility(View.GONE);
            deleteAll.setVisibility(View.GONE);
            listPositionDelete.clear();
            isDeleteMode = false;
            ((BaseAdapter) historyList.getAdapter()).notifyDataSetChanged();
            return;
        }

        if (id == R.id.delete_all) {
            isDeleteAll = !isDeleteAll;
            deleteAll.setChecked(isDeleteAll);

            if (!isDeleteAll) {
                listPositionDelete.clear();
                deleteContact.setVisibility(View.GONE);
            } else {
                deleteContact.setVisibility(View.VISIBLE);
                listPositionDelete.clear();
                for (int i = 0; i < DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs().size(); i++) {
                    listPositionDelete.add(i);
                }
            }

            ((BaseAdapter) historyList.getAdapter()).notifyDataSetChanged();
            return;
        }

        if (id == R.id.delete_bar) {

            return;
        }

        if (id == R.id.delete_contact) {
            try {
                MyCallLogs myCallLogs = new MyCallLogs();
                if (isDeleteAll) {

                    DbContext.getInstance().setMyCallLogs(myCallLogs, getActivity());

                } else {
                    ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();

                    for (int position : listPositionDelete) {
                        currentCallLogs.remove(position);
                    }
                    myCallLogs.setCallLogs(currentCallLogs);
                    DbContext.getInstance().setMyCallLogs(myCallLogs, getActivity());
                }
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            mLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();
            if (onlyDisplayMissedCalls) {
                removeNotMissedCallsFromLogs();
            }
            deleteAll.setChecked(false);
            hideHistoryListAndDisplayMessageIfEmpty();
            ((BaseAdapter) historyList.getAdapter()).notifyDataSetChanged();
            return;
        }

        if (id == R.id.cancel) {
            quitEditMode();
            return;
        }
        if (id == R.id.clearSearchField) {
            searchField.setText("");
            return;
        }

        if (id == R.id.delete) {
            if (historyList.getCheckedItemCount() == 0) {
                quitEditMode();
                return;
            }

            final Dialog dialog = LinphoneActivity.instance().displayDialog(getString(R.string.delete_text));
            Button delete = (Button) dialog.findViewById(R.id.delete_button);
            Button cancel = (Button) dialog.findViewById(R.id.cancel);
            dialog.show();
            return;
        }

        if (id == R.id.all_calls) {
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twAllCall);
            searchField.setText("");
            allCalls.setEnabled(false);
            allCallsSelected.setVisibility(View.VISIBLE);
            missedCallsSelected.setVisibility(View.INVISIBLE);
            missedCalls.setEnabled(true);
            onlyDisplayMissedCalls = false;
            refresh();
        }
        if (id == R.id.missed_calls) {
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twMissedCall);
            searchField.setText("");
            allCalls.setEnabled(true);
            allCallsSelected.setVisibility(View.INVISIBLE);
            missedCallsSelected.setVisibility(View.VISIBLE);
            missedCalls.setEnabled(false);
            onlyDisplayMissedCalls = true;
        }

        if (id == R.id.edit) {
            topBar.setVisibility(View.GONE);
            editList.setVisibility(View.VISIBLE);
            enabledDeleteButton(false);
            isEditMode = true;
        }

        if (!hideHistoryListAndDisplayMessageIfEmpty()) {
            historyList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            historyList.setAdapter(new CallHistoryAdapter(getActivity().getApplicationContext()));
        }

        if (isEditMode) {
            deselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (isEditMode) {
            MyCallLogs.CallLog log = mLogs.get(position);
//			LinphoneManager.getLc().removeCallLog(log);
//			mLogs = Arrays.asList(LinphoneManager.getLc().getCallLogs());
        }
    }

    public void quitEditMode() {
        isEditMode = false;
        editList.setVisibility(View.GONE);
        topBar.setVisibility(View.VISIBLE);

        if (!hideHistoryListAndDisplayMessageIfEmpty()) {
            historyList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            historyList.setAdapter(new CallHistoryAdapter(getActivity().getApplicationContext()));
        }
        if (getResources().getBoolean(R.bool.isTablet)) {
            displayFirstLog();
        }
    }

    class CallHistoryAdapter extends BaseAdapter {
        private class ViewHolder {
            public CheckBox cbxDelete;
            public TextView contact;
            public ImageView detail;
            public CheckBox select;
            public ImageView callDirection;
            public ImageView contactPicture;
            public LinearLayout CallContact;
            public TextView date;

            public ViewHolder(View view) {
                cbxDelete = view.findViewById(R.id.cbx_delete);
                contact = (TextView) view.findViewById(R.id.sip_uri);
                detail = (ImageView) view.findViewById(R.id.detail);
                select = (CheckBox) view.findViewById(R.id.delete);
                callDirection = (ImageView) view.findViewById(R.id.icon);
                contactPicture = (ImageView) view.findViewById(R.id.contact_picture);
                CallContact = (LinearLayout) view.findViewById(R.id.history_click);
                date = (TextView) view.findViewById(R.id.date_log);
            }
        }

        CallHistoryAdapter(Context aContext) {

        }

        public int getCount() {
            return mLogs.size();
        }

        public Object getItem(int position) {
            return mLogs.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SimpleDateFormat")
        private String timestampToHumanDate(Calendar cal) {
            SimpleDateFormat dateFormat;
            if (isToday(cal)) {
                return getString(R.string.today);
            } else if (isYesterday(cal)) {
                return getString(R.string.yesterday);
            } else {
                dateFormat = new SimpleDateFormat(getResources().getString(R.string.history_date_format));
            }

            return dateFormat.format(cal.getTime());
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            if (cal1 == null || cal2 == null) {
                return false;
            }

            return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
        }

        private boolean isToday(Calendar cal) {
            return isSameDay(cal, Calendar.getInstance());
        }

        private boolean isYesterday(Calendar cal) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.roll(Calendar.DAY_OF_MONTH, -1);
            return isSameDay(cal, yesterday);
        }


        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            ViewHolder holder = null;

            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = mInflater.inflate(R.layout.history_cell, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            if (mLogs == null || mLogs.size() < position) return view;
            final ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs();
            final MyCallLogs.CallLog log = mLogs.get(position);
            if (isDeleteMode) {
                holder.cbxDelete.setVisibility(View.VISIBLE);
                holder.detail.setVisibility(View.GONE);
            } else {
                holder.detail.setVisibility(View.VISIBLE);
                holder.cbxDelete.setVisibility(View.GONE);
            }
            final ViewHolder finalHolder = holder;
            holder.CallContact.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d(TAG, "onLongClick: ");

                    isDeleteMode = true;
                    changeMode(finalHolder, isDeleteMode);

                    return true;
                }
            });
            //check all delete
            if (isDeleteAll) {
                holder.cbxDelete.setChecked(isDeleteAll);
            }
            Log.d(TAG, "getPosition: " + currentCallLogs.indexOf(log));
            if (listPositionDelete.indexOf(currentCallLogs.indexOf(log)) != -1) {
                holder.cbxDelete.setChecked(true);
            } else {
                holder.cbxDelete.setChecked(false);
            }
            holder.cbxDelete.setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Log.d(TAG, "onCheckedChanged: ");
                                                        if (finalHolder.cbxDelete.isChecked()) {
                                                            finalHolder.cbxDelete.setChecked(true);
                                                            listPositionDelete.add(currentCallLogs.indexOf(log));
                                                            Log.d(TAG, "onCheckedChanged: " + listPositionDelete.toString());
                                                            notifyDataSetChanged();
                                                        } else {
                                                            finalHolder.cbxDelete.setChecked(false);
                                                            listPositionDelete.remove(listPositionDelete.indexOf(currentCallLogs.indexOf(log)));
                                                        }
                                                        if (listPositionDelete.size() == 0) {
                                                            deleteContact.setVisibility(View.GONE);
                                                        } else {
                                                            deleteContact.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }

            );

            long timestamp = log.getTime();
            LinphoneAddress address;
            holder.contact.setSelected(true); // For automated horizontal scrolling of long texts
            String callDate = String.valueOf(log.getTime());
            Long longDate = Long.parseLong(callDate);

            String datetime = LinphoneUtils.timestampToHumanDate(getActivity(), longDate, getString(R.string.history_detail_date_format));
//			if (datetime.length() > 0) {
//				datetime = datetime.substring(0, datetime.length() - 2);
//			}
            holder.date.setText(datetime);
            LinearLayout separator = (LinearLayout) view.findViewById(R.id.separator);
            TextView separatorText = (TextView) view.findViewById(R.id.separator_text);
            Calendar logTime = Calendar.getInstance();
            logTime.setTimeInMillis(timestamp);
            separatorText.setText(timestampToHumanDate(logTime));

            if (position > 0) {
                MyCallLogs.CallLog previousLog = mLogs.get(position - 1);
                long previousTimestamp = previousLog.getTime();
                Calendar previousLogTime = Calendar.getInstance();
                previousLogTime.setTimeInMillis(previousTimestamp);

                if (isSameDay(previousLogTime, logTime)) {
                    separator.setVisibility(View.GONE);
                } else {
                    separator.setVisibility(View.VISIBLE);
                }
            } else {
                separator.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "getView: " + log.getStatus());
            if (log.getStatus() == MyCallLogs.CallLog.CUOC_GOI_DEN) {
                holder.callDirection.setImageResource(R.drawable.my_incoming_call);
            } else if (log.getStatus() == MyCallLogs.CallLog.CUOC_GOI_DI) {
                holder.callDirection.setImageResource(R.drawable.my_outgoing_call);
            } else if (log.getStatus() == MyCallLogs.CallLog.CUOC_GOI_NHO) {
                holder.callDirection.setImageResource(R.drawable.my_missed_call);
            } else if (log.getStatus() == MyCallLogs.CallLog.MAY_BAN) {
                holder.callDirection.setImageResource(R.drawable.my_busy_call);
            } else if (log.getStatus() == MyCallLogs.CallLog.OFFLINE) {
                holder.callDirection.setImageResource(R.drawable.offline_ext);
            }

//
//			LinphoneContact c = ContactsManager.getInstance().findContactFromAddress(address);
//			if (c == null) c = ContactsManager.getInstance().findContactFromPhoneNumber(address.getUserName());
            String displayName = null;
//			Log.d(TAG, "getView: "+mLogs.get(0).getFrom().asString());
//			if (c != null) {
//				displayName = c.getFullName();
//				LinphoneUtils.setThumbnailPictureFromUri(LinphoneActivity.instance(), holder.contactPicture, c.getThumbnailUri());
//			} else {
            holder.contactPicture.setImageBitmap(ContactsManager.getInstance().getDefaultAvatarBitmap());
//			}
            displayName = ContactUltils.instance.getContactName(log.getPhoneNumber(), view.getContext());

            Log.d(TAG, "getView: " + displayName);
            if (displayName == null) {
                holder.contact.setText(LinphoneUtils.getAddressDisplayName(log.getPhoneNumber()));
            } else {
                holder.contact.setText(displayName);
            }

            if (isEditMode) {
                holder.CallContact.setOnClickListener(null);
                holder.select.setVisibility(View.VISIBLE);
                holder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        historyList.setItemChecked(position, b);
                        if (getNbItemsChecked() == getCount()) {
                            deselectAll.setVisibility(View.VISIBLE);
                            selectAll.setVisibility(View.GONE);
                            enabledDeleteButton(true);
                        } else {
                            if (getNbItemsChecked() == 0) {
                                deselectAll.setVisibility(View.GONE);
                                selectAll.setVisibility(View.VISIBLE);
                                enabledDeleteButton(false);
                            } else {
                                deselectAll.setVisibility(View.GONE);
                                selectAll.setVisibility(View.VISIBLE);
                                enabledDeleteButton(true);
                            }
                        }
                    }
                });
//                holder.detail.setVisibility(View.INVISIBLE);
                if (historyList.isItemChecked(position)) {
                    holder.select.setChecked(true);
                } else {
                    holder.select.setChecked(false);
                }
            } else {
                holder.select.setVisibility(View.GONE);
//                holder.detail.setVisibility(View.VISIBLE);
                holder.detail.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: 558");
                        if (LinphoneActivity.isInstanciated()) {
                            Log.d(TAG, "onClick: 560");
                            LinphoneActivity.instance().displayHistoryDetail(log.getPhoneNumber(), log);
                        }
                    }
                });
                final View finalView = view;
                holder.CallContact.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (LinphoneActivity.isInstanciated() && !isDeleteMode) {
                            String address;
                            address = "sip:" + log.getPhoneNumber() + "@" + LinphonePreferences.instance().getAccountDomain(0);
                            Log.d(TAG, "onClick: " + address);
                            LinphoneActivity.instance().setAddresGoToDialerAndCall(address, ContactUltils.instance.getContactName(log.getPhoneNumber(), finalView.getContext()), null);
                        }
                    }
                });
            }
            return view;
        }

    }

    private void changeMode(CallHistoryAdapter.ViewHolder holder, boolean isDeleteMode) {
        if (isDeleteMode) {
            deleteBar.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.GONE);
            backDeleteMode.setVisibility(View.VISIBLE);
            deleteAll.setVisibility(View.VISIBLE);

        }
        ((BaseAdapter) historyList.getAdapter()).notifyDataSetChanged();
    }
}