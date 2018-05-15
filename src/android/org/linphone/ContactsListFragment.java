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


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.Space;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import org.linphone.database.DbContext;
import org.linphone.layoutXML.ExtendedEditText;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.connectivity.Connectivity;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.LoginRespon;
import org.linphone.network.models.NonTodaContactsResponse;
import org.linphone.network.models.VoidRespon;
import org.linphone.notice.DisplayNotice;
import org.linphone.ultils.ContactUltils;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

public class ContactsListFragment extends Fragment implements OnClickListener, OnItemClickListener, ContactsUpdatedListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int ALL_EXT = 1;
    private static final int ONL_EXT = 2;
    private static final int OFF_EXT = 3;
    private static final int LOCAL_CONTACT = 0;
    private static final int TODA_CONTACT = 1;
    private static final int CUS_CONTACT = 2;
    private LayoutInflater mInflater;
    private ListView contactsList;
    private TextView allContacts, linphoneContacts, cusContacts, noSipContact, noContact;
    private ImageView newContact, edit, selectAll, deselectAll, delete, cancel, backDeleteMode, deleteContact;
    private ImageView addContacts;
    private RelativeLayout rlCusContact, rlTodaContact;
    private RelativeLayout rlNoResult, rlContact;
    private SwipeRefreshLayout refreshLayout;
    private boolean isEditMode, isSearchMode;
    private ArrayList<ContactResponse.DSDanhBa> dsDanhBas = new ArrayList<>();
    public static int onlyDisplayLinphoneContacts;
    private View allContactsSelected, linphoneContactsSelected, cusContactSelected;
    private LinearLayout editList, topbar;
    private RelativeLayout deleteBar;
    private int lastKnownPosition;
    private boolean editOnClick = false, editConsumed = false, onlyDisplayChatAddress = false;
    private String sipAddressToAdd, displayName = null;
    private ImageView clearSearchField;
    private ExtendedEditText searchField;
    private CheckBox deleteAll, allExt, onlExt, offExt;
    private ProgressBar contactsFetchInProgress;
    private String TAG = "ContactsListFragment";
    BroadcastReceiver receiverLoadData;
    private int lastID = 0;
    private int extStatusCheckBox = 1;
    private LinearLayout llContainer;
    private ProgressDialog dialogSearch;
    private ProgressDialog dialogRemove;
    private String searchText = "";
    private boolean isDeleteMode = false;
    private boolean isDeleteAll = false;
    private ArrayList<Integer> listIdDelete = new ArrayList<>();
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
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(final Editable editable) {

            searchTodaOrCusContacts(searchField.getText().toString());
            if (!isDeleteMode) addContacts.setVisibility(View.VISIBLE);

        }
    };

    private void searchTodaOrCusContacts(String s) {
        searchText = s;
        s = ContactUltils.instance.removeAccents(s);

        ArrayList<ContactResponse.DSDanhBa> dsDanhBa = null;
        ArrayList<ContactResponse.DSDanhBa> listSearchDanhBa = new ArrayList<>();
        if (onlyDisplayLinphoneContacts == 1) {
            dsDanhBa = DbContext.getInstance().getContactResponse(getActivity()).getDsdanhba();
            for (ContactResponse.DSDanhBa ds : dsDanhBa) {
                if (
                        ContactUltils.instance.removeAccents(ds.getSodienthoai()).contains(s) ||
                                ContactUltils.instance.removeAccents(ds.getJob()).contains(s) ||
                                ContactUltils.instance.removeAccents(ds.getTenlienhe()).contains(s) ||
                                ContactUltils.instance.removeAccents(ds.getSodienthoai()).startsWith(s) ||
                                ContactUltils.instance.removeAccents(ds.getTenlienhe()).startsWith(s) ||
                                ContactUltils.instance.removeAccents(ds.getJob()).startsWith(s)) {
                    listSearchDanhBa.add(ds);
                }
            }

        } else if (onlyDisplayLinphoneContacts == 2) {
            dsDanhBa = DbContext.getInstance().getCusContactResponse(getActivity()).getDsdanhba();
            for (ContactResponse.DSDanhBa ds : dsDanhBa) {
                if (ContactUltils.instance.removeAccents(ds.getSodienthoai()).contains(s) ||
                        ContactUltils.instance.removeAccents(ds.getTenlienhe()).contains(s) ||
                        ContactUltils.instance.removeAccents(ds.getSodienthoai()).startsWith(s) ||
                        ContactUltils.instance.removeAccents(ds.getTenlienhe()).startsWith(s)) {
                    listSearchDanhBa.add(ds);
                }
            }

        }
        ContactResponse currentSearchResponse = DbContext.getInstance().getSearchContactResponse(getActivity());
        currentSearchResponse.setDsdanhba(listSearchDanhBa);
        DbContext.getInstance().setSearchContactResponse(currentSearchResponse, getActivity());
        reloadListContacts();
        changeAdapter();
    }

    private void reloadListContacts() {
        Context context = getActivity();
        dsDanhBas.clear();
        ArrayList<ContactResponse.DSDanhBa> danhsach;
        if (onlyDisplayLinphoneContacts != LOCAL_CONTACT) {
            if (!searchField.getText().toString().equals("")) {
                danhsach = DbContext.getInstance().getSearchContactResponse(context).getDsdanhba();
                if (onlyDisplayLinphoneContacts == TODA_CONTACT) {
                    if (extStatusCheckBox == ONL_EXT) {
                        for (ContactResponse.DSDanhBa ds : danhsach) {
                            if (ds.isStatus()) dsDanhBas.add(ds);
                        }
                    } else if (extStatusCheckBox == OFF_EXT) {
                        for (ContactResponse.DSDanhBa ds : danhsach) {
                            if (!ds.isStatus()) dsDanhBas.add(ds);
                        }
                    } else dsDanhBas = danhsach;
                } else dsDanhBas = danhsach;
            } else if (onlyDisplayLinphoneContacts == TODA_CONTACT) {
                danhsach = DbContext.getInstance().getContactResponse(context).getDsdanhba();
                if (extStatusCheckBox == ONL_EXT) {
                    for (ContactResponse.DSDanhBa ds : danhsach) {
                        if (ds.isStatus()) dsDanhBas.add(ds);
                    }
                } else if (extStatusCheckBox == OFF_EXT) {
                    for (ContactResponse.DSDanhBa ds : danhsach) {
                        if (!ds.isStatus()) dsDanhBas.add(ds);
                    }
                } else dsDanhBas = danhsach;
            } else if (onlyDisplayLinphoneContacts == CUS_CONTACT) {
                danhsach = DbContext.getInstance().getCusContactResponse(context).getDsdanhba();
                dsDanhBas = danhsach;
            }

        }
    }
    private boolean isLoaded = false;


    private boolean listIsAtTop() {
        if (contactsList.getChildCount() == 0) return true;
        return contactsList.getChildAt(0).getTop() == 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isLoaded = false;
        mInflater = inflater;
        View view = inflater.inflate(R.layout.contacts_list, container, false);
        receiverLoadData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras().get("AddContacts").equals("reloadContacts")) {
                    onRefresh();
                    Log.d(TAG, "receiverLoadData: ");
                }
            }
        };
        try {
            lastID = 0;
            searchText = "";
            if (getArguments() != null) {
                editOnClick = getArguments().getBoolean("EditOnClick");
                sipAddressToAdd = getArguments().getString("SipAddress");
                if (getArguments().getString("DisplayName") != null)
                    displayName = getArguments().getString("DisplayName");
                onlyDisplayChatAddress = getArguments().getBoolean("ChatAddressOnly");
            }
//            getContactToda();
            rlContact = view.findViewById(R.id.rl_contactlist);
            rlNoResult = view.findViewById(R.id.rl_no_result);
            rlCusContact = view.findViewById(R.id.rl_cus_contact);

            rlTodaContact = view.findViewById(R.id.rl_toda_contact);
            deleteBar = view.findViewById(R.id.delete_bar);
            refreshLayout = view.findViewById(R.id.refresh_layout);
            backDeleteMode = view.findViewById(R.id.back_delete_mode);
            deleteContact = view.findViewById(R.id.delete_contact);
            deleteAll = view.findViewById(R.id.delete_all);
            allExt = view.findViewById(R.id.all_ext);
            allExt.setOnClickListener(this);
            onlExt = view.findViewById(R.id.onl_ext);
            onlExt.setOnClickListener(this);
            offExt = view.findViewById(R.id.off_ext);
            offExt.setOnClickListener(this);
            addContacts = view.findViewById(R.id.add_contacts);
            rlCusContact.setVisibility(View.GONE);
            rlTodaContact.setVisibility(View.GONE);
            for (LoginRespon.Data.DSloaidanhba ds : DbContext.getInstance().getLoginRespon(getActivity()).getData().getDsloaidanhba()) {
                int type = ds.getIdloaidanhba();
                switch (type) {
                    case 1:
                        rlTodaContact.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        rlCusContact.setVisibility(View.VISIBLE);
                        break;
                }

            }
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
            addContacts.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onlyDisplayLinphoneContacts == 1)
                        startActivity(new Intent(getActivity(), NonTodaContacts.class));
                    else startActivity(new Intent(getActivity(), CusContactsActivity.class));
                }
            });
            deleteContact.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
