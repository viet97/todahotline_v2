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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.database.DbContext;
import org.linphone.layoutXML.ExtendedEditText;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.LoginRespon;
import org.linphone.network.models.NonTodaContactsResponse;
import org.linphone.network.models.VoidRespon;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    private ImageView clearSearchField;
    private ExtendedEditText searchField;
    private CheckBox deleteAll;
    private Button completeAddBtn;
    private ProgressBar contactsFetchInProgress;
    private String TAG = "ContactsListFragment";
    private int prelast;
    private Timer timer = new Timer();
    private int lastID = 0;
    private ProgressDialog dialogSearch;
    private String searchText = "";
    private boolean isDeleteMode = false;
    private boolean isDeleteAll = false;
    private ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> listAddContacts = new ArrayList<>();
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
            Log.d(TAG, "afterTextChanged: " + editable.toString());
            prelast = 0;
            isLoaded = false;
            try {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        NonTodaContacts.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogSearch = ProgressDialog.show(NonTodaContacts.this, "", "Đang tìm kiếm...", true, false);
                                String urlContact;
                                urlContact = "AppDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct()
                                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=" + searchText;//lay tat ca danh ba ra


                                Service service = NetContext.getInstance().create(Service.class);
                                service.getNonTodaDanhBa(urlContact).enqueue(new Callback<NonTodaContactsResponse>() {
                                    @Override
                                    public void onResponse(Call<NonTodaContactsResponse> call, Response<NonTodaContactsResponse> response) {
                                        try {
                                            dialogSearch.cancel();
                                        } catch (Exception e) {

                                        }
                                        NonTodaContactsResponse nonTodaContactsResponse;
                                        nonTodaContactsResponse = response.body();
                                        if (nonTodaContactsResponse.isStatus()) {
                                            ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> listDB = nonTodaContactsResponse.getDsdanhba();
                                            try {
                                                DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                                            } catch (Exception e) {

                                            }
                                            if (nonTodaContactsResponse.isEndlist())
                                                isLoaded = true;
                                            changeAdapter();
                                            lastID = nonTodaContactsResponse.getLastid();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<NonTodaContactsResponse> call, Throwable t) {
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
    private boolean isLoaded = false;


    private boolean listIsAtTop() {
        if (contactsList.getChildCount() == 0) return true;
        return contactsList.getChildAt(0).getTop() == 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_toda_contacts);
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
            getContactToda();
            searchField = findViewById(R.id.searchField);
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twToda);
            contactsFetchInProgress = (ProgressBar) findViewById(R.id.contactsFetchInProgress);
            contactsList = (ListView) findViewById(R.id.contactsList);
            refreshLayout.setOnRefreshListener(this);
            completeAddBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listAddContacts.size() == 0) {
                        Toast.makeText(NonTodaContacts.this, "Chưa có liên hệ nào được chọn", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            dialogSearch = ProgressDialog.show(NonTodaContacts.this, "", "Đang Thêm...", true, false);
                            String listAddContactsNonToda = listAddContacts.toString();
                            listAddContactsNonToda= listAddContactsNonToda.replaceAll(" +","");
                            listAddContactsNonToda= listAddContactsNonToda.replaceAll("=",":");
                            try{
                                listAddContactsNonToda = URLEncoder.encode(listAddContactsNonToda);

                            }catch (Exception e){

                            }
                            String urlAddContact = "/AppThemDanhBaNoiBo.aspx?idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() +
                                    "&idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct() + "&dulieudanhba="+listAddContactsNonToda ;
                            Log.d(TAG, "onClick: "+urlAddContact);
                            Service addContacts = NetContext.getInstance().create(Service.class);
                            addContacts.addNonTodaDanhBa(urlAddContact).enqueue(new Callback<VoidRespon>() {
                                @Override
                                public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                                    Log.d(TAG, "onResponse: " + response);
                                    try {
                                        dialogSearch.cancel();
                                    } catch (Exception e) {

                                    }
                                    if (response != null) {
                                        Log.d(TAG, "onResponse: "+response.body());
                                        VoidRespon respon = response.body();
                                        if (respon.getStatus()) {
                                            NonTodaContactsResponse nonTodaContactsResponse = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this);
                                            for (NonTodaContactsResponse.DSDanhBaNonToda  ds : new ArrayList<NonTodaContactsResponse.DSDanhBaNonToda>(listAddContacts)){
                                                    nonTodaContactsResponse.getDsdanhba().remove(ds);

                                            }
                                            DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                                            changeAdapter();
                                            listAddContacts.clear();
                                            try {
                                                Toast.makeText(NonTodaContacts.this,
                                                        "Thêm danh bạ thành công",
                                                        Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {

                                            }
                                        } else {
                                            try {
                                                Toast.makeText(NonTodaContacts.this,
                                                        "Có lỗi xảy ra, vui lòng liên hệ với quản trị viên để biết thêm chi tiết",
                                                        Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {

                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "onResponse: "+response.body());
                                        try {
                                            Toast.makeText(NonTodaContacts.this,
                                                    "Có lỗi xảy ra, vui lòng liên hệ với quản trị viên để biết thêm chi tiết",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<VoidRespon> call, Throwable t) {
                                    Log.d(TAG, "onFailure: " + t.toString());
                                    try {
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
                            android.util.Log.d(TAG, "Exception: " + e);
                        }


                    }
                }
            });
            clearSearchField.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchField.setText("");
                }
            });
            contactsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    try {
                        refreshLayout.setEnabled(listIsAtTop());
                    } catch (Exception e) {

                    }
                    int lastItem = i1 + i;

                    if (lastItem == i2) {
                        if (prelast != lastItem && !isLoaded) {
                            Log.d(TAG, "onScroll: last");
                            prelast = lastItem;
                            getContactToda();
                        }
                    }
                }
            });
            allContacts.callOnClick();
        } catch (Exception e) {
            Log.d(TAG, "onCreateView: " + e);
        }
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
            urlContact = "AppDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=" + searchText;//lay tat ca danh ba ra

            contactService.getNonTodaDanhBa(urlContact).enqueue(new Callback<NonTodaContactsResponse>() {
                @Override
                public void onResponse(Call<NonTodaContactsResponse> call, Response<NonTodaContactsResponse> response) {
                    NonTodaContactsResponse nonTodaContactsResponse = new NonTodaContactsResponse();
                    nonTodaContactsResponse = response.body();
                    if (nonTodaContactsResponse.isStatus()) {

                        if (lastID == 0) {
                            try {
                                DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                            } catch (Exception e) {

                            }

                        } else {
                            try {
                                NonTodaContactsResponse currentNonTodaContactResponse = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this);
                                ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> dsDanhBas = currentNonTodaContactResponse.getDsdanhba();
                                for (NonTodaContactsResponse.DSDanhBaNonToda ds : nonTodaContactsResponse.getDsdanhba()) {
                                    dsDanhBas.add(ds);
                                }
                                currentNonTodaContactResponse.setDsdanhba(dsDanhBas);
                                DbContext.getInstance().setNonTodaContactsResponse(currentNonTodaContactResponse, NonTodaContacts.this);
                            } catch (Exception e) {

                            }

                        }
                        try {
                        } catch (Exception e) {

                        }
                        changeAdapter();
                        if (nonTodaContactsResponse.isEndlist()) isLoaded = true;
                        lastID = nonTodaContactsResponse.getLastid();
                    }
                }

                @Override
                public void onFailure(Call<NonTodaContactsResponse> call, Throwable t) {
                    try {

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
            android.util.Log.d(TAG, "Exception: " + e);
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
        NonTodaContacts adapter = (NonTodaContacts) contactsList.getAdapter();
        if (adapter != null) {
            contactsList.setFastScrollEnabled(false);
//			if (onlyDisplayLinphoneContacts) {
//				adapter.updateDataSet(ContactsManager.getInstance().getSIPContacts());
//			} else {
//				adapter.updateDataSet(ContactsManager.getInstance().getContacts());
//			}
//            contactsList.setFastScrollEnabled(true);
            contactsFetchInProgress.setVisibility(View.GONE);
        }
    }


    @Override
    public void onRefresh() {
        try {
            lastID = 0;
            isLoaded = false;
            prelast = 0;
            Service contactService = NetContext.instance.create(Service.class);
            String urlContact;
            urlContact = "AppDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(NonTodaContacts.this).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=" + searchText;//lay tat ca danh ba ra

            contactService.getNonTodaDanhBa(urlContact).enqueue(new Callback<NonTodaContactsResponse>() {
                @Override
                public void onResponse(Call<NonTodaContactsResponse> call, Response<NonTodaContactsResponse> response) {
                    NonTodaContactsResponse nonTodaContactsResponse = new NonTodaContactsResponse();
                    nonTodaContactsResponse = response.body();
                    if (nonTodaContactsResponse.isStatus()) {

                        if (lastID == 0) {
                            try {
                                DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, NonTodaContacts.this);
                            } catch (Exception e) {

                            }

                        } else {
                            try {
                                NonTodaContactsResponse currentNonTodaContactResponse = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this);
                                ArrayList<NonTodaContactsResponse.DSDanhBaNonToda> dsDanhBas = currentNonTodaContactResponse.getDsdanhba();
                                for (NonTodaContactsResponse.DSDanhBaNonToda ds : nonTodaContactsResponse.getDsdanhba()) {
                                    dsDanhBas.add(ds);
                                }
                                currentNonTodaContactResponse.setDsdanhba(dsDanhBas);
                                DbContext.getInstance().setNonTodaContactsResponse(currentNonTodaContactResponse, NonTodaContacts.this);
                            } catch (Exception e) {

                            }

                        }
                        try {
                            refreshLayout.setRefreshing(false);
                        } catch (Exception e) {

                        }
                        changeAdapter();
                        if (nonTodaContactsResponse.isEndlist()) isLoaded = true;
                        lastID = nonTodaContactsResponse.getLastid();
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
            android.util.Log.d(TAG, "Exception: " + e);
        }
    }

    @Override
    public void onClick(View view) {

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
            Log.d(TAG, "getCount: " + DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().size());
            return DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().size();

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
            if (isDeleteAll) {
                holder.cbxPick.setChecked(isDeleteAll);
            }

            holder.cbxPick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    NonTodaContactsResponse.DSDanhBaNonToda dsDanhBaNonToda = DbContext.getInstance().getNonTodaContactsResponse(NonTodaContacts.this).getDsdanhba().get(position);
                    if (b) {
                        finalHolder.cbxPick.setChecked(b);
                        listAddContacts.add(dsDanhBaNonToda);
                    } else {
                        finalHolder.cbxPick.setChecked(b);
                        listAddContacts.remove(dsDanhBaNonToda);
                    }
                    Log.d(TAG, "onCheckedChanged: " + listAddContacts.toString());
                }
            });

            try {
                holder.name.setText(DbContext.getInstance().getNonTodaContactsResponse(view.getContext()).getDsdanhba().get(position).getTennhanvien());
                holder.organization.setVisibility(View.VISIBLE);
                holder.address.setText(DbContext.getInstance().getNonTodaContactsResponse(view.getContext()).getDsdanhba().get(position).getSodienthoai());
                holder.organization.setText(DbContext.getInstance().getNonTodaContactsResponse(view.getContext()).getDsdanhba().get(position).getChucvu());
            } catch (Exception e) {

            }
            if (!isSearchMode) {
                if (getPositionForSection(getSectionForPosition(position)) != position) {
                    holder.separator.setVisibility(View.GONE);
                } else {
                    holder.separator.setVisibility(View.GONE);
                    String fullName = "";
                    try {
                        fullName = DbContext.getInstance().getNonTodaContactsResponse(context).getDsdanhba().get(position).getTennhanvien();
                    } catch (Exception e) {

                    }
                    if (fullName != null && !fullName.isEmpty()) {
                        holder.separatorText.setVisibility(View.GONE);
                    }
                }
            } else {
                holder.separator.setVisibility(View.GONE);
            }
            holder.contactPicture.setImageBitmap(ContactsManager.getInstance().getDefaultAvatarBitmap());
            if (contact != null) {
                if (contact.isInLinphoneFriendList()) {
                    holder.linphoneFriend.setVisibility(View.VISIBLE);
                } else {
                    holder.linphoneFriend.setVisibility(View.GONE);
                }
                if (contact.hasPhoto()) {
                    LinphoneUtils.setThumbnailPictureFromUri(LinphoneActivity.instance(), holder.contactPicture, contact.getThumbnailUri());
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
