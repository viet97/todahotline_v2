package org.linphone;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.pepperonas.materialdialog.MaterialDialog;
import com.rey.material.widget.Switch;

import org.linphone.assistant.AssistantActivity;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.PayloadType;
import org.linphone.database.DbContext;
import org.linphone.myactivity.InfoActivityMain;
import org.linphone.myactivity.LoginActivity;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.AboutRespon;
import org.linphone.network.models.VoidRespon;
import org.linphone.notice.DisplayNotice;
import org.linphone.ultils.StringUltils;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.todahotline.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {


    private static final String Pref_String_DB = "baseUrl";
    public static final String Pref_Language_DB = "language";
    public static final String Pref_Codec_DB = "codec";
    private static final int ENGLISH_SELECTED = 0;
    private static final int VIETNAMESE_SELECTED = 1;
    private static final int G729_SELECTED = 1;
    private static final int G711_SELECTED = 0;
    public static final String KEY_FUNC_URL = "AppLogOut.aspx?";
    private ProgressDialog dialogLogin;
    private String TAG = "Profile";
    private String idnv;
    private int idct;
    private String IPSV;
    private int languageSelected = 0;
    private int codecSelected = 0;

    TextView tenlienhe;
    TextView diachi;
    TextView dienthoai1;
    TextView dienthoai2;
    TextView hotline;
    TextView cskh;
    TextView website;
    TextView email;
    ImageView backImg;


    public Profile() {
        // Required empty public constructor
    }

    RelativeLayout ll1, ll2, ll3, ll4, ll5;
    TextView tvName, tvSipAddress, tvJob;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ll1 = view.findViewById(R.id.llprofile_info);
        ll2 = view.findViewById(R.id.llprofile_changepass);
        ll3 = view.findViewById(R.id.llprofile_logout);
        ll4 = view.findViewById(R.id.ll_switch_language);
        ll5 = view.findViewById(R.id.ll_switch_codecs);
        tvName = view.findViewById(R.id.tv_profilename);
        tvJob = view.findViewById(R.id.tv_job);
        tvSipAddress = view.findViewById(R.id.tv_sipAddress);
        try {
            tvName.setText(DbContext.getInstance().getLoginRespon(getActivity()).getData().getTennhanvien());
            tvSipAddress.setText(DbContext.getInstance().getLoginRespon(getActivity()).getData().getSomayle());
            if (DbContext.getInstance().getLoginRespon(getActivity()).getData().getChucvu() != null) {
                tvJob.setText(DbContext.getInstance().getLoginRespon(getActivity()).getData().getChucvu());
                tvJob.setVisibility(View.VISIBLE);
            } else {
                tvJob.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }


        try {

            tenlienhe = (TextView) view.findViewById(R.id.tv_tenlienhe);
            diachi = (TextView) view.findViewById(R.id.tv_diachi);
            dienthoai1 = (TextView) view.findViewById(R.id.tv_dienthoai1);
            dienthoai2 = (TextView) view.findViewById(R.id.tv_dienthoai2);
            hotline = (TextView) view.findViewById(R.id.tv_hotline);
            cskh = (TextView) view.findViewById(R.id.tv_cskh);
            website = (TextView) view.findViewById(R.id.tv_website);
            email = (TextView) view.findViewById(R.id.tv_email);
//            if (DbContext.getInstance().getAboutRespon(getActivity()) == null) {
                getAbout();
//            } else {
//                setDataNotEmpty(DbContext.getInstance().getAboutRespon(getActivity()));
//            }


        } catch (Exception e) {
            Log.d("Info", "Exception: " + e);
        }

//                Log.d(TAG, "onClick: " + switchLanguage.isChecked());
//                if (!switchLanguage.isChecked()) {
//                    switchLanguage.setChecked(false);
//                    Locale myLocale = new Locale("vi");
//                    //saveLocale(lang, activity);
//                    Locale.setDefault(myLocale);
//                    android.content.res.Configuration config = new android.content.res.Configuration();
//                    config.locale = myLocale;
//                    getActivity().getResources().updateConfiguration(config,
//                            getActivity().getResources().getDisplayMetrics());
//                    SharedPreferences.Editor languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE).edit();
//                    languagePrefs.putBoolean("en", false);
//                    languagePrefs.commit();
//                    getActivity().recreate();
//
//                } else {
//                    switchLanguage.setChecked(true);
//                    Locale myLocale = new Locale("en");
//                    //saveLocale(lang, activity);
//                    switchLanguage.setChecked(false);
//                    Locale.setDefault(myLocale);
//                    android.content.res.Configuration config = new android.content.res.Configuration();
//                    config.locale = myLocale;
//                    getActivity().getResources().updateConfiguration(config,
//                            getActivity().getResources().getDisplayMetrics());
//                    SharedPreferences.Editor languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE).edit();
//                    languagePrefs.putBoolean("en", true);
//                    languagePrefs.commit();
//                    getActivity().recreate();
//                }


        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration(10);//duration in millisecond
                ll1.startAnimation(alphaAnimation);
                startActivity(new Intent(getActivity(), InfoActivityMain.class));
            }
        });
        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration(10);//duration in millisecond
                ll2.startAnimation(alphaAnimation);
                startActivity(new Intent(getActivity(), AssistantActivity.class));
            }
        });
        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration(10);//duration in millisecond
                ll3.startAnimation(alphaAnimation);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }
                try {
                    builder.setTitle(getString(R.string.dialog_logout_title))
                            .setMessage(getString(R.string.dialog_logout_message_builder))
                            .setPositiveButton(getString(R.string.confirm_dialog), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    logoutAct();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(R.drawable.logout1)
                            .show();
                    Log.d("LinphoneActivity", "onBackPressed: ");
                } catch (Exception e) {
                    Log.d("SipHome", "Exception: " + e);
                }
            }
        });

        ll4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] ITEMSLANGUE = new String[]{getString(R.string.EngLish), getString(R.string.Vietnamese)};

                showMaterialDialogListSingleChoiceLanguage(getString(R.string.Language), ITEMSLANGUE);
            }
        });

        ll5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] ITEMSCODECS = new String[]{"G711", "G729"};
                showMaterialDialogListSingleChoiceCodec(getString(R.string.Codec), ITEMSCODECS);
            }
        });
        return view;
    }

    public void setDataNotEmpty(AboutRespon aboutRespon) {
        try {
            if (aboutRespon.getData().getTenlienhe().length() != 0) {
                tenlienhe.setText(aboutRespon.getData().getTenlienhe());
                Log.d(TAG, "setDataNotEmpty: " + aboutRespon.getData().getTenlienhe());
                tenlienhe.setVisibility(View.VISIBLE);
            } else tenlienhe.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {
            if (aboutRespon.getData().getDiachi().length() != 0) {
                diachi.setText(aboutRespon.getData().getDiachi());
                diachi.setVisibility(View.VISIBLE);
            } else diachi.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {
            if (aboutRespon.getData().getEmail().length() != 0) {
                email.setText(aboutRespon.getData().getEmail());
                email.setVisibility(View.VISIBLE);
            } else email.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {
            if (aboutRespon.getData().getHotline().length() != 0) {
                hotline.setText(aboutRespon.getData().getHotline());
                hotline.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else hotline.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {
            if (aboutRespon.getData().getDienthoai1().length() != 0) {
                dienthoai1.setText(aboutRespon.getData().getDienthoai1());
                dienthoai1.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else dienthoai1.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());

        }

        try {
            if (aboutRespon.getData().getDienthoai2().length() != 0) {
                dienthoai2.setText(aboutRespon.getData().getDienthoai2());
                dienthoai2.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else dienthoai2.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {

            if (aboutRespon.getData().getCskh().length() != 0) {
                cskh.setText(aboutRespon.getData().getCskh());
                cskh.setVisibility(View.VISIBLE);
            } else cskh.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

        try {
            if (aboutRespon.getData().getWebsite().length() != 0) {
                website.setText(aboutRespon.getData().getWebsite());
                website.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else website.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

    }

    public String getURLAbout() {
        String URL;
        final SharedPreferences languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE);
        if (languagePrefs.getInt(Pref_Language_DB, ENGLISH_SELECTED) == VIETNAMESE_SELECTED) {
            URL = "AppLienHe.aspx?lang=vi";
        } else {
            URL = "AppLienHe.aspx?lang=en";
        }
        Log.d(TAG, "getURLAbout: " + URL);
        return URL;

    }

    private void showMaterialDialogListSingleChoiceLanguage(String title, String[] ITEMS) {
        final SharedPreferences languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE);
        languageSelected = languagePrefs.getInt(Pref_Language_DB, ENGLISH_SELECTED);


        new MaterialDialog.Builder(getActivity())
                .title(title)
                .message(null)
                .positiveText(getActivity().getString(R.string.confirm_dialog))
                .negativeText(getActivity().getString(R.string.cancel_dialog))
                .listItemsSingleSelection(false, ITEMS)
                .selection(languageSelected)
                .itemClickListener(new MaterialDialog.ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, long id) {
                        super.onClick(v, position, id);
                        languageSelected = position;
                    }
                })

                .buttonCallback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Locale myLocale;
                        if (languageSelected == ENGLISH_SELECTED) {
                            myLocale = new Locale("en");
                            //saveLocale(lang, activity);


                        } else {
                            myLocale = new Locale("vi");
                            //saveLocale(lang, activity);


                        }
                        Locale.setDefault(myLocale);
                        android.content.res.Configuration config = new android.content.res.Configuration();
                        config.locale = myLocale;
                        getActivity().getResources().updateConfiguration(config,
                                getActivity().getResources().getDisplayMetrics());


//                        LinphoneActivity.instance().newMessages.setVisibility(View.GONE);
                        SharedPreferences.Editor edit = languagePrefs.edit();
                        edit.putInt(Pref_Language_DB, languageSelected);
                        edit.commit();
                        getActivity().recreate();
//                        getAbout();
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        languageSelected = languagePrefs.getInt(Pref_Language_DB, ENGLISH_SELECTED);
                    }
                })
                .show();
    }

    private void showMaterialDialogListSingleChoiceCodec(String title, String[] ITEMS) {
        final SharedPreferences codecPrefs = getActivity().getSharedPreferences(Pref_Codec_DB, getActivity().MODE_PRIVATE);
        codecSelected = codecPrefs.getInt(Pref_Codec_DB, G711_SELECTED);
        new MaterialDialog.Builder(getActivity())
                .title(title)
                .message(null)
                .positiveText(getActivity().getString(R.string.confirm_dialog))
                .negativeText(getActivity().getString(R.string.cancel_dialog))
                .listItemsSingleSelection(false, ITEMS)
                .selection(codecSelected)
                .itemClickListener(new MaterialDialog.ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, long id) {
                        super.onClick(v, position, id);
                        codecSelected = position;
                    }
                })
                .showListener(new MaterialDialog.ShowListener() {
                    @Override
                    public void onShow(AlertDialog dialog) {
                        super.onShow(dialog);
                    }
                })
                .buttonCallback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
                        if (codecSelected == G729_SELECTED) {
                            for (final PayloadType pt : lc.getAudioCodecs()) {
                                if (pt.getMime().equals("G729") || pt.getMime().equals("PCMU")) {
                                    try {
                                        lc.enablePayloadType(pt, true);
                                    } catch (LinphoneCoreException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (pt.getMime().equals("PCMA")) {
                                    try {
                                        lc.enablePayloadType(pt, false);
                                    } catch (LinphoneCoreException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            if (codecSelected == G711_SELECTED) {
                                for (final PayloadType pt : lc.getAudioCodecs()) {
                                    if (pt.getMime().equals("G729") || pt.getMime().equals("PCMU")) {
                                        try {
                                            lc.enablePayloadType(pt, false);
                                        } catch (LinphoneCoreException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (pt.getMime().equals("PCMA")) {
                                        try {
                                            lc.enablePayloadType(pt, true);
                                        } catch (LinphoneCoreException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }
                        }
                        SharedPreferences.Editor edit = codecPrefs.edit();
                        edit.putInt(Pref_Codec_DB, codecSelected);
                        edit.commit();
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        codecSelected = codecPrefs.getInt(Pref_Codec_DB, G711_SELECTED);
                    }
                })
                .show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.CHAT);
        }
    }

    public void getAbout() {
        try {

            Service userService = NetContext.instance.create(Service.class);
            userService.getAbout(getURLAbout()).enqueue(new Callback<AboutRespon>() {
                @Override
                public void onResponse(Call<AboutRespon> call, Response<AboutRespon> response) {

                    AboutRespon aboutRespon = response.body();
                    if (aboutRespon.isStatus()) {
                        try {
                            DbContext.getInstance().setAboutRespon(aboutRespon, getActivity());

                            Log.d(TAG, "onResponse: " + DbContext.getInstance().getAboutRespon(getActivity()).getData().getDiachi());
                            setDataNotEmpty(aboutRespon);
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }

                    } else {
                        Toast.makeText(getActivity(), "Lấy thông tin thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AboutRespon> call, Throwable t) {
                    DisplayNotice.displayOnFailure(getActivity());
                    Log.d("LoggerInterceptor", "onFailure: " + t.toString());
                    Log.d("LoggerInterceptor", "onFailure: " + call.toString());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
    }


    private void logoutAct() {
        String logoutURL = KEY_FUNC_URL
                + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien()
                + "&hinhthucdangxuat=0"
                + "&imei=" + Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);      //0 la chu dong  1 la bi dong
        dialogLogin = ProgressDialog.show(getActivity(), "", "Đăng xuất...", true, false);

        final Service service = NetContext.instance.create(Service.class);
        service.dangxuat(logoutURL).enqueue(new Callback<VoidRespon>() {
            @Override
            public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {

                VoidRespon voidRespon = response.body();
                boolean logOutResponse = voidRespon.getStatus();
                if (!logOutResponse) {
                    dialogLogin.cancel();
                    Toast.makeText(getActivity(), getString(R.string.occured_error), Toast.LENGTH_SHORT).show();
                } else {

                    try {
                        if (LinphonePreferences.instance().getAccountCount() > 0) {
                            LinphonePreferences.instance().setAccountEnabled(0, false);
                            int accountNumber = LinphonePreferences.instance().getAccountCount();
                            while (accountNumber >= 0) {

                                LinphonePreferences.instance().deleteAccount(accountNumber);
                                accountNumber--;
                            }
                        }

//                    LocalBroadcastManager.getInstance(SipHome.this).unregisterReceiver(statusReceiver);

//                                        finish();
//                                        android.util.Log.d("SipHome", "registerBroadcasts: " + StaticForDynamicReceiver4.getInstance().deviceStateReceiver);

                        SharedPreferences.Editor autoLoginEditor = getActivity().getSharedPreferences("AutoLogin", getActivity().MODE_PRIVATE).edit();
                        autoLoginEditor.putBoolean("AutoLogin", false);
                        autoLoginEditor.apply();
                        SharedPreferences.Editor databasePref = getActivity().getSharedPreferences(Pref_String_DB, getActivity().MODE_PRIVATE).edit();
                        databasePref.clear();
                        databasePref.commit();
                        dialogLogin.cancel();
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("TodaPhone");
                        getActivity().stopService(new Intent(Intent.ACTION_MAIN).setClass(getActivity(), LinphoneService.class));
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
//                        getActivity().finish();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<VoidRespon> call, Throwable t) {
                try {
                    dialogLogin.cancel();
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
                DisplayNotice.displayOnFailure(getActivity());
            }

        });

    }


}
