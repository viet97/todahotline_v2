package org.linphone;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.todahotline.R;

import org.linphone.assistant.AssistantActivity;
import org.linphone.database.DbContext;
import org.linphone.myactivity.InfoActivityMain;
import org.linphone.myactivity.LoginActivity;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.VoidRespon;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {


    private String Pref_String_DB = "baseUrl";
    public static final String KEY_FUNC_URL = "AppLogOut.aspx?";
    private ProgressDialog dialogLogin;
    private String TAG = "Profile";

    public Profile() {
        // Required empty public constructor
    }

    LinearLayout ll1, ll2, ll3;
    TextView tvName, tvSipAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ll1 = view.findViewById(R.id.llprofile_info);
        ll2 = view.findViewById(R.id.llprofile_changepass);
        ll3 = view.findViewById(R.id.llprofile_logout);
        tvName = view.findViewById(R.id.tv_profilename);
        tvSipAddress = view.findViewById(R.id.tv_sipAddress);
        tvName.setText(DbContext.getInstance().getLoginRespon(getActivity()).getData().getTennhanvien());
        tvSipAddress.setText(DbContext.getInstance().getLoginRespon(getActivity()).getData().getSomayle());

        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration(10);//duration in millisecond
                ll1.startAnimation(alphaAnimation);
                startActivity(new Intent(getActivity(), InfoActivityMain.class));
            }
        });
        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration(10);//duration in millisecond
                ll2.startAnimation(alphaAnimation);
                startActivity(new Intent(getActivity(), AssistantActivity.class));
            }
        });
        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
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
                    android.util.Log.d("LinphoneActivity", "onBackPressed: ");
                } catch (Exception e) {
                    android.util.Log.d("SipHome", "Exception: " + e);
                }
            }
        });
        return view;
    }

    private void logoutAct() {
        String logoutURL = KEY_FUNC_URL
                + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(getActivity()).getData().getIdnhanvien()
                + "&hinhthucdangxuat=0";      //0 la chu dong  1 la bi dong
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
                        if (LinphonePreferences.instance().getAccountCount() >= 0) {
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
//					stopService(new Intent(Intent.ACTION_MAIN).setClass(LinphoneActivity.this, LinphoneService.class));
//					Intent intent = new Intent(LinphoneActivity.this, LoginActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(intent);
                        getActivity().finish();
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<VoidRespon> call, Throwable t) {
                try {
                    dialogLogin.cancel();
                } catch (Exception e) {

                }
                Toast.makeText(getActivity(),
                        "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                        Toast.LENGTH_SHORT).show();
            }

        });

    }


}