//                    AlertDialog.Builder builder;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
//                    } else {
//                        builder = new AlertDialog.Builder(getActivity());
//                    }
                    try {
//                        builder.setTitle("Xóa")
//                                .setMessage("Bạn có thật sự muốn xóa những liên hệ này ?")
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteArrayContact();
//                                    }
//
//                                })
//                                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // do nothing
//                                    }
//                                })
//                                .setIcon(R.drawable.ic_delete_black_24dp)
//                                .show();

                    } catch (Exception e) {

                    }
                }
            });
            searchField = view.findViewById(R.id.searchField);
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twLocal);
            searchField.setOnClickListener(this);
            contactsFetchInProgress = (ProgressBar) view.findViewById(R.id.contactsFetchInProgress);
//            contactsFetchInProgress.setVisibility(View.VISIBLE);
            refreshLayout.setOnRefreshListener(this);

            deleteAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    deleteAll.setChecked(b);
                    isDeleteAll = b;
                    if (!b) {
                        listIdDelete.clear();
                        deleteContact.setVisibility(View.GONE);
                    } else {
                        deleteContact.setVisibility(View.VISIBLE);
                        listIdDelete.clear();
                        for (ContactResponse.DSDanhBa danhba : dsDanhBas) {
                            listIdDelete.add(danhba.getIddanhba());
                        }
                    }
                    changeAdapter();
                }
            });
            backDeleteMode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteBar.setVisibility(View.GONE);
                    topbar.setVisibility(View.VISIBLE);
                    backDeleteMode.setVisibility(View.GONE);
                    if (onlyDisplayLinphoneContacts == 1) {
                        if (DbContext.getInstance().getLoginRespon(getActivity()).getData().getChophepxemonoffext().equals("true")) {
                            allExt.setVisibility(View.VISIBLE);
                            onlExt.setVisibility(View.VISIBLE);
                            offExt.setVisibility(View.VISIBLE);
                            deleteAll.setVisibility(View.INVISIBLE);
                        } else {
                            allExt.setVisibility(View.GONE);
                            onlExt.setVisibility(View.GONE);
                            offExt.setVisibility(View.GONE);
                            deleteAll.setVisibility(View.GONE);
                        }

                    } else {
                        deleteAll.setVisibility(View.GONE);
                    }

                    listIdDelete.clear();
                    isDeleteMode = false;
                    addContacts.setVisibility(View.VISIBLE);
                    changeAdapter();
                }
            });

            contactsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    if (onlyDisplayLinphoneContacts != 0) {
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && listIsAtTop() && !isDeleteMode) {
                            addContacts.setVisibility(View.VISIBLE);
                        } else {
                            addContacts.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                    Log.d(TAG, "onScroll: ");
                    try {
                        refreshLayout.setEnabled(listIsAtTop());
                    } catch (Exception e) {

                    }
//                    Log.d(TAG, "onScroll: " + listIsAtTop());
//                    if (onlyDisplayLinphoneContacts != 0) {
//                        int lastItem = i1 + i;
//
//                        if (lastItem == i2) {
//                            if (prelast != lastItem && !isLoaded) {
//                                Log.d(TAG, "onScroll: last");
//                                prelast = lastItem;
//                                getContactToda();
//                            }
//                        }
//                    }
                }
            });
            allContacts.callOnClick();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
