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


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.linphone.database.DbContext;
import org.linphone.layoutXML.ExtendedEditText;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.NonTodaContactsResponse;
import org.linphone.network.models.VoidRespon;
import org.linphone.ultils.ContactUltils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.linphone.FragmentsAvailable.CONTACTS_LIST;

public class NonTodaContacts extends Activity implements OnClickListener, OnItemClickListener, ContactsUpdatedListener, SwipeRefreshLayout.OnRefreshListener {
    private LayoutInflater mInflater;
    private ListView contactsList;
    private TextView allContacts, linphoneContacts, cusContacts, noSipContact, noContact;
    private ImageView newContact, edit, selectAll, deselectAll, delete, cancel, backDeleteMode, deleteContact;
    private RelativeLayout rlCusContact, rlLocalContact, rlTodaContact;
    private RelativeLayout rlNoResult, rlContact;
    private SwipeRefreshLayout refreshLayout;
    private boolean isEditMode, isSearchMode;

    public static int onlyDisplayLinphoneContacts;
    private View allContactsSelected, linphoneContactsSelected, cusContactSelected;
    private LinearLayout editList, topbar;
    private RelativeLayout deleteBar;
    private int lastKnownPosition;
    private boolean editOnClick = false, editConsumed = false, onlyDisplayChatAddress = false;
    private String sipAddressToAdd, displayName = null;
    private ImageView clearSearchField, backImg;
    private ExtendedEditText searchField;
    private CheckBox deleteAll;
    private ImageView completeAddBtn;
    private ProgressBar contactsFetchInProgress;
    private String TAG = "NonTodaContacts";

