package org.linphone;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.rey.material.widget.Switch;

import org.linphone.assistant.AssistantActivity;
import org.linphone.database.DbContext;
import org.linphone.myactivity.InfoActivityMain;
import org.linphone.myactivity.LoginActivity;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.AboutRespon;
import org.linphone.network.models.VoidRespon;
import org.linphone.notice.DisplayNotice;
import org.linphone.ultils.StringUltils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.todahotline.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {


    private String Pref_String_DB = "baseUrl";
    private String Pref_Language_DB = "language";
    public static final String KEY_FUNC_URL = "AppLogOut.aspx?";
    private ProgressDialog dialogLogin;
    private String TAG = "Profile";
    private String idnv;
    private int idct;
    private String IPSV;

    TextView tenlienhe;
    TextView diachi;
    TextView dienthoai1;
    TextView dienthoai2;
    TextView hotline;
    TextView cskh;
    TextView website;
    TextView email;
    ImageView backImg;
    Switch switchLanguage;

    public Profile() {
        // Required empty public constructor
    }

    RelativeLayout ll1, ll2, ll3;
    TextView tvName, tvSipAddress, tvJob;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ll1 = view.findViewById(R.id.llprofile_info);
        ll2 = view.findViewById(R.id.llprofile_changepass);
        ll3 = view.findViewById(R.id.llprofile_logout);
        tvName = view.findViewById(R.id.tv_profilename);
        tvJob = view.findViewById(R.id.tv_job);
        tvSipAddress = view.findViewById(R.id.tv_sipAddress);
        switchLanguage = view.findViewById(R.id.switch_language);
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
            if (DbContext.getInstance().getAboutRespon(getActivity()).getData() == null) {
                getAbout();
            } else {
                setDataNotEmpty(DbContext.getInstance().getAboutRespon(getActivity()));
            }


        } catch (Exception e) {
            Log.d("Info", "Exception: " + e);
        }
        switchLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: " + switchLanguage.isChecked());
                if (!switchLanguage.isChecked()) {
                    switchLanguage.setChecked(false);
                    Locale myLocale = new Locale("vi");
                    //saveLocale(lang, activity);
                    Locale.setDefault(myLocale);
                    android.content.res.Configuration config = new android.content.res.Configuration();
                    config.locale = myLocale;
                    getActivity().getResources().updateConfiguration(config,
                            getActivity().getResources().getDisplayMetrics());
                    SharedPreferences.Editor languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE).edit();
                    languagePrefs.putBoolean("en", false);
                    languagePrefs.commit();
                    getActivity().recreate();

                } else {
                    switchLanguage.setChecked(true);
                    Locale myLocale = new Locale("en");
                    //saveLocale(lang, activity);
                    switchLanguage.setChecked(false);
                    Locale.setDefault(myLocale);
                    android.content.res.Configuration config = new android.content.res.Configuration();
                    config.locale = myLocale;
                    getActivity().getResources().updateConfiguration(config,
                            getActivity().getResources().getDisplayMetrics());
                    SharedPreferences.Editor languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE).edit();
                    languagePrefs.putBoolean("en", true);
                    languagePrefs.commit();
                    getActivity().recreate();
                }
            }
        });


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
                    builder.setTitle("Đăng xuất")
                            .setMessage("Bạn có thật sự muốn đăng xuất ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    logoutAct();
                                }
                            })
                            .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
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
        return view;
    }

    public void setDataNotEmpty(AboutRespon aboutRespon) {
        try {
            if (aboutRespon.getData().getTenlienhe().length() != 0) {
                tenlienhe.setText(aboutRespon.getData().getTenlienhe());
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
        String URL = "AppLienHe.aspx";
        return URL;

    }

    private void restartActivity() {
        Intent intent = getActivity().getIntent();
        intent.putExtra(LinphoneActivity.CHANGE_LANGUAGE, true);
        getActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            SharedPreferences languagePrefs = getActivity().getSharedPreferences(Pref_Language_DB, getActivity().MODE_PRIVATE);
            if (switchLanguage.isChecked() != languagePrefs.getBoolean("en", false)) {
                switchLanguage.setChecked(languagePrefs.getBoolean("en", false));
            }
            Log.d(TAG, "onResume: " + switchLanguage.isChecked());
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
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
                            setDataNotEmpty(DbContext.getInstance().getAboutRespon(getActivity()));
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
                    Toast.makeText(getActivity(), voidRespon.getMsg(), Toast.LENGTH_SHORT).show();
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