//        changeAdapter();
        return view;
    }

    private void deleteArrayContact() {
        if (listIdDelete.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.no_contacts_picked), Toast.LENGTH_SHORT).show();
        } else {
            try {
                dialogRemove = ProgressDialog.show(getActivity(), "", getString(R.string.removing_message_dialog), true, false);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            String arrayContact = listIdDelete.toString();
            try {
                arrayContact = URLEncoder.encode(arrayContact);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            String deleteContactURL = "";
            if (onlyDisplayLinphoneContacts == 1) {
                deleteContactURL = "AppXoaDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&dulieudanhba=" + arrayContact;
            } else if (onlyDisplayLinphoneContacts == 2)
                deleteContactURL = "AppXoaDanhBaKhachHang.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&dulieudanhba=" + arrayContact;
            Log.d(TAG, "deleteContact: " + deleteContact);
            Service service = NetContext.getInstance().create(Service.class);
            service.xoaDanhBa(deleteContactURL).enqueue(new Callback<VoidRespon>() {
                @Override
                public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                    Log.d(TAG, "onResponse: " + response);
                    try {
                        dialogRemove.cancel();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }

                    if (response != null) {
                        VoidRespon respon = response.body();
                        if (respon.getStatus()) {
                            deleteContact.setVisibility(View.GONE);
                            ContactResponse contactResponse = null;
                            NonTodaContactsResponse nonContactResponse = null;
                            HashMap<String, String> itemContactName = null;
                            if (onlyDisplayLinphoneContacts == 1) {
                                contactResponse = DbContext.getInstance().getContactResponse(getActivity());
                                nonContactResponse = DbContext.getInstance().getNonTodaContactsResponse(getActivity());
                                itemContactName = DbContext.getInstance().getListContactTodaName(getActivity());
                            } else if (onlyDisplayLinphoneContacts == 2) {
                                contactResponse = DbContext.getInstance().getCusContactResponse(getActivity());
                                itemContactName = DbContext.getInstance().getListCusContactTodaName(getActivity());
                            }
                            for (ContactResponse.DSDanhBa dsDanhBa : new ArrayList<ContactResponse.DSDanhBa>(contactResponse.getDsdanhba())) {
                                int id = dsDanhBa.getIddanhba();
                                if (listIdDelete.indexOf(id) != -1) {
                                    contactResponse.getDsdanhba().remove(dsDanhBa);
                                    itemContactName.remove(dsDanhBa.getSodienthoai());
                                    if (onlyDisplayLinphoneContacts == 1) {
                                        nonContactResponse.getDsdanhba().add(new NonTodaContactsResponse.DSDanhBaNonToda(
                                                dsDanhBa.getIdrow(),
                                                Integer.parseInt(dsDanhBa.getIdqllh()),
                                                Integer.parseInt(dsDanhBa.getIdnhanvien()),
                                                dsDanhBa.getTenlienhe(),
                                                dsDanhBa.getSodienthoai(),
                                                dsDanhBa.getJob(),
                                                false));
                                    }
                                }

                            }
                            if (onlyDisplayLinphoneContacts == 1) {
                                DbContext.getInstance().setListContactTodaName(itemContactName, getActivity());
                                DbContext.getInstance().setContactResponse(contactResponse, getActivity());
                                DbContext.getInstance().setNonTodaContactsResponse(nonContactResponse, getActivity());
                            } else if (onlyDisplayLinphoneContacts == 2) {
                                DbContext.getInstance().setListCusContactTodaName(itemContactName, getActivity());
                                DbContext.getInstance().setCusContactResponse(contactResponse, getActivity());
                            }
                            Log.d(TAG, "onResponse: " + itemContactName.toString());

                            if (!searchText.equals("")) {
                                contactResponse = DbContext.getInstance().getSearchContactResponse(getActivity());

                                for (ContactResponse.DSDanhBa dsDanhBa : new ArrayList<ContactResponse.DSDanhBa>(contactResponse.getDsdanhba())) {
                                    int id = dsDanhBa.getIddanhba();
                                    if (listIdDelete.indexOf(id) != -1) {
                                        contactResponse.getDsdanhba().remove(dsDanhBa);
                                    }
                                }
                                DbContext.getInstance().setSearchContactResponse(contactResponse, getActivity());

                            }
                            deleteAll.setChecked(false);
                            listIdDelete.clear();
                            reloadListContacts();
                            changeAdapter();
                        } else {

                            try {
                                listIdDelete.clear();
                                Toast.makeText(getActivity(),
                                        getString(R.string.adminstrator_error),
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                        }
                    } else {
                        try {
                            listIdDelete.clear();
                            Toast.makeText(getActivity(),
                                    getString(R.string.adminstrator_error),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {

                        }
                    }

                }

                @Override
                public void onFailure(Call<VoidRespon> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.toString());
                    try {
                        dialogRemove.cancel();
                    } catch (Exception e) {

                    }
                    try {
                        listIdDelete.clear();
                        Toast.makeText(getActivity(),
                                getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                }
            });
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
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiverLoadData);
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
            Log.d(TAG, "all_contacts: ");

            // thay doi trang thai moi lan chuyen tab
            deleteAll.setVisibility(View.GONE);
            allExt.setVisibility(View.GONE);
            onlExt.setVisibility(View.GONE);
            offExt.setVisibility(View.GONE);
            onlyDisplayLinphoneContacts = LOCAL_CONTACT;

            lastID = 0;
            isLoaded = false;
            addContacts.setVisibility(View.GONE);
            searchField.clearTextChangedListeners();
            searchField.addTextChangedListener(twLocal);
            searchField.setText("");
            reloadListContacts();
            allContactsSelected.setVisibility(View.VISIBLE);
            allContacts.setEnabled(false);
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setEnabled(true);
            cusContacts.setEnabled(true);
            cusContactSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);
            changeAdapter();
        } else if (id == R.id.linphone_contacts) {
            if (DbContext.getInstance().getLoginRespon(getActivity()).getData().getChophepxemonoffext().equals("true")) {
                deleteAll.setVisibility(View.INVISIBLE);
                allExt.setVisibility(View.VISIBLE);
                onlExt.setVisibility(View.VISIBLE);
                offExt.setVisibility(View.VISIBLE);
            } else {
                deleteAll.setVisibility(View.GONE);
                allExt.setVisibility(View.GONE);
                onlExt.setVisibility(View.GONE);
                offExt.setVisibility(View.GONE);
            }
            filtContactsByCheckbox(extStatusCheckBox);
            addContacts.setVisibility(View.VISIBLE);
            onlyDisplayLinphoneContacts = TODA_CONTACT;

            lastID = 0;
            isLoaded = false;
            searchField.clearTextChangedListeners();
            searchField.setText("");
            searchText = "";
            reloadListContacts();

            searchField.addTextChangedListener(twToda);
            onRefresh();
            changeAdapter();
//            try {
//                dialogSearch = ProgressDialog.show(getActivity(), "", "Đang tải...", true, false);
//                Service contactService = NetContext.instance.create(Service.class);
//                String urlContact = "AppDanhBa_v2.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
//                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=" + searchText;//lay tat ca danh ba ra
//                android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
//                contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
//                    @Override
//                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
//                        ContactResponse contactResponse = new ContactResponse();
//                        contactResponse = response.body();
//                        try {
//                            dialogSearch.cancel();
//                        } catch (Exception e) {
//
//                        }
//                        if (contactResponse.getStatus()) {
//                            Log.d(TAG, "onResponse: " + lastID);
//                            if (lastID == 0) {
//
//                                Log.d(TAG, "onResponse:" + DbContext.getInstance());
//                                try {
//                                    DbContext.getInstance().setContactResponse(contactResponse, getActivity());
//                                } catch (Exception e) {
//
//                                }
//
//                            } else {
//                                try {
//                                    ContactResponse currentContactResponse = DbContext.getInstance().getContactResponse(getActivity());
//                                    ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContactResponse.getDsdanhba();
//                                    for (ContactResponse.DSDanhBa ds : contactResponse.getDsdanhba()) {
//                                        dsDanhBas.add(ds);
//                                    }
//                                    currentContactResponse.setDsdanhba(dsDanhBas);
//                                    DbContext.getInstance().setContactResponse(currentContactResponse, getActivity());
//                                } catch (Exception e) {
//
//                                }
//
//                            }
//
//                            changeAdapter();
//                            if (contactResponse.isEndlist()) isLoaded = true;
//                            lastID=contactResponse.getLastid();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ContactResponse> call, Throwable t) {
//                        try {
//                            dialogSearch.cancel();
//                        } catch (Exception e) {
//
//                        }
//                        try {
//                            Toast.makeText(getActivity(),
//                                    "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
//                                    Toast.LENGTH_SHORT).show();
//                        } catch (Exception e) {
//
//                        }
//                    }
//
//                });
//            } catch (Exception e) {
//                try {
//                    dialogSearch.cancel();
//                } catch (Exception ex) {
//
//                }
//
//                android.util.Log.d(TAG, "Exception: " + e);
//            }
            allContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.VISIBLE);
            linphoneContacts.setEnabled(false);
            allContacts.setEnabled(true);
            cusContacts.setEnabled(true);
            cusContactSelected.setVisibility(View.INVISIBLE);

        } else if (id == R.id.cus_contacts) {
            deleteAll.setVisibility(View.GONE);
            allExt.setVisibility(View.GONE);
            onlExt.setVisibility(View.GONE);
            offExt.setVisibility(View.GONE);
            addContacts.setVisibility(View.VISIBLE);
            onlyDisplayLinphoneContacts = CUS_CONTACT;

            lastID = 0;
            isLoaded = false;
            searchText = "";
            searchField.clearTextChangedListeners();
            searchField.setText("");
            searchField.addTextChangedListener(twToda);
            reloadListContacts();
            changeAdapter();
//            try {
//                dialogSearch = ProgressDialog.show(getActivity(), "", "Đang tải...", true, false);
//                Service contactService = NetContext.instance.create(Service.class);
//                String urlContact = "AppDanhBaKhachHang_v2.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
//                        + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=" + searchText;//lay tat ca danh ba ra
//                android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
//                contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
//                    @Override
//                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
//                        ContactResponse contactResponse = new ContactResponse();
//                        contactResponse = response.body();
//                        try {
//                            dialogSearch.cancel();
//                        } catch (Exception e) {
//
//                        }
//                        if (contactResponse.getStatus()) {
//                            Log.d(TAG, "onResponse: " + lastID);
//                            if (lastID == 0) {
//
//                                Log.d(TAG, "onResponse:" + DbContext.getInstance());
//                                try {
//                                    DbContext.getInstance().setContactResponse(contactResponse, getActivity());
//                                } catch (Exception e) {
//
//                                }
//
//                            } else {
//                                try {
//                                    ContactResponse currentContactResponse = DbContext.getInstance().getContactResponse(getActivity());
//                                    ArrayList<ContactResponse.DSDanhBa> dsDanhBas = currentContactResponse.getDsdanhba();
//                                    for (ContactResponse.DSDanhBa ds : contactResponse.getDsdanhba()) {
//                                        dsDanhBas.add(ds);
//                                    }
//                                    currentContactResponse.setDsdanhba(dsDanhBas);
//                                    DbContext.getInstance().setContactResponse(currentContactResponse, getActivity());
//                                } catch (Exception e) {
//
//                                }
//
//                            }
//                            if (contactResponse.isEndlist()) isLoaded = true;
//                            changeAdapter();
//
//                            lastID=contactResponse.getLastid();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ContactResponse> call, Throwable t) {
//                        try {
//                            dialogSearch.cancel();
//                        } catch (Exception e) {
//
//                        }
//                        try {
//                            Toast.makeText(getActivity(),
//                                    "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
//                                    Toast.LENGTH_SHORT).show();
//                        } catch (Exception e) {
//
//                        }
//                    }
//
//                });
//            } catch (Exception e) {
//                android.util.Log.d(TAG, "Exception: " + e);
//            }

            allContacts.setTextColor(Color.parseColor("#ffffff"));
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            allContactsSelected.setVisibility(View.INVISIBLE);
            linphoneContactsSelected.setVisibility(View.INVISIBLE);
            cusContacts.setEnabled(false);
            cusContactSelected.setVisibility(View.VISIBLE);
            linphoneContacts.setEnabled(true);
            allContacts.setEnabled(true);

        } else if (id == R.id.all_ext) {
            onRefresh();
            filtContactsByCheckbox(ALL_EXT);
        } else if (id == R.id.onl_ext) {
            onRefresh();
            filtContactsByCheckbox(ONL_EXT);
        } else if (id == R.id.off_ext) {
            onRefresh();
            filtContactsByCheckbox(OFF_EXT);
        } else if (isEditMode) {
            Log.d(TAG, "onClick: 418");
            deselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
        }

//        if (searchField.getText().toString().length() > 0) {
//            Log.d(TAG, "onClick: 424");
////            searchContacts();
//        } else {
//            Log.d(TAG, "onClick: 427");
//            changeContactsAdapter();
//        }

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
            searchText = "";
        } else if (id == R.id.searchField) {
            onRefresh();
        }
    }

    public void filtContactsByCheckbox(int status) {
        extStatusCheckBox = status;
        if (!isDeleteMode) addContacts.setVisibility(View.VISIBLE);
        switch (status) {
            case ALL_EXT:
                allExt.setChecked(true);
                onlExt.setChecked(false);
                offExt.setChecked(false);
                allExt.setEnabled(false);
                onlExt.setEnabled(true);
                offExt.setEnabled(true);
                break;
            case ONL_EXT:
                allExt.setChecked(false);
                onlExt.setChecked(true);
                offExt.setChecked(false);
                allExt.setEnabled(true);
                onlExt.setEnabled(false);
                offExt.setEnabled(true);
                break;
            case OFF_EXT:
                allExt.setChecked(false);
                onlExt.setChecked(false);
                offExt.setChecked(true);
                allExt.setEnabled(true);
                onlExt.setEnabled(true);
                offExt.setEnabled(false);
                break;
        }
        reloadListContacts();
        changeAdapter();

    }

    public void changeAdapter() {
//        listIdDelete.clear();
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
        contactsList.setAdapter(null);
        contactsList.setAdapter(adapter);
        ((ContactsListAdapter) contactsList.getAdapter()).notifyDataSetChanged();
        if (((ContactsListAdapter) contactsList.getAdapter()).getCount() == 0) {
            rlNoResult.setVisibility(View.VISIBLE);
            rlContact.setVisibility(View.GONE);
        } else {
            rlContact.setVisibility(View.VISIBLE);
            rlNoResult.setVisibility(View.GONE);
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
//        if (getResources().getBoolean(R.bool.isTablet)) {
//            displayFirstContact();
//        }
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
        searchText = search;
        isSearchMode = true;
        contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getContacts(search)));
        changeAdapter();
//		if (onlyDisplayLinphoneContacts!=0) {
//			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//			contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getSIPContacts(search)));
//		} else {
        contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getContacts(search)));
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
//        contactsList.setFastScrollEnabled(true);
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
            linphoneContacts.setTextColor(Color.parseColor("#ffffff"));
            cusContacts.setTextColor(Color.parseColor("#ffffff"));

        } else if (onlyDisplayLinphoneContacts == 1) {
            allContacts.setTextColor(Color.parseColor("#ffffff"));
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
        if (onlyDisplayLinphoneContacts != 0) {
            onRefresh();
        }
        ContactsManager.addContactsListener(this);
        super.onResume();
        getActivity().registerReceiver(receiverLoadData, new IntentFilter("AddContacts"));

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
        if (onlyDisplayLinphoneContacts != 0) addContacts.setVisibility(View.VISIBLE);
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
//            contactsList.setFastScrollEnabled(true);
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

    @Override
    public void onRefresh() {
        if (onlyDisplayLinphoneContacts != 0) {
            try {
                lastID = 0;
                isLoaded = false;
                Service contactService = NetContext.instance.create(Service.class);
                String urlContact;
                if (onlyDisplayLinphoneContacts == 1)
                    urlContact = "AppDanhBa_v2.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                            + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=";//lay tat ca danh ba ra
                else
                    urlContact = "AppDanhBaKhachHang_v2.aspx?idct=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdct()
                            + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien() + "&lastID=" + lastID + "&timkiem=";//lay tat ca danh ba ra

                contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                    @Override
                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                        ContactResponse contactResponse;
                        contactResponse = response.body();
                        try {
                            if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        if (contactResponse.getStatus()) {

                            try {
                                if (onlyDisplayLinphoneContacts == 1) {
                                    DbContext.getInstance().setContactResponse(contactResponse, getActivity());

                                }
                                if (onlyDisplayLinphoneContacts == 2) {
                                    DbContext.getInstance().setCusContactResponse(contactResponse, getActivity());
                                }
                                if (!searchText.equals("")) {
                                    searchTodaOrCusContacts(searchField.getText().toString());
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }


                            addContacts.setVisibility(View.VISIBLE);
                            reloadListContacts();
                            changeAdapter();
                        }
                    }

                    @Override
                    public void onFailure(Call<ContactResponse> call, Throwable t) {
                        try {
                            if (refreshLayout.isRefreshing())
                                refreshLayout.setRefreshing(false);
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }

                        DisplayNotice.displayOnFailure(getActivity());
                    }

                });
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception: " + e);
            }
        } else {
            refreshLayout.setRefreshing(false);
        }

    }

    class ContactsListAdapter extends BaseAdapter implements SectionIndexer {
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
            public ImageView avatar;
            public ImageButton imgCall;
            public ImageButton imgDelete;
            public ImageButton imgEdit;
            public RelativeLayout rlDeleteBar;
            public CheckBox cbxDelete;
            private boolean isChoose;
            private RelativeLayout llContainer;
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
                imgDelete = (ImageButton) view.findViewById(R.id.delete_contact);
                cbxDelete = view.findViewById(R.id.cbx_delete);
                layout = view.findViewById(R.id.layout);
                avatar = view.findViewById(R.id.mask);
                llContainer = view.findViewById(R.id.contacts_container);
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
                case LOCAL_CONTACT:
                    return contacts.size();

                default:
                    return dsDanhBas.size();


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
            ContactResponse.DSDanhBa danhBa = null;
            if (onlyDisplayLinphoneContacts != LOCAL_CONTACT) {
                danhBa = dsDanhBas.get(position);
            }
            final LinphoneContact contact = (LinphoneContact) getItem(position);
//            if (contact == null) return null;
            ViewHolder holder = null;

            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = mInflater.inflate(R.layout.contact_cell, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            if (view.getId() == R.id.layout) {
                if (onlyDisplayLinphoneContacts == 0) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.llContainer.getLayoutParams();
                    layoutParams.setMargins(5, 24, 5, 24);
                    holder.llContainer.setLayoutParams(layoutParams);
                } else {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.llContainer.getLayoutParams();
                    layoutParams.setMargins(5, 12, 5, 12);
                    holder.llContainer.setLayoutParams(layoutParams);
                }
                // giu nguyen trang thai check box moi lan adapter thay doi
                if (onlyDisplayLinphoneContacts != 0) {
                    if (listIdDelete.indexOf(danhBa.getIddanhba()) != -1) {
                        Log.d(TAG, "getIddanhba: " + listIdDelete.toString());
                        holder.cbxDelete.setChecked(true);
                    } else {
                        holder.cbxDelete.setChecked(false);
                    }
                }

                final ViewHolder finalHolder = holder;
                if (isDeleteAll) {
                    holder.cbxDelete.setChecked(isDeleteAll);
                }

                if (isDeleteMode) {
                    allExt.setVisibility(View.GONE);
                    onlExt.setVisibility(View.GONE);
                    offExt.setVisibility(View.GONE);
                    holder.cbxDelete.setVisibility(View.VISIBLE);
                    holder.imgCall.setVisibility(View.GONE);
                } else {

//                holder.imgCall.setVisibility(View.VISIBLE);
                    holder.cbxDelete.setVisibility(View.GONE);
                }


                holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Log.d(TAG, "onLongClick: ");
                        if (onlyDisplayLinphoneContacts != 0) {
                            isDeleteMode = true;
                            changeMode(finalHolder, isDeleteMode);
                            addContacts.setVisibility(View.GONE);
                        }
                        return true;
                    }
                });