    private Timer timer = new Timer();
    private int lastID = 0;
    private ProgressDialog dialogSearch;
    private String searchText = "";
    private boolean isDeleteMode = false;
    private boolean isDeleteAll = false;
    private ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> listAddContacts = new ArrayList<>();
    private ArrayList<Integer> listIdAddContacts = new ArrayList<>();
    TextWatcher twToda = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            lastID = 0;
            if (timer != null)
                timer.cancel();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(final Editable editable) {
            searchText = editable.toString();
            try {
                searchNonTodaContacts(searchText);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e);
            }


        }
    };


    private boolean listIsAtTop() {
        if (contactsList.getChildCount() == 0) return true;
        return contactsList.getChildAt(0).getTop() == 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_toda_contacts2);
        mInflater = getLayoutInflater();

        try {
            lastID = 0;
            searchText = "";
            rlContact = findViewById(R.id.rl_contactlist);
            clearSearchField = findViewById(R.id.clearSearchFieldNonToda);
            rlNoResult = findViewById(R.id.rl_no_result);
            rlTodaContact = findViewById(R.id.rl_toda_contact);
            refreshLayout = findViewById(R.id.refresh_layout);
            completeAddBtn = findViewById(R.id.complete_add_contact);
            searchField = findViewById(R.id.searchField);
            backImg = findViewById(R.id.back_add_contact);
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twToda);
            contactsFetchInProgress = (ProgressBar) findViewById(R.id.contactsFetchInProgress);
            contactsList = (ListView) findViewById(R.id.contactsList);
            refreshLayout.setOnRefreshListener(this);
            completeAddBtn.setOnClickListener(this);
            clearSearchField.setOnClickListener(this);
            backImg.setOnClickListener(this);
            contactsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    try {
                        Log.d(TAG, "onScroll: " + listIsAtTop());
                        refreshLayout.setEnabled(listIsAtTop());
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "onCreateView: " + e);
        }

        changeAdapter();

    }


    private void searchNonTodaContacts(String s) {
        searchText = s;
        s = ContactUltils.instance.removeAccents(s);
        ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> dsDanhBa = null;
        ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> listSearchDanhBa = new ArrayList<>();
        dsDanhBa = DbContext.getInstance().getNonTodaContactsResponse(this).getDsdanhba();

        for (NonTodaContactsResponse.DSDanhBaNonToda ds : dsDanhBa) {
            if (ContactUltils.instance.removeAccents(ds.getSodienthoai()).contains(s) ||
                    ContactUltils.instance.removeAccents(ds.getChucvu()).contains(s) ||
                    ContactUltils.instance.removeAccents(ds.getTennhanvien()).contains(s) ||
                    ContactUltils.instance.removeAccents(ds.getSodienthoai()).startsWith(s) ||
                    ContactUltils.instance.removeAccents(ds.getChucvu()).startsWith(s) ||
                    ContactUltils.instance.removeAccents(ds.getTennhanvien()).startsWith(s)) {

                listSearchDanhBa.add(ds);
            }
        }
        NonTodaContactsResponse currentSearchResponse = DbContext.getInstance().getSearchNonTodaContactResponse(this);
        Log.d(TAG, "searchNonTodaContacts: " + currentSearchResponse);
        currentSearchResponse.setDsdanhba(listSearchDanhBa);
        DbContext.getInstance().setSearchNonTodaContactResponse(currentSearchResponse, this);
        changeAdapter();
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


    public void changeAdapter() {

        NonTodaContactsAdapter adapter;
        contactsList.setFastScrollEnabled(false);
        contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        adapter = new NonTodaContactsAdapter(ContactsManager.getInstance().getContacts(), NonTodaContacts.this);
        contactsList.setAdapter(null);
        contactsList.setAdapter(adapter);
        ((NonTodaContactsAdapter) contactsList.getAdapter()).notifyDataSetChanged();

        if (((NonTodaContactsAdapter) contactsList.getAdapter()).getCount() == 0) {
            rlNoResult.setVisibility(View.VISIBLE);
            rlContact.setVisibility(View.GONE);
        } else {
            rlContact.setVisibility(View.VISIBLE);
            rlNoResult.setVisibility(View.GONE);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        LinphoneContact contact = (LinphoneContact) adapter.getItemAtPosition(position);
        Log.d(TAG, "onItemClick: ");
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
//        NonTodaContacts adapter = (NonTodaContacts) contactsList.getAdapter();
//        if (adapter != null) {
//            contactsList.setFastScrollEnabled(false);
////			if (onlyDisplayLinphoneContacts) {
////				adapter.updateDataSet(ContactsManager.getInstance().getSIPContacts());
////			} else {
////				adapter.updateDataSet(ContactsManager.getInstance().getContacts());
////			}
////            contactsList.setFastScrollEnabled(true);
//            contactsFetchInProgress.setVisibility(View.GONE);
//        }
    }


    @Override
    public void onRefresh() {
        try {
            lastID = 0;

            Service contactService = NetContext.instance.create(Service.class);
            String urlContact;
            urlContact = "AppDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=";//lay tat ca danh ba ra

            contactService.getNonTodaDanhBa(urlContact).enqueue(new Callback<NonTodaContactsResponse>() {
                @Override
                public void onResponse(Call<NonTodaContactsResponse> call, Response<NonTodaContactsResponse> response) {
                    NonTodaContactsResponse nonTodaContactsResponse;
                    nonTodaContactsResponse = response.body();
                    if (nonTodaContactsResponse.isStatus()) {
                        try {
                            DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                            //xoa ca du lieu trong data tim kiem
                            if (!searchText.equals("")) searchNonTodaContacts(searchText);

                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }

                        try {
                            refreshLayout.setRefreshing(false);
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        changeAdapter();
                    }


                }

                @Override
                public void onFailure(Call<NonTodaContactsResponse> call, Throwable t) {
                    try {
                        refreshLayout.setRefreshing(false);
                        dialogSearch.cancel();
                    } catch (Exception e) {

                    }

                    try {
                        Toast.makeText(NonTodaContacts.this,
                                "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(TAG, "onClick: ");
        if (id == R.id.back_add_contact) {
            onBackPressed();
        } else if (id == R.id.clearSearchFieldNonToda) {
            Log.d(TAG, "clearSearchFieldNonToda: ");
            searchField.setText("");
            searchText = "";
        } else if (id == R.id.complete_add_contact) {
            Log.d(TAG, "complete_add_contact: ");
            if (listAddContacts.size() == 0) {
                Toast.makeText(NonTodaContacts.this, "Chưa có liên hệ nào được chọn", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    dialogSearch = ProgressDialog.show(NonTodaContacts.this, "", "Đang Thêm...", true, false);
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String listAddContactsNonToda = ow.writeValueAsString(listAddContacts);
                    listAddContactsNonToda = listAddContactsNonToda.replaceAll(" ", "");
                    listAddContactsNonToda = listAddContactsNonToda.replaceAll("choose", "isChoose");
                    Log.d(TAG, "onClick: " + listAddContactsNonToda);

                    try {
                        listAddContactsNonToda = URLEncoder.encode(listAddContactsNonToda);

                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                    String urlAddContact = "/AppThemDanhBaNoiBo.aspx?idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() +
                            "&idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct() + "&dulieudanhba=" + listAddContactsNonToda;
                    Log.d(TAG, "onClick: " + urlAddContact);
                    Service addContacts = NetContext.getInstance().create(Service.class);
                    addContacts.addNonTodaDanhBa(urlAddContact).enqueue(new Callback<VoidRespon>() {
                        @Override
                        public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                            Log.d(TAG, "onResponse: " + response);
                            try {
                                dialogSearch.cancel();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                            if (response != null) {
                                Log.d(TAG, "onResponse: " + response.body());
                                VoidRespon respon = response.body();
                                HashMap<String, String> itemContactName = DbContext.getInstance().getListContactTodaName(NonTodaContacts.this);
                                Intent intent = new Intent("AddContacts");
                                intent.putExtra("AddContacts", "reloadContacts");
                                sendBroadcast(intent);

                                if (respon.getStatus()) {
                                    NonTodaContactsResponse nonTodaContactsResponse = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this);
                                    ContactResponse currentContact = DbContext.getInstance().getContactResponse(NonTodaContacts.this);
                                    ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContact.getDsdanhba();
                                    for (NonTodaContactsResponse.DSDanhBaNonToda ds : new ArrayList<NonTodaContactsResponse.DSDanhBaNonToda>(listAddContacts)) {
                                        nonTodaContactsResponse.getDsdanhba().remove(ds);
                                        itemContactName.put(ds.getSodienthoai(), ds.getTennhanvien());

                                    }
                                    DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                                    DbContext.getInstance().setListContactTodaName(itemContactName, NonTodaContacts.this);
                                    //add vao danh ba chinh

                                    //xoa ca du lieu trong data tim kiem
                                    if (!searchText.equals("")) searchNonTodaContacts(searchText);

                                    changeAdapter();
                                    listAddContacts.clear();
                                    listIdAddContacts.clear();
                                    try {
                                        Toast.makeText(NonTodaContacts.this,
                                                "Thêm danh bạ thành công",
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.d(TAG, "Exception: " + e.toString());
                                    }
                                } else {
                                    try {
                                        Toast.makeText(NonTodaContacts.this,
                                                "Có lỗi xảy ra, vui lòng liên hệ với quản trị viên để biết thêm chi tiết",
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.d(TAG, "Exception: " + e.toString());
                                    }
                                }
                            } else {
                                Log.d(TAG, "onResponse: " + response.body());
                                try {
                                    Toast.makeText(NonTodaContacts.this,
                                            "Có lỗi xảy ra, vui lòng liên hệ với quản trị viên để biết thêm chi tiết",
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VoidRespon> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.toString());
                            try {
                                dialogSearch.cancel();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                            try {
                                Toast.makeText(NonTodaContacts.this,
                                        "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e);
                }


            }
        }
    }

    class NonTodaContactsAdapter extends BaseAdapter implements SectionIndexer {
        private Context context = null;

        private class ViewHolder {
            public LinearLayout layout;
            public CheckBox delete;
            public ImageView linphoneFriend;
            public TextView name;
            public LinearLayout separator;
            public TextView separatorText;
            public ImageView contactPicture;
            public TextView organization;
            public TextView address;
            public RelativeLayout rlDeleteBar;
            public CheckBox cbxPick;
            private boolean isOpenSwipeLayout;

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
                cbxPick = view.findViewById(R.id.cbx_pick);
                layout = view.findViewById(R.id.layout);

                //friendStatus = (ImageView) view.findViewById(R.id.friendStatus);
            }
        }

        private List<LinphoneContact> contacts;
        String[] sections;
        ArrayList<String> sectionsList;
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();

        NonTodaContactsAdapter(List<LinphoneContact> contactsList) {
            updateDataSet(contactsList);
        }

        NonTodaContactsAdapter(List<LinphoneContact> contactsList, Context context) {
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
            if (searchText.equals(""))
                return DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().size();
            return DbContext.getInstance().getSearchNonTodaContactResponse(NonTodaContacts.this).getDsdanhba().size();

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
            Log.d(TAG, "getView: 1055");
            View view = null;
            final LinphoneContact contact = (LinphoneContact) getItem(position);

//            if (contact == null) return null;
            ViewHolder holder = null;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = mInflater.inflate(R.layout.nontoda_contact_cell, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            final ViewHolder finalHolder = holder;
            NonTodaContactsResponse.DSDanhBaNonToda danhBa;
            if (searchText.equals("")) {
                danhBa = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().get(position);
            } else
                danhBa = DbContext.getInstance().getSearchNonTodaContactResponse(NonTodaContacts.this).getDsdanhba().get(position);
            //giu nguyen trang thai checkbox

            if (listIdAddContacts.indexOf(danhBa.getIdnhanvien()) != -1) {
                finalHolder.cbxPick.setChecked(true);
            } else finalHolder.cbxPick.setChecked(false);

            if (isDeleteAll) {
                finalHolder.cbxPick.setChecked(isDeleteAll);
            }

            finalHolder.cbxPick.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    NonTodaContactsResponse.DSDanhBaNonToda dsDanhBaNonToda;
                    if (searchText.equals(""))
                        dsDanhBaNonToda = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().get(position);
                    else
                        dsDanhBaNonToda = DbContext.getInstance().getSearchNonTodaContactResponse(NonTodaContacts.this).getDsdanhba().get(position);

                    if (finalHolder.cbxPick.isChecked()) {
                        dsDanhBaNonToda.setChoose(true);
                        finalHolder.cbxPick.setChecked(true);
                        listAddContacts.add(dsDanhBaNonToda);
                        listIdAddContacts.add(dsDanhBaNonToda.getIdnhanvien());
                    } else {
                        dsDanhBaNonToda.setChoose(false);
                        finalHolder.cbxPick.setChecked(false);
                        try {
                            listAddContacts.remove(dsDanhBaNonToda);
                            listIdAddContacts.remove(listIdAddContacts.indexOf(dsDanhBaNonToda.getIdnhanvien()));
                        } catch (Exception e) {
                            Log.d(TAG, "onCheckedChanged: " + e.toString());
                        }
                    }
                    Log.d(TAG, "onCheckedChanged: " + listAddContacts.toString());
                }
            });

            try {
                finalHolder.name.setText(danhBa.getTennhanvien());
                finalHolder.organization.setVisibility(View.VISIBLE);
                finalHolder.address.setText(danhBa.getSodienthoai());
                finalHolder.organization.setText(danhBa.getChucvu());
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            if (!isSearchMode) {
                if (getPositionForSection(getSectionForPosition(position)) != position) {
                    finalHolder.separator.setVisibility(View.GONE);
                } else {
                    finalHolder.separator.setVisibility(View.GONE);
                    String fullName = "";
                    try {
                        fullName = DbContext.getInstance().getNonTodaContactsResponse(context).getDsdanhba().get(position).getTennhanvien();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                    if (fullName != null && !fullName.isEmpty()) {
                        finalHolder.separatorText.setVisibility(View.GONE);
                    }
                }
            } else {
                finalHolder.separator.setVisibility(View.GONE);
            }
            finalHolder.contactPicture.setImageBitmap(ContactsManager.getInstance().getDefaultAvatarBitmap());
            if (contact != null) {
                if (contact.isInLinphoneFriendList()) {
                    finalHolder.linphoneFriend.setVisibility(View.VISIBLE);
                } else {
                    finalHolder.linphoneFriend.setVisibility(View.GONE);
                }
                if (contact.hasPhoto()) {
                    LinphoneUtils.setThumbnailPictureFromUri(LinphoneActivity.instance(), finalHolder.contactPicture, contact.getThumbnailUri());
                }
            }
            boolean isOrgVisible = getResources().getBoolean(R.bool.display_contact_organization);
            String org = "";
            if (contact != null) org = contact.getOrganization();
//            if (org != null && !org.isEmpty() && isOrgVisible) {
//                holder.organization.setText(org);
//                holder.organization.setVisibility(View.VISIBLE);
//            } else {
//                holder.organization.setVisibility(View.GONE);
//            }

            if (isEditMode) {
                finalHolder.delete.setVisibility(View.VISIBLE);
                finalHolder.delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    finalHolder.delete.setChecked(true);
                } else {
                    finalHolder.delete.setChecked(false);
                }
            } else {
                finalHolder.delete.setVisibility(View.INVISIBLE);
            }

			/*LinphoneFriend[] friends = LinphoneManager.getLc().getFriendList();
            if (!ContactsManager.getInstance().isContactPresenceDisabled() && friends != null) {
				holder.friendStatus.setVisibility(View.VISIBLE);
				PresenceActivityType presenceActivity = friends[0].getPresenceModel().NonTodaContacts.this.getType();
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
