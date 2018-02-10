package org.linphone;

/*
ContactsListFragment.java
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


import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.linphone.database.DbContext;
import org.linphone.layoutXML.ExtendedEditText;
import org.linphone.myactivity.LoginActivity;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.ContactResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.linphone.FragmentsAvailable.CONTACTS_LIST;

public class ContactsListFragment extends Fragment implements OnClickListener, OnItemClickListener, ContactsUpdatedListener {
    private LayoutInflater mInflater;
    private ListView contactsList;
    private TextView allContacts, linphoneContacts, cusContacts, noSipContact, noContact;
    private ImageView newContact, edit, selectAll, deselectAll, delete, cancel;

    private boolean isEditMode, isSearchMode;
    public static int onlyDisplayLinphoneContacts;
    private View allContactsSelected, linphoneContactsSelected, cusContactSelected;
    private LinearLayout editList, topbar;
    private int lastKnownPosition;
    private boolean editOnClick = false, editConsumed = false, onlyDisplayChatAddress = false;
    private String sipAddressToAdd, displayName = null;
    private ImageView clearSearchField;
    private ExtendedEditText searchField;
    private ProgressBar contactsFetchInProgress;
    private String TAG = "ContactsListFragment";
    private int prelast;
    private Timer timer = new Timer();
    private int page = 1;
    private ProgressDialog dialogSearch;
    private String searchText = "";
    TextWatcher twLocal = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            searchContacts(searchField.getText().toString());
        }
    };
    TextWatcher twToda = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (timer != null)
                timer.cancel();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(final Editable editable) {
            searchText = editable.toString();
            Log.d(TAG, "afterTextChanged: " + editable.toString());
            page = 1;
            try {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogSearch = ProgressDialog.show(getActivity(), "", "Đang tìm kiếm...", true, false);
                                String urlContact;
                                if (onlyDisplayLinphoneContacts == 1)
                                    urlContact = "AppDanhBa.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                                            + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
                                else
                                    urlContact = "AppDanhBaKhachHang.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                                            + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
                                android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
                                Service service = NetContext.getInstance().create(Service.class);
                                service.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                                    @Override
                                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                                        try {
                                            dialogSearch.cancel();
                                        } catch (Exception e) {

                                        }
                                        ContactResponse contactResponse;
                                        contactResponse = response.body();
                                        if (contactResponse.getStatus()) {
                                            ArrayList<ContactResponse.DSDanhBa> listDB = contactResponse.getDsdanhba();
                                            try {
                                                DbContext.getInstance().setContactResponse(contactResponse, getActivity());
                                            } catch (Exception e) {

                                            }

                                            ((ContactsListAdapter) contactsList.getAdapter()).notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ContactResponse> call, Throwable t) {
                                        try {
                                            dialogSearch.cancel();
                                        } catch (Exception e) {

                                        }
                                    }
                                });
                            }
                        });

                    }
                }, 1000);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e);
            }


        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mInflater = inflater;
        View view = inflater.inflate(R.layout.contacts_list, container, false);
        try {
            Log.d(TAG, "onCreateView: onCreate");
            page = 1;
            searchText = "";
            if (getArguments() != null) {
                editOnClick = getArguments().getBoolean("EditOnClick");
                sipAddressToAdd = getArguments().getString("SipAddress");
                if (getArguments().getString("DisplayName") != null)
                    displayName = getArguments().getString("DisplayName");
                onlyDisplayChatAddress = getArguments().getBoolean("ChatAddressOnly");
            }
//            getContactToda();
            noSipContact = (TextView) view.findViewById(R.id.noSipContact);
            noContact = (TextView) view.findViewById(R.id.noContact);

            contactsList = (ListView) view.findViewById(R.id.contactsList);
            contactsList.setOnItemClickListener(this);

            allContacts = view.findViewById(R.id.all_contacts);
            allContacts.setOnClickListener(this);

            linphoneContacts = (view.findViewById(R.id.linphone_contacts));
            linphoneContacts.setOnClickListener(this);

            cusContacts = view.findViewById(R.id.cus_contacts);
            cusContacts.setOnClickListener(this);

            allContactsSelected = view.findViewById(R.id.all_contacts_select);
            linphoneContactsSelected = view.findViewById(R.id.linphone_contacts_select);
            cusContactSelected = view.findViewById(R.id.cus_contacts_select);

            newContact = (ImageView) view.findViewById(R.id.newContact);
            newContact.setOnClickListener(this);
            newContact.setEnabled(LinphoneManager.getLc().getCallsNb() == 0);

            allContacts.setEnabled(true);
            linphoneContacts.setEnabled(!allContacts.isEnabled());

            selectAll = (ImageView) view.findViewById(R.id.select_all);
            selectAll.setOnClickListener(this);

            deselectAll = (ImageView) view.findViewById(R.id.deselect_all);
            deselectAll.setOnClickListener(this);

            delete = (ImageView) view.findViewById(R.id.delete);
            delete.setOnClickListener(this);

            editList = (LinearLayout) view.findViewById(R.id.edit_list);
            topbar = (LinearLayout) view.findViewById(R.id.top_bar);

            cancel = (ImageView) view.findViewById(R.id.cancel);
            cancel.setOnClickListener(this);

            edit = (ImageView) view.findViewById(R.id.edit);
            edit.setOnClickListener(this);

            clearSearchField = (ImageView) view.findViewById(R.id.clearSearchField);
            clearSearchField.setOnClickListener(this);

            searchField = view.findViewById(R.id.searchField);
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twLocal);
            contactsFetchInProgress = (ProgressBar) view.findViewById(R.id.contactsFetchInProgress);
            contactsFetchInProgress.setVisibility(View.VISIBLE);
            contactsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    int lastItem = i1 + i;
                    Log.d(TAG, "onScroll: lastItem la " + lastItem);
                    Log.d(TAG, "onScroll: i2 la " + i2);
                    if (lastItem == i2) {
                        if (prelast != lastItem) {
                            Log.d(TAG, "onScroll: last");
                            prelast = lastItem;
//                        getConta try {    ctToda();
                        }
                    }

                }
            });
        } catch (Exception e) {

        }
        return view;
    }

    public int getNbItemsChecked() {
        int size = contactsList.getAdapter().getCount();
        int nb = 0;
        for (int i = 0; i < size; i++) {
            if (contactsList.isItemChecked(i)) {
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

    public void getContactToda() {
        try {

            Service contactService = NetContext.instance.create(Service.class);
            String urlContact;
            if (onlyDisplayLinphoneContacts == 1)
                urlContact = "AppDanhBa.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
            else
                urlContact = "AppDanhBaKhachHang.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
            android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
            contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                @Override
                public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                    ContactResponse contactResponse = new ContactResponse();
                    contactResponse = response.body();
                    if (contactResponse.getStatus()) {
                        Log.d(TAG, "onResponse: " + page);
                        if (page == 1) {
                            Log.d(TAG, "onResponse:" + DbContext.getInstance());
                            try {
                                DbContext.getInstance().setContactResponse(contactResponse, getActivity());
                            } catch (Exception e) {

                            }

                        } else {
                            try {
                                ContactResponse currentContactResponse = DbContext.getInstance().getContactResponse(getActivity());
                                ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContactResponse.getDsdanhba();
                                for (ContactResponse.DSDanhBa ds : contactResponse.getDsdanhba()) {
                                    dsDanhBas.add(ds);
                                }
                                currentContactResponse.setDsdanhba(dsDanhBas);
                                DbContext.getInstance().setContactResponse(currentContactResponse, getActivity());
                            } catch (Exception e) {

                            }

                        }

                        ((ContactsListAdapter) contactsList.getAdapter()).notifyDataSetChanged();

//                        page++;
                    }
                }

                @Override
                public void onFailure(Call<ContactResponse> call, Throwable t) {
                    try {
                        Toast.makeText(getActivity(),
                                "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }
                }

            });
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "onClick: 333");
        if (id == R.id.select_all) {
            Log.d(TAG, "onClick: 335");
            deselectAll.setVisibility(View.VISIBLE);
            selectAll.setVisibility(View.GONE);
            enabledDeleteButton(true);
            selectAllList(true);
            return;
        }
        if (id == R.id.deselect_all) {
            Log.d(TAG, "onClick: 343");
            deselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
            enabledDeleteButton(false);
            selectAllList(false);
            return;
        }

        if (id == R.id.cancel) {
            Log.d(TAG, "onClick: 352");
            quitEditMode();
            return;
        }

        if (id == R.id.delete) {
            Log.d(TAG, "onClick: 358");
            final Dialog dialog = LinphoneActivity.instance().displayDialog(getString(R.string.delete_text));
            Button delete = (Button) dialog.findViewById(R.id.delete_button);
            Button cancel = (Button) dialog.findViewById(R.id.cancel);

            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: 366");
                    removeContacts();
                    dialog.dismiss();
                    quitEditMode();
                }
            });

            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: 376");
                    dialog.dismiss();
                    quitEditMode();
                }
            });
            dialog.show();
            return;
        }

        if (id == R.id.edit) {
            Log.d(TAG, "onClick: 386");
            editList.setVisibility(View.VISIBLE);
            topbar.setVisibility(View.GONE);
            enabledDeleteButton(false);
            isEditMode = true;
        }

        if (id == R.id.all_contacts) {
            Log.d(TAG, "onClick: 394");
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twLocal);
            searchField.setText("");
            onlyDisplayLinphoneContacts = 0;
            allContactsSelected.setVisibility(View.VISIBLE);
            allContacts.setEnabled(false);
            allContacts.setTextColor(Color.parseColor("#ffa645"));
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setEnabled(true);
            cusContacts.setEnabled(true);
            cusContactSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);

        } else if (id == R.id.linphone_contacts) {
            searchField.setText("");
            searchText = "";
            try {
                dialogSearch = ProgressDialog.show(getActivity(), "", "Đang tải...", true, false);
                Service contactService = NetContext.instance.create(Service.class);
                String urlContact = "AppDanhBa.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
                android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
                contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                    @Override
                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                        ContactResponse contactResponse = new ContactResponse();
                        contactResponse = response.body();
                        try {
                            dialogSearch.cancel();
                        } catch (Exception e) {

                        }
                        if (contactResponse.getStatus()) {
                            Log.d(TAG, "onResponse: " + page);
                            if (page == 1) {

                                Log.d(TAG, "onResponse:" + DbContext.getInstance());
                                try {
                                    DbContext.getInstance().setContactResponse(contactResponse, getActivity());
                                } catch (Exception e) {

                                }

                            } else {
                                try {
                                    ContactResponse currentContactResponse = DbContext.getInstance().getContactResponse(getActivity());
                                    ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContactResponse.getDsdanhba();
                                    for (ContactResponse.DSDanhBa ds : contactResponse.getDsdanhba()) {
                                        dsDanhBas.add(ds);
                                    }
                                    currentContactResponse.setDsdanhba(dsDanhBas);
                                    DbContext.getInstance().setContactResponse(currentContactResponse, getActivity());
                                } catch (Exception e) {

                                }

                            }

                            ((ContactsListAdapter) contactsList.getAdapter()).notifyDataSetChanged();

//                        page++;
                        }
                    }

                    @Override
                    public void onFailure(Call<ContactResponse> call, Throwable t) {
                        try {
                            dialogSearch.cancel();
                        } catch (Exception e) {

                        }
                        try {
                            Toast.makeText(getActivity(),
                                    "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {

                        }
                    }

                });
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception: " + e);
            }
            allContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setTextColor(Color.parseColor("#ffa645"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twToda);
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.VISIBLE);
            linphoneContacts.setEnabled(false);
            allContacts.setEnabled(true);
            cusContacts.setEnabled(true);
            cusContactSelected.setVisibility(View.INVISIBLE);
            onlyDisplayLinphoneContacts = 1;

        } else if (id == R.id.cus_contacts) {
            searchField.setText("");
            searchText = "";
            try {
                dialogSearch = ProgressDialog.show(getActivity(), "", "Đang tải...", true, false);
                Service contactService = NetContext.instance.create(Service.class);
                String urlContact = "AppDanhBaKhachHang.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&page=" + page + "&timkiem=" + searchText;//lay tat ca danh ba ra
                android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
                contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                    @Override
                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                        ContactResponse contactResponse = new ContactResponse();
                        contactResponse = response.body();
                        try {
                            dialogSearch.cancel();
                        } catch (Exception e) {

                        }
                        if (contactResponse.getStatus()) {
                            Log.d(TAG, "onResponse: " + page);
                            if (page == 1) {

                                Log.d(TAG, "onResponse:" + DbContext.getInstance());
                                try {
                                    DbContext.getInstance().setContactResponse(contactResponse, getActivity());
                                } catch (Exception e) {

                                }

                            } else {
                                try {
                                    ContactResponse currentContactResponse = DbContext.getInstance().getContactResponse(getActivity());
                                    ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContactResponse.getDsdanhba();
                                    for (ContactResponse.DSDanhBa ds : contactResponse.getDsdanhba()) {
                                        dsDanhBas.add(ds);
                                    }
                                    currentContactResponse.setDsdanhba(dsDanhBas);
                                    DbContext.getInstance().setContactResponse(currentContactResponse, getActivity());
                                } catch (Exception e) {

                                }

                            }

                            ((ContactsListAdapter) contactsList.getAdapter()).notifyDataSetChanged();

//                        page++;
                        }
                    }

                    @Override
                    public void onFailure(Call<ContactResponse> call, Throwable t) {
                        try {
                            dialogSearch.cancel();
                        } catch (Exception e) {

                        }
                        try {
                            Toast.makeText(getActivity(),
                                    "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {

                        }
                    }

                });
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception: " + e);
            }
            allContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffa645"));
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twToda);
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);
            cusContacts.setEnabled(false);
            cusContactSelected.setVisibility(View.VISIBLE);
            linphoneContacts.setEnabled(true);
            allContacts.setEnabled(true);
            onlyDisplayLinphoneContacts = 2;
        }

        if (isEditMode) {
            Log.d(TAG, "onClick: 418");
            deselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
        }

        if (searchField.getText().toString().length() > 0) {
            Log.d(TAG, "onClick: 424");
//            searchContacts();
        } else {
            Log.d(TAG, "onClick: 427");
            changeContactsAdapter();
        }

        if (id == R.id.newContact) {
            Log.d(TAG, "onClick: 432");
            editConsumed = true;
            if (displayName != null)
                LinphoneActivity.instance().addContact(displayName, sipAddressToAdd);
            else
                LinphoneActivity.instance().addContact(null, sipAddressToAdd);
        } else if (id == R.id.clearSearchField) {
            Log.d(TAG, "onClick: 439");
            searchField.setText("");
        }
    }

    private void selectAllList(boolean isSelectAll) {
        int size = contactsList.getAdapter().getCount();
        for (int i = 0; i < size; i++) {
            contactsList.setItemChecked(i, isSelectAll);
        }
    }

    private void removeContacts() {
        ArrayList<String> ids = new ArrayList<String>();
        int size = contactsList.getAdapter().getCount();

        for (int i = size - 1; i >= 0; i--) {
            if (contactsList.isItemChecked(i)) {
                LinphoneContact contact = (LinphoneContact) contactsList.getAdapter().getItem(i);
                if (contact.isAndroidContact()) {
                    contact.deleteFriend();
                    ids.add(contact.getAndroidId());
                } else {
                    contact.delete();
                }
            }
        }

        ContactsManager.getInstance().deleteMultipleContactsAtOnce(ids);
    }

    public void quitEditMode() {
        isEditMode = false;
        editList.setVisibility(View.GONE);
        topbar.setVisibility(View.VISIBLE);
        invalidate();
        if (getResources().getBoolean(R.bool.isTablet)) {
            displayFirstContact();
        }
    }

    public void displayFirstContact() {
        if (contactsList != null && contactsList.getAdapter() != null && contactsList.getAdapter().getCount() > 0) {
            LinphoneActivity.instance().displayContact((LinphoneContact) contactsList.getAdapter().getItem(0), false);
        } else {
            LinphoneActivity.instance().displayEmptyFragment();
        }
    }

    private void searchContacts() {
        searchContacts(searchField.getText().toString());
    }

    private void searchContacts(String search) {
        if (search == null || search.length() == 0) {
            changeContactsAdapter();
            return;
        }
        changeContactsToggle();

        isSearchMode = true;
        contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getContacts(search)));

//		if (onlyDisplayLinphoneContacts) {
//			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//			contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getSIPContacts(search)));
//		} else {
//			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//			contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getContacts(search)));
//		}
    }

    private void changeContactsAdapter() {
        changeContactsToggle();

        isSearchMode = false;
        noSipContact.setVisibility(View.GONE);
        noContact.setVisibility(View.GONE);
        contactsList.setVisibility(View.VISIBLE);

        ContactsListAdapter adapter;
        contactsList.setFastScrollEnabled(false);
        contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        switch (onlyDisplayLinphoneContacts) {
            case 0:
                adapter = new ContactsListAdapter(ContactsManager.getInstance().getContacts());
                break;
            case 1:
                adapter = new ContactsListAdapter(ContactsManager.getInstance().getContacts(), getActivity());
                break;
            default:
                adapter = new ContactsListAdapter(ContactsManager.getInstance().getContacts(), getActivity());
                break;
        }

        contactsList.setAdapter(adapter);
        edit.setEnabled(true);
//		if (onlyDisplayLinphoneContacts) {
//			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//			adapter = new ContactsListAdapter(ContactsManager.getInstance().getSIPContacts());
//			contactsList.setAdapter(adapter);
//			edit.setEnabled(true);
//		} else {
//			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//			adapter = new ContactsListAdapter(ContactsManager.getInstance().getContacts());
//			contactsList.setAdapter(adapter);
//			edit.setEnabled(true);
//		}
        contactsList.setFastScrollEnabled(true);
        adapter.notifyDataSetInvalidated();


        if (adapter.getCount() > 0) {
            contactsFetchInProgress.setVisibility(View.GONE);
        }
        if (onlyDisplayLinphoneContacts == 0) {
            ContactsManager.getInstance().setLinphoneContactsPrefered(false);
        } else {
            ContactsManager.getInstance().setLinphoneContactsPrefered(true);
        }

    }

    private void changeContactsToggle() {
        if (onlyDisplayLinphoneContacts == 0) {
            allContacts.setEnabled(false);
            allContactsSelected.setVisibility(View.VISIBLE);
            linphoneContacts.setEnabled(true);
            cusContacts.setEnabled(true);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);
            cusContactSelected.setVisibility(View.INVISIBLE);
            allContacts.setTextColor(Color.parseColor("#ffa645"));
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));

        } else if (onlyDisplayLinphoneContacts == 1) {
            allContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setTextColor(Color.parseColor("#ffa645"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));
            allContacts.setEnabled(true);
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContacts.setEnabled(false);
            linphoneContactsSelected.setVisibility(View.VISIBLE);
            cusContacts.setEnabled(true);
            cusContactSelected.setVisibility(View.INVISIBLE);
        } else if (onlyDisplayLinphoneContacts == 2) {
            allContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffa645"));
            allContacts.setEnabled(true);
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContacts.setEnabled(true);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);
            cusContacts.setEnabled(false);
            cusContactSelected.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        LinphoneContact contact = (LinphoneContact) adapter.getItemAtPosition(position);
        if (editOnClick) {
            editConsumed = true;
            LinphoneActivity.instance().editContact(contact, sipAddressToAdd);
        } else {
            lastKnownPosition = contactsList.getFirstVisiblePosition();
            LinphoneActivity.instance().displayContact(contact, onlyDisplayChatAddress);
        }
    }

    @Override
    public void onResume() {

        ContactsManager.addContactsListener(this);
        super.onResume();

        if (editConsumed) {
            editOnClick = false;
            sipAddressToAdd = null;
        }

        if (searchField != null && searchField.getText().toString().length() > 0) {
            if (contactsFetchInProgress != null) contactsFetchInProgress.setVisibility(View.GONE);
        }

        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.CONTACTS_LIST);
            LinphoneActivity.instance().hideTabBar(false);
        }
        changeContactsToggle();
        invalidate();
    }

    @Override
    public void onPause() {
        ContactsManager.removeContactsListener(this);
        super.onPause();
    }

    @Override
    public void onContactsUpdated() {
        if (!LinphoneActivity.isInstanciated() || LinphoneActivity.instance().getCurrentFragment() != CONTACTS_LIST)
            return;
        ContactsListAdapter adapter = (ContactsListAdapter) contactsList.getAdapter();
        if (adapter != null) {
            contactsList.setFastScrollEnabled(false);
            adapter.updateDataSet(ContactsManager.getInstance().getContacts());
//			if (onlyDisplayLinphoneContacts) {
//				adapter.updateDataSet(ContactsManager.getInstance().getSIPContacts());
//			} else {
//				adapter.updateDataSet(ContactsManager.getInstance().getContacts());
//			}
            contactsList.setFastScrollEnabled(true);
            contactsFetchInProgress.setVisibility(View.GONE);
        }
    }

    public void invalidate() {
        if (searchField != null && searchField.getText().toString().length() > 0) {
            searchContacts(searchField.getText().toString());
        } else {
            changeContactsAdapter();
        }
        contactsList.setSelectionFromTop(lastKnownPosition, 0);
    }

    class ContactsListAdapter extends BaseAdapter implements SectionIndexer {
        private Context context = null;

        private class ViewHolder {
            public CheckBox delete;
            public ImageView linphoneFriend;
            public TextView name;
            public LinearLayout separator;
            public TextView separatorText;
            public ImageView contactPicture;
            public TextView organization;
            public TextView address;
            public ImageButton imgCall;
            //public ImageView friendStatus;

            public ViewHolder(View view) {
                delete = (CheckBox) view.findViewById(R.id.delete);
                linphoneFriend = (ImageView) view.findViewById(R.id.friendLinphone);
                name = (TextView) view.findViewById(R.id.name);
                separator = (LinearLayout) view.findViewById(R.id.separator);
                separatorText = (TextView) view.findViewById(R.id.separator_text);
                contactPicture = (ImageView) view.findViewById(R.id.contact_picture);
                organization = (TextView) view.findViewById(R.id.contactOrganization);
                address = (TextView) view.findViewById(R.id.address);
                imgCall = (ImageButton) view.findViewById(R.id.secondary_action_icon);
                //friendStatus = (ImageView) view.findViewById(R.id.friendStatus);
            }
        }

        private List<LinphoneContact> contacts;
        String[] sections;
        ArrayList<String> sectionsList;
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();

        ContactsListAdapter(List<LinphoneContact> contactsList) {
            updateDataSet(contactsList);
        }

        ContactsListAdapter(List<LinphoneContact> contactsList, Context context) {
            updateDataSet(contactsList);
            this.context = context;
        }

        public void updateDataSet(List<LinphoneContact> contactsList) {
            contacts = contactsList;

            map = new LinkedHashMap<String, Integer>();
            String prevLetter = null;
            for (int i = 0; i < contacts.size(); i++) {
                LinphoneContact contact = contacts.get(i);
                String fullName = contact.getFullName();
                if (fullName == null || fullName.isEmpty()) {
                    continue;
                }
                String firstLetter = fullName.substring(0, 1).toUpperCase(Locale.getDefault());
                if (!firstLetter.equals(prevLetter)) {
                    prevLetter = firstLetter;
                    map.put(firstLetter, i);
                }
            }
            sectionsList = new ArrayList<String>(map.keySet());
            sections = new String[sectionsList.size()];
            sectionsList.toArray(sections);

            notifyDataSetChanged();
        }

        public int getCount() {
            switch (ContactsListFragment.onlyDisplayLinphoneContacts) {
                case 0:
                    return contacts.size();

                case 1:
                    try {
                        return DbContext.getInstance().getContactResponse(context).getDsdanhba().size();
                    } catch (Exception e) {
                        return 0;
                    }
                default:
                    try {
                        return DbContext.getInstance().getContactResponse(context).getDsdanhba().size();
                    } catch (Exception e) {
                        return 0;
                    }

            }

        }

        public Object getItem(int position) {
            if (position >= getCount()) return null;
            try {
                return contacts.get(position);
            } catch (Exception e) {
                return null;
            }

        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            final LinphoneContact contact = (LinphoneContact) getItem(position);
            if (contact == null) return null;
            ViewHolder holder = null;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = mInflater.inflate(R.layout.contact_cell, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            holder.imgCall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onlyDisplayLinphoneContacts == 0) {
                        String uri = "sip:" + contacts.get(position).getNumbersOrAddresses().get(0).getValue() + "@" + LinphonePreferences.instance().getAccountDomain(0);
                        LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, contacts.get(position).getFullName(), null);
                    } else {
                        try {
                            ContactResponse.DSDanhBa to = DbContext.getInstance().getContactResponse(getActivity()).getDsdanhba().get(position);
                            String uri = "sip:" + to.getSodienthoai() + "@" + LinphonePreferences.instance().getAccountDomain(0);
                            LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, to.getTenlienhe(), null);
                        } catch (Exception e) {

                        }

                    }
                }
            });
            if (onlyDisplayLinphoneContacts == 0) {
                holder.name.setText(contact.getFullName());
                holder.address.setVisibility(View.GONE);
                holder.organization.setVisibility(View.GONE);
            } else {
                try {
                    holder.imgCall.setColorFilter(Color.parseColor(DbContext.getInstance().getContactResponse(view.getContext()).getDsdanhba().get(position).getMamau()));
                    holder.name.setText(DbContext.getInstance().getContactResponse(view.getContext()).getDsdanhba().get(position).getTenlienhe());
                    holder.address.setVisibility(View.VISIBLE);
                    holder.address.setText(DbContext.getInstance().getContactResponse(view.getContext()).getDsdanhba().get(position).getSodienthoai());
                    holder.organization.setText(DbContext.getInstance().getContactResponse(view.getContext()).getDsdanhba().get(position).getJob());
                } catch (Exception e) {

                }

            }
            if (!isSearchMode) {
                if (getPositionForSection(getSectionForPosition(position)) != position) {
                    holder.separator.setVisibility(View.GONE);
                } else {
                    holder.separator.setVisibility(View.GONE);
                    String fullName = "";
                    if (onlyDisplayLinphoneContacts == 0)
                        fullName = contact.getFullName();
                    else
                        try {
                            fullName = DbContext.getInstance().getContactResponse(context).getDsdanhba().get(position).getTenlienhe();
                        } catch (Exception e) {

                        }
                    if (fullName != null && !fullName.isEmpty()) {
                        holder.separatorText.setVisibility(View.GONE);
                    }
                }
            } else {
                holder.separator.setVisibility(View.GONE);
            }

            if (contact.isInLinphoneFriendList()) {
                holder.linphoneFriend.setVisibility(View.VISIBLE);
            } else {
                holder.linphoneFriend.setVisibility(View.GONE);
            }

            holder.contactPicture.setImageBitmap(ContactsManager.getInstance().getDefaultAvatarBitmap());
            if (contact.hasPhoto()) {
                LinphoneUtils.setThumbnailPictureFromUri(LinphoneActivity.instance(), holder.contactPicture, contact.getThumbnailUri());
            }

            boolean isOrgVisible = getResources().getBoolean(R.bool.display_contact_organization);
            String org = contact.getOrganization();
//            if (org != null && !org.isEmpty() && isOrgVisible) {
//                holder.organization.setText(org);
//                holder.organization.setVisibility(View.VISIBLE);
//            } else {
//                holder.organization.setVisibility(View.GONE);
//            }

            if (isEditMode) {
                holder.delete.setVisibility(View.VISIBLE);
                holder.delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        contactsList.setItemChecked(position, b);
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
                if (contactsList.isItemChecked(position)) {
                    holder.delete.setChecked(true);
                } else {
                    holder.delete.setChecked(false);
                }
            } else {
                holder.delete.setVisibility(View.INVISIBLE);
            }

			/*LinphoneFriend[] friends = LinphoneManager.getLc().getFriendList();
            if (!ContactsManager.getInstance().isContactPresenceDisabled() && friends != null) {
				holder.friendStatus.setVisibility(View.VISIBLE);
				PresenceActivityType presenceActivity = friends[0].getPresenceModel().getActivity().getType();
				if (presenceActivity == PresenceActivityType.Online) {
					holder.friendStatus.setImageResource(R.drawable.led_connected);
				} else if (presenceActivity == PresenceActivityType.Busy) {
					holder.friendStatus.setImageResource(R.drawable.led_error);
				} else if (presenceActivity == PresenceActivityType.Away) {
					holder.friendStatus.setImageResource(R.drawable.led_inprogress);
				} else if (presenceActivity == PresenceActivityType.Offline) {
					holder.friendStatus.setImageResource(R.drawable.led_disconnected);
				} else {
					holder.friendStatus.setImageResource(R.drawable.call_quality_indicator_0);
				}
			}*/

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