//            holder.swLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
//            holder.swLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_view));


                holder.cbxDelete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onCheckedChanged: ");
                        int iddanhba = 0;
                        iddanhba = dsDanhBas.get(position).getIddanhba();
                        if (finalHolder.cbxDelete.isChecked()) {
                            finalHolder.cbxDelete.setChecked(true);
                            listIdDelete.add(iddanhba);
                            Log.d(TAG, "onCheckedChanged: " + listIdDelete.toString());
                        } else {
                            finalHolder.cbxDelete.setChecked(false);
                            listIdDelete.remove(listIdDelete.indexOf(iddanhba));
                        }
                        if (listIdDelete.size() == 0) {
                            deleteContact.setVisibility(View.GONE);
                        } else {
                            deleteContact.setVisibility(View.VISIBLE);
                        }
                                                        }
                                                    }

                );
//            holder.imgDelete.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    AlertDialog.Builder builder;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
//                    } else {
//                        builder = new AlertDialog.Builder(getActivity());
//                    }
//                    try {
//                        builder.setTitle("Xóa")
//                                .setMessage("Bạn có thật sự muốn xóa liên hệ này ?")
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        deleteContact(DbContext.getInstance().getContactResponse(getActivity()).getDsdanhba().get(position).getIddanhba(), position);
//                                    }
//                                })
//                                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // do nothing
//                                    }
//                                })
//                                .setIcon(R.drawable.ic_delete_black_24dp)
//                                .show();
//
//                    } catch (Exception e) {
//
//                    }
//                }
//            });
                final ContactResponse.DSDanhBa finalDanhBa = danhBa;
                holder.layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                    LinphoneContact contact = (LinphoneContact) getItem(position);
