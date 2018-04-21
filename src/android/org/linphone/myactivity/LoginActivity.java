package org.linphone.myactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneLauncherActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.LinphoneUtils;
import org.linphone.R;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.LoginRespon;
import org.linphone.network.models.NonTodaContactsResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {
    private static String TODA = "";
    public static String DEFAULT_BASE = "42.112.31.63:10001";
    public static EditText et_username, et_password, et_idct;
    private Button bt_login;
    private CheckBox cbx_remember;
    private Boolean saveLogin;
    private ImageView visibility;
    private ImageView im_config;
    EditText et_config;
    AlertDialog.Builder loginError;
    private static SharedPreferences accountSelected, autoLogin, config, loginPreferences, listContactTodaPreferences;
    private SharedPreferences.Editor internalIpEditor, autoLoginEditor, configEditor, loginEditor, listContactTodaEditor;
    public static final String PREF_URLCONFIG = "urlconfig";
    public boolean isShowPassWord = false;
    private ProgressDialog dialogLogin;
    private NotificationManager mgr;
    private static final int NOTIFY_ME_ID = 1337;


    public final String KEY_FUNC_URL = "AppLogin.aspx?";
    private String loginURL;
    private static final String KEYLACHONG = "!lac@hong#media$";
    public LinphonePreferences mPrefs;
    public static final String TAG = "LoginActivity";

    public static long ACCOUNT_ID = 0;


    public void getContactToda() {
        try {
            Service contactService = NetContext.instance.create(Service.class);
            String urlContact = "AppDanhBa.aspx?idct=" + DbContext.getInstance().getLoginRespon(this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(this).getData().getIdnhanvien() + "&page=-1";//lay tat ca danh ba ra
            android.util.Log.d("GetconTact", "getContactToda: " + urlContact);
            contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                @Override
                public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                    ContactResponse contactResponse = new ContactResponse();
                    contactResponse = response.body();
                    if (contactResponse != null) {
                        if (contactResponse.getStatus()) {
                            ArrayList<ContactResponse.DSDanhBa> listDB = contactResponse.getDsdanhba();
                            DbContext.getInstance().setContactResponse(contactResponse, LoginActivity.this);
                            HashMap<String, String> itemContactName = new HashMap<String, String>();
                            HashMap<String, String> itemContactJob = new HashMap<String, String>();
                            for (ContactResponse.DSDanhBa ds : listDB) {
                                itemContactName.put(ds.getSodienthoai(), ds.getTenlienhe());
                                itemContactJob.put(ds.getSodienthoai(), ds.getJob());
                            }
                            DbContext.getInstance().setListContactTodaName(itemContactName, LoginActivity.this);
                            DbContext.getInstance().setListContactTodaJob(itemContactJob);
//                        if (SipHome.mViewPager != null)
//                            SipHome.mViewPager.refreshDrawableState();
                            //save vào trong sharedPreferences
                            Gson gson = new Gson();
                            String listContactTodaHashMapStringName = gson.toJson(itemContactName);
                            String listContactTodaHashMapStringJob = gson.toJson(itemContactJob);
                            android.util.Log.d("CheckContact", "onResponse: ");
                            listContactTodaEditor.putString("listContactTodaName", listContactTodaHashMapStringName);
                            listContactTodaEditor.putString("listContactTodaJob", listContactTodaHashMapStringJob);
                            listContactTodaEditor.apply();
                        } else {
                            listContactTodaEditor.clear();
                            listContactTodaEditor.apply();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ContactResponse> call, Throwable t) {
                    try {
                        Toast.makeText(LoginActivity.this,
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

    public void getCusContactToda() {
        try {
            String urlContact;
            urlContact = "AppDanhBaKhachHang.aspx?idct=" + DbContext.getInstance().getLoginRespon(this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(this).getData().getIdnhanvien() + "&lastID=0" + "&timkiem=";//lay tat ca danh ba ra
            Service contactService = NetContext.instance.create(Service.class);
            android.util.Log.d(TAG, "getCusContactToda: " + urlContact);
            contactService.getDanhBa(urlContact).enqueue(new Callback<ContactResponse>() {
                @Override
                public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                    ContactResponse contactResponse;
                    contactResponse = response.body();
                    if (contactResponse.getStatus()) {
                        ArrayList<ContactResponse.DSDanhBa> listDB = contactResponse.getDsdanhba();
                        DbContext.getInstance().setCusContactResponse(contactResponse, LoginActivity.this);
                        HashMap<String, String> itemCusContactName = new HashMap<String, String>();
                        for (ContactResponse.DSDanhBa ds : listDB) {
                            itemCusContactName.put(ds.getSodienthoai(), ds.getTenlienhe());
                        }
                        DbContext.getInstance().setListCusContactTodaName(itemCusContactName, LoginActivity.this);

                    }
                }

                @Override
                public void onFailure(Call<ContactResponse> call, Throwable t) {

                    try {
                        Toast.makeText(LoginActivity.this,
                                "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        android.util.Log.d(TAG, "Exception: " + e.toString());
                    }
                }

            });
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }

    }

    @Override
    protected void onResume() {

        LinphoneLauncherActivity.isLinphoneActivity = false;
        LinphonePreferences mPrefs = LinphonePreferences.instance();
        if (mPrefs.getAccountCount() > 0) {
            int accountNumber = mPrefs.getAccountCount();
            LinphonePreferences.instance().setAccountEnabled(0, false);
            while (accountNumber >= 0) {
                mPrefs.deleteAccount(accountNumber);
                accountNumber--;
            }
        }
        if (!LinphoneService.isReady()) {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
        super.onResume();

    }

    public void getNonTodaContacts() {
        try {
            Service contactService = NetContext.instance.create(Service.class);
            String urlContact;
            urlContact = "AppDanhBaNoiBo.aspx?idct=" + DbContext.getInstance().getLoginRespon(LoginActivity.this).getData().getIdct()
                    + "&idnhanvien=" + DbContext.getInstance().getLoginRespon(LoginActivity.this).getData().getIdnhanvien() + "&lastID=0";//lay tat ca danh ba ra

            contactService.getNonTodaDanhBa(urlContact).enqueue(new Callback<NonTodaContactsResponse>() {
                @Override
                public void onResponse(Call<NonTodaContactsResponse> call, Response<NonTodaContactsResponse> response) {
                    NonTodaContactsResponse nonTodaContactsResponse;
                    nonTodaContactsResponse = response.body();
                    if (nonTodaContactsResponse.isStatus()) {
                        DbContext.getInstance().setNonTodaContactsResponse(nonTodaContactsResponse, LoginActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<NonTodaContactsResponse> call, Throwable t) {

                    try {
                        Toast.makeText(LoginActivity.this,
                                "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        android.util.Log.d(TAG, "Exception: " + e.toString());
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }

    }

    public Animation getBlinkAnimation() {
        Animation animation = new AlphaAnimation(1, 0);         // Change alpha from fully visible to invisible
        animation.setDuration(300);                             // duration - half a second
        animation.setInterpolator(new LinearInterpolator());    // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE);                            // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);             // Reverse animation at the end so the button will fade back in

        return animation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = LinphonePreferences.instance();
        try {

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            internalIpEditor = getSharedPreferences("server", MODE_PRIVATE).edit();
            setContentView(R.layout.activity_login);

            InternetSpeedTest internetSpeedTest = new InternetSpeedTest();
            internetSpeedTest.execute("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG");
            DbContext.getInstance().getListContactTodaName(this).clear();
            DbContext.getInstance().getListContactTodaJob().clear();
            mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mgr.cancel(NOTIFY_ME_ID);
            im_config = (ImageView) findViewById(R.id.config_url);
            et_username = (EditText) findViewById(R.id.username);
            et_password = (EditText) findViewById(R.id.password);
            et_idct = (EditText) findViewById(R.id.idct);
            bt_login = (Button) findViewById(R.id.btnLogin);
            cbx_remember = (CheckBox) findViewById(R.id.cbxRemember);

            visibility = (ImageView) findViewById(R.id.visibility);


            config = getSharedPreferences(PREF_URLCONFIG, MODE_PRIVATE);
            configEditor = config.edit();
            loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            loginEditor = loginPreferences.edit();
            listContactTodaPreferences = getSharedPreferences("listContacTodaPrefs", MODE_PRIVATE);
            listContactTodaEditor = listContactTodaPreferences.edit();

            visibility.setImageResource(R.drawable.ic_visibility_black_24dp);

	/*	   Intent intentSSS = new Intent(Activity_Login.this, SendSttService.class);
            startService(intentSSS);*/

            //set Adapter for spinner
//            DSCongTyResponse dsCongTyResponse = DbContext.getInstance().getDsCongTy(this);
//            String[] idctItems = new String[dsCongTyResponse.getDscongty().size()];
//            for (int i = 0; i < dsCongTyResponse.getDscongty().size(); i++) {
//                idctItems[i] = dsCongTyResponse.getDscongty().get(i).getMacongty();
//            }
//            spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, idctItems);
//            spinnerAdapter.setDropDownViewResource(R.layout.checked_textview_spinner);
//            spinnerIdct.setAdapter(spinnerAdapter);
            //editor.putFloat(PREF_EARGAIN, (float) 0.8);
            //editor.putFloat(PREF_HEARGAIN, (float) 0.8);
            //editor.putFloat(PoREF_HMICGAIN, (float) 1.0);
            //editor.putFloat(PREF_MICGAIN, (float) 1.0);
            //editor.commit();

            //loginMess = new Dialog(this);
            //loginMess.setCancelable(false);
            String s = config.getString(PREF_URLCONFIG, "");
            if (!s.equals(DEFAULT_BASE)) {
                String urlConfig = "http://" + s + TODA;
                NetContext.getInstance().setBASE_URL(urlConfig);

            } else {
                String urlConfig = "http://" + s + TODA;
                NetContext.getInstance().setBASE_URL(urlConfig);

            }

            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            //long accountId = 0;
            loginError = new AlertDialog.Builder(this);
            loginError.setCancelable(false);
            saveLogin = loginPreferences.getBoolean("saveLogin", false);
            if (saveLogin) {

                et_username.setText(loginPreferences.getString("username", ""));
                et_password.setText(loginPreferences.getString("password", ""));
                et_idct.setText(loginPreferences.getString("server", ""));
                cbx_remember.setChecked(true);
                //  new sendSttTask().execute();

    /*        loginEditor.putBoolean("autoLogin", true);
            loginEditor.commit();*/
/*        	Intent intent2 = new Intent(Activity_Login.this,SipHome.class);
            startActivity(intent2);*/
            }
            im_config.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfigDialog();
                }
            });
            visibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowPassWord) {
                        visibility.setImageResource(R.drawable.ic_visibility_black_24dp);
                        et_password.setInputType(129);
                        isShowPassWord = false;
                    } else {
                        visibility.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                        et_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        isShowPassWord = true;
                    }
                }
            });
            bt_login.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

//                Basic.accDisplayName = et_username.getText().toString();
//                Basic.accServer = et_idct.getText().toString();
//                Basic.accUserName = et_username.getText().toString();
//                Basic.accPassword = et_password.getText().toString();
//                saveAccount("BASIC");

                    loginAct();

                }
            });
            config = getSharedPreferences(PREF_URLCONFIG, Context.MODE_PRIVATE);
            configEditor = config.edit();
//        LinearLayoutThatDetectsSoftKeyboard mainLayout = (LinearLayoutThatDetectsSoftKeyboard)findViewById(R.id.login_container);
//        mainLayout.setListener(this);
            autoLogin = getSharedPreferences("AutoLogin", MODE_PRIVATE);

            //tu dong dang nhap khi tat nong app
            if (LoginActivity.ACCOUNT_ID > 0 || autoLogin.getBoolean("AutoLogin", false)) {
                accountSelected = getSharedPreferences("AccountSelected", MODE_PRIVATE);
                ACCOUNT_ID = accountSelected.getLong("AccountSelected", 0);
                android.util.Log.d(TAG, "onCreate: " + ACCOUNT_ID);
                try {
                    loginAct();
                } catch (Exception e) {
                    android.util.Log.d(TAG, "Exception: " + e);
                }
            }
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }
    }


    private void showConfigDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(LoginActivity.this);
        }
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_config_dialog, null);
        builder.setView(view);
        et_config = (EditText) view.findViewById(R.id.et_config);

        et_config.setText(config.getString(PREF_URLCONFIG, DEFAULT_BASE));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!et_config.getText().toString().equals(DEFAULT_BASE)) {
                    String urlConfig = "http://" + et_config.getText() + TODA;
                    NetContext.getInstance().setBASE_URL(urlConfig);
                    configEditor.putString(PREF_URLCONFIG, et_config.getText().toString());
                    configEditor.commit();
                } else {
                    String urlConfig = "http://" + et_config.getText() + TODA;
                    NetContext.getInstance().setBASE_URL(urlConfig);
                    configEditor.putString(PREF_URLCONFIG, et_config.getText().toString());
                    configEditor.commit();
                }

            }
        })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d("swiping", "onPause: 123123");
    }

    public void loginAct() {
        if (!config.getString(PREF_URLCONFIG, "").equals("")) {
            dialogLogin = ProgressDialog.show(LoginActivity.this, "", "Đăng nhập...", true, false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (cbx_remember.isChecked()) {
                        loginEditor.putBoolean("saveLogin", true);
                        loginEditor.putString("username", et_username.getText().toString());
                        loginEditor.putString("password", et_password.getText().toString());
                        loginEditor.putString("server", et_idct.getText().toString());
                        loginEditor.apply();
                    } else {
                        loginEditor.clear();
                        loginEditor.apply();
                    }

                    if (!isNetworkAvailable(getBaseContext())) {
                        try {
                            if (dialogLogin.isShowing())
                                dialogLogin.cancel();
                        } catch (Exception e) {
                            android.util.Log.d(TAG, "Exception: " + e);
                        }
                        Toast.makeText(getBaseContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                    } else if (et_password.getText().toString().equals("")
                            || et_username.getText().toString().equals("")) {
                        try {
                            if (dialogLogin.isShowing())
                                dialogLogin.cancel();
                        } catch (Exception e) {
                            android.util.Log.d(TAG, "Exception: " + e);
                        }
                        Toast.makeText(getBaseContext(), "Thông tin đăng nhập không được để trống", Toast.LENGTH_SHORT).show();
                    } else {
                        NetContext.getInstance().init(LoginActivity.this);
                        android.util.Log.d(TAG, "run: " + NetContext.getInstance().getBASE_URL());
                        loginURL = KEY_FUNC_URL
                                + "token="
                                + "&idct=" + et_idct.getText().toString()
                                + "&taikhoan=" + et_username.getText().toString()
                                + "&matkhau=" + StringtoMD5(et_password.getText().toString())
                                + "&os=2"
                                + "&osversion=" + Build.VERSION.RELEASE
                                + "&dongmay=" + Build.DEVICE
                                + "&devicename=" + Build.DEVICE
                                + "&imei=" + Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
                                + "&ver=1.3.0.3"
                                + "&idpush=" + FirebaseInstanceId.getInstance().getToken();
                        android.util.Log.d(TAG, "loginURL: " + loginURL);
                        final Service service = NetContext.instance.create(Service.class);
                        service.login(loginURL).enqueue(new Callback<LoginRespon>() {
                            @Override
                            public void onResponse(Call<LoginRespon> call, Response<LoginRespon> response) {
                                LoginRespon loginRespon = response.body();
                                android.util.Log.d(TAG, "onResponse:512 " + response.body());
                                if (dialogLogin.isShowing())
                                    dialogLogin.cancel();
                                try {
                                    if (loginRespon.getStatus()) {
                                        loginEditor.apply();
                                        autoLoginEditor = autoLogin.edit();
                                        autoLoginEditor.putBoolean("AutoLogin", false);
                                        autoLoginEditor.apply();
                                        String userRespon = loginRespon.getData().getUsertoda();
                                        userRespon = new StringBuilder(userRespon).reverse().toString();
                                        DbContext.getInstance().setLoginRespon(loginRespon, LoginActivity.this);
                                        byte[] data = new byte[0];
                                        try {
                                            data = KEYLACHONG.getBytes("UTF-8");
                                            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                                            try {
                                                Jws<Claims> claims = Jwts.parser()
                                                        .setSigningKey(base64)
                                                        .parseClaimsJws(userRespon);
                                                android.util.Log.d("DangNhap", "onResponse: " + claims.toString());
                                                //3 cai can de dang nhap vao toda
                                                String user = (String) claims.getBody().get("user");
                                                String server = (String) claims.getBody().get("server");
                                                String pass = (String) claims.getBody().get("matkhautoda");
                                                String serverMain = "";
                                                internalIpEditor.putString("server", server);

                                                internalIpEditor.apply();

                                                //test config url
                                                getContactToda();
                                                getCusContactToda();
                                                getNonTodaContacts();

                                                //xoa nhung account cu di
                                                if (mPrefs.getAccountCount() > 0) {
                                                    int accountNumber = mPrefs.getAccountCount();
                                                    while (accountNumber >= 0) {
                                                        mPrefs.deleteAccount(accountNumber);
                                                        accountNumber--;
                                                    }
                                                }

                                                saveCreatedAccount(user, user, pass, loginRespon.getData().getTennhanvien(), null, null, server, null);


//
                                                //moi lan dang nhap la xoa luon cai tai khoan cu

                                            } catch (MissingClaimException e) {
                                                android.util.Log.d(TAG, "MissingClaimException: " + e);
                                                // we get here if the required claim is not present

                                            } catch (IncorrectClaimException e) {
                                                android.util.Log.d(TAG, "IncorrectClaimException: " + e);
                                                // we get here if the required claim has the wrong value

                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        messegeDialog(loginRespon.getMsg());
                                    }
                                } catch (Exception e) {
                                    if (dialogLogin.isShowing())
                                        dialogLogin.cancel();
                                }
                            }

                            @Override
                            public void onFailure(Call<LoginRespon> call, Throwable t) {
                                android.util.Log.d(TAG, "onFailure: ");
                                autoLoginEditor = autoLogin.edit();
                                autoLoginEditor.putBoolean("AutoLogin", false);
                                autoLoginEditor.apply();
                                if (dialogLogin.isShowing())
                                    dialogLogin.cancel();
                            }
                        });
                    }

                }
            }, 0);
        } else {
            showConfigDialog();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d(TAG, "onDestroy: ");

        try {
            stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
//            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//            am.killBackgroundProcesses(getString(R.string.sync_account_type));
//            android.os.Process.killProcess(android.os.Process.myPid());

            if (dialogLogin != null && dialogLogin.isShowing())
                dialogLogin.cancel();
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e.toString());
        }

        Intent startService = new Intent("com.example.helloandroid.alarms");
        sendBroadcast(startService);
    }

    public void saveCreatedAccount(String username, String userid, String password, String displayname, String ha1, String prefix, String domain, LinphoneAddress.TransportType transport) {
        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        android.util.Log.d(TAG, "saveCreatedAccount: 616");
        LinphonePreferences.AccountBuilder builder = null;
        try {
            builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                    .setUsername(username)
                    .setDomain(domain)
                    .setHa1(ha1)
                    .setUserId(userid)
                    .setDisplayName(displayname)
                    .setPassword(password);
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }
        android.util.Log.d(TAG, "saveCreatedAccount: 624");
        if (prefix != null) {
            builder.setPrefix(prefix);
        }

        String forcedProxy = "";
        if (!TextUtils.isEmpty(forcedProxy)) {
            builder.setProxy(forcedProxy)
                    .setOutboundProxyEnabled(true)
                    .setAvpfRRInterval(5);
        }
        if (transport != null) {
            builder.setTransport(transport);
        }

        try {
            builder.saveNewAccount();
//            if (!newAccount) {
//                displayRegistrationInProgressDialog();
//            }
//            accountCreated = true;
        } catch (LinphoneCoreException e) {
            Log.e(e);
        }
        android.util.Log.d(TAG, "saveCreatedAccount: 648");
        LinphoneLauncherActivity.isLinphoneActivity = true;

        startActivity(new Intent().setClass(this, LinphoneActivity.class));
        android.util.Log.d(TAG, "saveCreatedAccount: 650");
    }

    public boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting() && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Apply default settings for a new account to check very basic coherence of settings and auto-modify settings missing
     *
     * @param account
     */


    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(Intent.ACTION_MAIN);
        intent2.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent2);
    }

    public String StringtoMD5(String from) {
        String to = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {

        }
        digest.update(from.getBytes(), 0, from.length());
        to = new BigInteger(1, digest.digest()).toString(16);
        while (to.length() < 32) {
            to = "0" + to;
        }
        return to;
    }

    public void messegeDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Thông Báo");
        builder.setMessage(text);
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }

    private class InternetSpeedTest extends AsyncTask<String, Void, String> {

        long startTime;
        long endTime;
        private long takenTime;

        @Override
        protected String doInBackground(String... paramVarArgs) {

            startTime = System.currentTimeMillis();
            android.util.Log.d(TAG, "doInBackground: StartTime" + startTime);

            Bitmap bmp = null;
            try {
                URL ulrn = new URL(paramVarArgs[0]);
                HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);

                Bitmap bitmap = bmp;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 99, stream);
                byte[] imageInByte = stream.toByteArray();
                long lengthbmp = imageInByte.length;


                if (null != bmp) {
                    endTime = System.currentTimeMillis();
                    Log.d(TAG, "doInBackground: EndTIme" + endTime);
                    return lengthbmp + "";
                }
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception: " + e.toString());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            android.util.Log.d(TAG, "onPostExecute: " + result);
            if (result != null) {
                long dataSize = Integer.parseInt(result) / 1024;
                takenTime = endTime - startTime;
                double s = (double) takenTime / 1000;
                double speed = dataSize / s;
                android.util.Log.d(TAG, "onPostExecute: " + "" + new DecimalFormat("##.##").format(speed) + "kb/second");
                new InternetSpeedTest().execute("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG");
            }
        }
    }

}