//                    ;
//                    if (onlyDisplayLinphoneContacts == 0) {
//                        if (editOnClick) {
//                            editConsumed = true;
//                            LinphoneActivity.instance().editContact(contact, sipAddressToAdd);
//                        } else {
//                            lastKnownPosition = contactsList.getFirstVisiblePosition();
//                            LinphoneActivity.instance().displayContact(contact, onlyDisplayChatAddress);
//                        }
//                    }
//                    if (onlyDisplayLinphoneContacts == 2) {
//                        Intent editIntent = new Intent(getActivity(), CusContactsActivity.class);
//                        editIntent.putExtra("tenlienhe", finalDanhBa.getTenlienhe());
//                        editIntent.putExtra("sodienthoai", finalDanhBa.getSodienthoai());
//                        editIntent.putExtra("iddanhba", finalDanhBa.getIddanhba());
//                        startActivity(editIntent);
//                    }
                        if (!isDeleteMode) {
                            if (onlyDisplayLinphoneContacts == 0) {

                                String phoneNumber = contacts.get(position).getNumbersOrAddresses().get(0).getValue();

                                if (phoneNumber.contains("+84")) {
                                    phoneNumber = "0" + phoneNumber.substring(3);
                                }
                                // xóa hết dấu cách trong số điện thoại
                                phoneNumber = phoneNumber.replaceAll(" ", "");
                                String uri = "sip:" + phoneNumber + "@" + LinphonePreferences.instance().getAccountDomain(0);

                                LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, contacts.get(position).getFullName(), null);
                            } else if (onlyDisplayLinphoneContacts == 1) {
                                try {
                                    ContactResponse.DSDanhBa to;
                                    to = dsDanhBas.get(position);
                                    String uri = "sip:" + to.getSodienthoai() + "@" + LinphonePreferences.instance().getAccountDomain(0);
                                    LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, to.getTenlienhe(), null);
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                            } else {
                                try {
                                    ContactResponse.DSDanhBa to;
                                    if (searchText.equals(""))
                                        to = DbContext.getInstance().getCusContactResponse(getActivity()).getDsdanhba().get(position);
                                    else
                                        to = DbContext.getInstance().getSearchContactResponse(getActivity()).getDsdanhba().get(position);
                                    String uri = "sip:" + to.getSodienthoai() + "@" + LinphonePreferences.instance().getAccountDomain(0);
                                    LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, to.getTenlienhe(), null);
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                            }
                        }
                    }
                });
//            holder.imgCall.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (onlyDisplayLinphoneContacts == 0) {
//                        String phoneNumber = contacts.get(position).getNumbersOrAddresses().get(0).getValue();
//                        if (phoneNumber.contains("+84")) {
//                            phoneNumber = "0" + phoneNumber.substring(3);
//                        }
//                        String uri = "sip:" + phoneNumber + "@" + LinphonePreferences.instance().getAccountDomain(0);
//                        LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, contacts.get(position).getFullName(), null);
//                    } else if (onlyDisplayLinphoneContacts == 1) {
//                        try {
//                            ContactResponse.DSDanhBa to;
//                            if (searchText.equals(""))
//                                to = DbContext.getInstance().getContactResponse(getActivity()).getDsdanhba().get(position);
//                            else
//                                to = DbContext.getInstance().getSearchContactResponse(getActivity()).getDsdanhba().get(position);
//                            String uri = "sip:" + to.getSodienthoai() + "@" + LinphonePreferences.instance().getAccountDomain(0);
//                            LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, to.getTenlienhe(), null);
//                        } catch (Exception e) {
//                            Log.d(TAG, "Exception: " + e.toString());
//                        }
//                    } else {
//                        try {
//                            ContactResponse.DSDanhBa to;
//                            if (searchText.equals(""))
//                                to = DbContext.getInstance().getCusContactResponse(getActivity()).getDsdanhba().get(position);
//                            else
//                                to = DbContext.getInstance().getSearchContactResponse(getActivity()).getDsdanhba().get(position);
//                            String uri = "sip:" + to.getSodienthoai() + "@" + LinphonePreferences.instance().getAccountDomain(0);
//                            LinphoneActivity.instance().setAddresGoToDialerAndCall(uri, to.getTenlienhe(), null);
//                        } catch (Exception e) {
//                            Log.d(TAG, "Exception: " + e.toString());
//                        }
//                    }
//
//
//                }
//            });
                if (onlyDisplayLinphoneContacts == 0 && contact != null) {
                    holder.name.setText(contact.getFullName());
                    try {
                        holder.address.setText(contact.getNumbersOrAddresses().get(0).getValue());
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }

                    holder.organization.setVisibility(View.GONE);
                } else if (onlyDisplayLinphoneContacts == 1) {
                    try {
                        Log.d(TAG, "getViewonlyDisplayLinphoneContacts: " + onlyDisplayLinphoneContacts);
                        if (DbContext.getInstance().getLoginRespon(view.getContext()).getData().getChophepxemonoffext().equals("true")) {
                            if (danhBa.isStatus()) {
                                holder.avatar.setImageResource(R.drawable.online_info_icon_medium);
                            } else {
                                holder.avatar.setImageResource(R.drawable.info_icon_medium);
                            }
                        }
                        holder.name.setText(dsDanhBas.get(position).getTenlienhe());
                        holder.organization.setVisibility(View.VISIBLE);
                        holder.address.setText(dsDanhBas.get(position).getSodienthoai());
                        holder.organization.setText(dsDanhBas.get(position).getJob());
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                } else {
                    try {
                        Log.d(TAG, "getViewonlyDisplayLinphoneContacts: " + onlyDisplayLinphoneContacts);
                        holder.name.setText(dsDanhBas.get(position).getTenlienhe());
                        holder.organization.setVisibility(View.GONE);
                        holder.address.setText(dsDanhBas.get(position).getSodienthoai());
                        holder.organization.setText(dsDanhBas.get(position).getJob());
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }

                }

                if (!isSearchMode) {
                    if (getPositionForSection(getSectionForPosition(position)) != position) {
                        holder.separator.setVisibility(View.GONE);
                    } else {
                        holder.separator.setVisibility(View.GONE);
                        String fullName = "";
                        if (onlyDisplayLinphoneContacts == 0 && contact != null)
                            fullName = contact.getFullName();
                        else
                            try {
                                fullName = dsDanhBas.get(position).getTenlienhe();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
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
            }
            return view;
        }

        private void changeMode(ViewHolder holder, boolean isDeleteMode) {
            if (isDeleteMode) {
                deleteBar.setVisibility(View.VISIBLE);
                topbar.setVisibility(View.GONE);
                backDeleteMode.setVisibility(View.VISIBLE);
                deleteAll.setVisibility(View.VISIBLE);

            }
            changeAdapter();
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
