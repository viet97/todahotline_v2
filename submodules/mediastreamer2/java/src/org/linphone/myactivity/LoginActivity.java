package org.linphone.myactivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.linphone.AccountPreferencesFragment;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneLauncherActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.LinphoneUtils;
import org.linphone.R;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.LoginRespon;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {
    private static String TODA = "/todahotline/";
    public static String DEFAULT_BASE = "42.112.31.63:10001";
    public boolean pingHost = false;
    public static EditText et_username, et_password, et_idct;
    private Button bt_login;
    private CheckBox cbx_remember;
    private Boolean saveLogin;
    private TextView tvVersion;
    private TextView tvHotline;
    private TextView tvWebsite;
    private ImageView visibility;
    private LinphoneAddress address;
    private ImageView im_config;
    EditText et_config;
    AlertDialog.Builder loginError;
    //Dialog loginMess;

    private static SharedPreferences accountSelected, autoLogin, config, settings, loginPreferences, accPre, listContactTodaPreferences;
    private SharedPreferences.Editor externalIpEditor, internalIpEditor, accountSelectedEditor, autoLoginEditor, configEditor, loginEditor, editor, editor2, listContactTodaEditor;
    private final String sharedPrefsFile = "vn.lachongmedia.tongdai_preferences";
    public static final String PREF_URLCONFIG = "urlconfig";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_SERVER = "server";
    public static final String PREF_DOMAIN = "domain";
    public static final String PREF_DNS = "dns0";
    public static final String PREF_EARGAIN = "eargain";
    public static final String PREF_MICGAIN = "micgain";
    public static final String PREF_HEARGAIN = "heargain";
    public static final String PREF_HMICGAIN = "hmicgain";
    public static boolean backToLoginFromSipHome = false;
    public boolean isShowPassWord = false;
    private ProgressDialog dialogLogin;

    private String wizardId = "Basic";
    private static final String THIS_FILE = "Login Activity";

    private NotificationManager mgr;
    private static final int NOTIFY_ME_ID = 1337;

    public static final String KEY_PROTOCOL = "http://42.112.31.63:10001";
    public final String KEY_FUNC_URL = "AppLogin.aspx?";
    private String loginURL;
    public static final String KEY_SIGN = "lh$toda@2014";
    private static final String KEYLACHONG = "!lac@hong#media$";
    private String deviceID;
    private String xmlLogin;
    private Document docLogin;
    private String urlConfig;
    private String sendSttURL;
    private String xmlStt;
    private Document docStt;
    private String sendSttCheck;
    private long accountId;
    public  LinphonePreferences mPrefs;
    private String loginCheck = "";
    private String ext = "default";
    private String ext_pwd;
    private String displayname;
    private String err_code;
    private String userid;
    private String sessionkey;
    public static final String TAG = "LoginActivity";
    public static final String KEY_LOGIN = "login";
    public static final String KEY_USERID = "userid";
    public static final String KEY_EXT = "ext";
    public static final String KEY_EXT_PWD = "ext_pwd";
    public static final String KEY_DISPLAY_NAME = "display_name";
    public static final String KEY_SESSION = "sessionkey";
    public static final String KEY_ERR_CODE = "err_code";
    public ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;

    public static long ACCOUNT_ID = 0;
    private SharedPreferences.Editor backToLoginFromSipHomeEditor;



    public void getContactToda(){
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
                    if (contactResponse.getStatus()) {
                        ArrayList<ContactResponse.DSDanhBa> listDB = contactResponse.getDsdanhba();
//                        DbContext.getInstance().setContactResponse(contactResponse, LoginActivity.this);
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

                @Override
                public void onFailure(Call<ContactResponse> call, Throwable t) {
                    try {
                        Toast.makeText(LoginActivity.this,
                                "Không có kết nối internet,vui lòng bật wifi hoặc 3g",
                                Toast.LENGTH_SHORT).show();
                    }catch (Exception e){

                    }
                }

            });
        } catch (Exception e) {
            android.util.Log.d(TAG, "Exception: " + e);
        }
    }

    @Override
    protected void onResume() {
        LinphoneLauncherActivity.isLinphoneActivity=false;
        LinphonePreferences mPrefs = LinphonePreferences.instance();
        if (mPrefs.getAccountCount()>0){
            int accountNumber = mPrefs.getAccountCount();
            LinphonePreferences.instance().setAccountEnabled(0,false);
            while(accountNumber>=0){
                mPrefs.deleteAccount(accountNumber);
                accountNumber--;
            }
        }
        if (!LinphoneService.isReady()) {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = LinphonePreferences.instance();
        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            internalIpEditor = getSharedPreferences("server", MODE_PRIVATE).edit();
            setContentView(R.layout.activity_login);
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
            tvWebsite = (TextView) findViewById(R.id.tv_website);
            tvHotline = (TextView) findViewById(R.id.tv_hotline);
            tvVersion = (TextView) findViewById(R.id.tv_version);
            visibility = (ImageView) findViewById(R.id.visibility);
//        tvWebsite.setText(DbContext.getInstance().getAboutRespon().getData().getWebsite());
//        tvHotline.setText(DbContext.getInstance().getAboutRespon().getData().getHotline());
            settings = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);

            config = getSharedPreferences(PREF_URLCONFIG, MODE_PRIVATE);
            configEditor = config.edit();
            loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            loginEditor = loginPreferences.edit();
            editor = settings.edit();
            listContactTodaPreferences = getSharedPreferences("listContacTodaPrefs", MODE_PRIVATE);
            listContactTodaEditor = listContactTodaPreferences.edit();
            accPre = getSharedPreferences("acc", MODE_PRIVATE);
            editor2 = accPre.edit();
            visibility.setImageResource(R.drawable.ic_visibility_black_24dp);

	/*	   Intent intentSSS = new Intent(Activity_Login.this, SendSttService.class);
            startService(intentSSS);*/


            //editor.putFloat(PREF_EARGAIN, (float) 0.8);
            //editor.putFloat(PREF_HEARGAIN, (float) 0.8);
            //editor.putFloat(PoREF_HMICGAIN, (float) 1.0);
            //editor.putFloat(PREF_MICGAIN, (float) 1.0);
            //editor.commit();

            //loginMess = new Dialog(this);
            //loginMess.setCancelable(false);
            String s = config.getString(PREF_URLCONFIG, "");
            if (!s.equals(DEFAULT_BASE)) {
                String urlConfig = "http://" + s + "/todahotline/";
                NetContext.getInstance().setBASE_URL(urlConfig);

            } else {
                String urlConfig = "http://" + s + "/";
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
                    try {
                        loginAct();
                    } catch (Exception e) {

                    }
                }
            });
            config = getSharedPreferences(PREF_URLCONFIG, Context.MODE_PRIVATE);
            configEditor = config.edit();
//        LinearLayoutThatDetectsSoftKeyboard mainLayout = (LinearLayoutThatDetectsSoftKeyboard)findViewById(R.id.login_container);
//        mainLayout.setListener(this);
            autoLogin = getSharedPreferences("AutoLogin", MODE_PRIVATE);
            android.util.Log.d(TAG, "ACCOUNTID: " + autoLogin.getBoolean("AutoLogin", false));
            android.util.Log.d(TAG, "onCreate: " + LoginActivity.ACCOUNT_ID);
            //tu dong dang nhap khi tat nong app
            if (LoginActivity.ACCOUNT_ID > 0 || autoLogin.getBoolean("AutoLogin", false)) {

                accountSelected = getSharedPreferences("AccountSelected", MODE_PRIVATE);
                ACCOUNT_ID = accountSelected.getLong("AccountSelected", 0);
                android.util.Log.d(TAG, "onCreate: " + ACCOUNT_ID);
                try {
                    loginAct();
                } catch (Exception e) {

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
                    String urlConfig = "http://" + et_config.getText() + "/todahotline/";
                    NetContext.getInstance().setBASE_URL(urlConfig);
                    configEditor.putString(PREF_URLCONFIG, et_config.getText().toString());
                    configEditor.commit();
                } else {
                    String urlConfig = "http://" + et_config.getText() + "/";
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
//                getBaseContext().getContentResolver().delete(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, 1), null, null);
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

                        }
                        Toast.makeText(getBaseContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                    } else if (et_password.getText().toString().equals("")
                            || et_idct.getText().toString().equals("")
                            || et_username.getText().toString().equals("")) {
                        try {
                            if (dialogLogin.isShowing())
                                dialogLogin.cancel();
                        } catch (Exception e) {

                        }
                        Toast.makeText(getBaseContext(), "Thông tin đăng nhập không được để trống", Toast.LENGTH_SHORT).show();
                    } else {
                        NetContext.getInstance().init(LoginActivity.this);
                        loginURL = KEY_FUNC_URL
                                + "token="
                                + "&idct=" + et_idct.getText().toString()
                                + "&taikhoan=" + et_username.getText().toString()
                                + "&matkhau=" + StringtoMD5(et_password.getText().toString())
                                + "&os=2"
                                + "&osversion=" + android.os.Build.VERSION.RELEASE
                                + "&dongmay=" + android.os.Build.DEVICE
                                + "&devicename=" + android.os.Build.DEVICE
                                + "&imei=" + Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
                                + "&ver=1.3.0.3"
                                + "&idpush=" ;
                        android.util.Log.d(TAG, "loginURL: " + loginURL);
                        final Service service = NetContext.instance.create(Service.class);
                        service.login(loginURL).enqueue(new Callback<LoginRespon>() {
                            @Override
                            public void onResponse(Call<LoginRespon> call, Response<LoginRespon> response) {
                                LoginRespon loginRespon = response.body();
                                android.util.Log.d(TAG, "onResponse:512 ");
                                if (dialogLogin.isShowing())
                                    dialogLogin.cancel();
                                try {
                                    if (loginRespon.getStatus()) {
                                        loginEditor.putString(KEY_USERID, userid);
                                        loginEditor.putString(KEY_SESSION, sessionkey);
                                        loginEditor.putString(KEY_DISPLAY_NAME, displayname);
                                        loginEditor.apply();
                                        autoLoginEditor = autoLogin.edit();
                                        autoLoginEditor.putBoolean("AutoLogin", false);
                                        autoLoginEditor.apply();

//                                        ChangePassFragment.currentPass = et_password.getText().toString();
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
                                                //xoa nhung account cu di
                                                if (mPrefs.getAccountCount()>0){
                                                    int accountNumber = mPrefs.getAccountCount();
                                                    while(accountNumber>=0){
                                                        mPrefs.deleteAccount(accountNumber);
                                                        accountNumber--;
                                                    }
                                                }

                                                saveCreatedAccount(user, user, pass, loginRespon.getData().getTennhanvien(), null, null, server, null);


//                                                account = SipProfile.getProfileFromDbId(LoginActivity.this, accountId, DBProvider.ACCOUNT_FULL_PROJECTION);
//                                                Intent intent2 = new Intent(LoginActivity.this, SipHome.class);
//                                                startActivity(intent2);
                                                //moi lan dang nhap la xoa luon cai tai khoan cu

                                            } catch (MissingClaimException e) {
                                                android.util.Log.d(TAG, "MissingClaimException: "+e);
                                                // we get here if the required claim is not present

                                            } catch (IncorrectClaimException e) {
                                                android.util.Log.d(TAG, "IncorrectClaimException: "+e);
                                                // we get here if the required claim has the wrong value

                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }


//                                new sendSttTask().execute();
                                        //Intent intent = new Intent(Activity_Login.this, SipHome.class);
                                        //startActivity(intent);
                                    } else {
//                                switch (response.code()) {
//                                    case 100:
//                                        messegeDialog(loginRespon.getMsg());
//                                        break;
//
//                                    case 101:
//                                        messegeDialog(loginRespon.getMsg());
//                                        break;
//
//                                    case 102:
//                                        messegeDialog(loginRespon.getMsg());
//                                        break;
//
//                                    case 104:
//                                        messegeDialog(loginRespon.getMsg());
//                                        break;

//                                    default:
                                        messegeDialog(loginRespon.getMsg());
//                                        break;
//                                }
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

//                    new loginTask().execute();

                        //android.util.Log.e("STT", sendSttURL);
                        //   Toast.makeText(getBaseContext(), ext, Toast.LENGTH_SHORT).show();
                    /*Basic.accDisplayName = "Toi";
					Basic.accServer = et_idct.getText().toString();
					Basic.accUserName = et_username.getText().toString();
					Basic.accPassword = et_password.getText().toString();
					saveAccount("BASIC");
					//startActivity(new Intent(getBaseContext(), SipHome.class));
					dialogLogin.cancel();
					//loginState(getBaseContext());
					Intent intent = new Intent(Activity_Login.this, SipHome.class);
                    startActivity(intent);
					finish();*/
                        //Toast.makeText(getBaseContext(), account.id + "", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getBaseContext(), loginState(getBaseContext(), account.id) + "", Toast.LENGTH_SHORT).show();
                    }
                    //	startActivity(new Intent(getBaseContext(), SipHome.class));
                    //finish();
                }
            }, 0);

        } else {
            showConfigDialog();
        }
    }

    private String getDeviceImei() {

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = mTelephonyManager.getDeviceId();

        return deviceid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d(TAG, "onDestroy: ");
        try {
            stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(getString(R.string.sync_account_type));
            android.os.Process.killProcess(android.os.Process.myPid());
            if (dialogLogin != null && dialogLogin.isShowing())
                dialogLogin.cancel();
        } catch (Exception e) {

        }
    }

    public void saveCreatedAccount(String username, String userid, String password, String displayname, String ha1, String prefix, String domain, LinphoneAddress.TransportType transport) {
//        while(!LinphoneService.isReady()){
//            android.util.Log.d(TAG, "saveCreatedAccount: "+LinphoneService.isReady());
//        }
        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            android.util.Log.d(TAG, "LinphoneCoreException: "+e);
        }
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
        }catch (Exception e){
            android.util.Log.d(TAG, "Exception: "+e);
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
        LinphoneLauncherActivity.isLinphoneActivity=true;
        startActivity(new Intent().setClass(this, LinphoneActivity.class));
        android.util.Log.d(TAG, "saveCreatedAccount: 650");
    }

    /*public class loginTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			//dialogLogin = ProgressDialog.show(Activity_login.this, "", "Đăng nhập...", true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {

			editor.putString(PREF_USERNAME, et_username.getText().toString());
			editor.putString(PREF_PASSWORD, et_password.getText().toString());
			editor.putString(PREF_SERVER, et_idct.getText().toString());
			editor.putString(PREF_DOMAIN, et_idct.getText().toString());
			editor.putString(PREF_DNS, et_idct.getText().toString());
			editor.commit();


			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (Receiver.engine(getBaseContext()).isRegistered())
				Receiver.engine(getBaseContext()).halt();
    		Receiver.engine(getBaseContext()).StartEngine();
    		try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//loginMess.dismiss();
    		dialogLogin.cancel();
			String ms = Receiver.engine(getBaseContext()).getErrorMess();
			if(ms.equals("ok")){
				Intent myIntent = new Intent(getBaseContext(),com.lachong.tongdai.ui.ActivitytabMain.class);
				startActivity(myIntent);
				finish();
			}
			else if(ms.contains("403")){
				loginError.setMessage("S�? máy lẻ hoặc mật khẩu không �?úng");
				Receiver.engine(getBaseContext()).halt();
				loginError.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putString(PREF_USERNAME, "");
						editor.putString(PREF_PASSWORD, "");
						editor.putString(PREF_SERVER, "");
						editor.putString(PREF_DOMAIN, "");
						editor.putString(PREF_DNS, "");
						editor.commit();
						finish();
						startActivity(getIntent());
					}
				});
				loginError.create();
				loginError.show();
			}
			else {
				Receiver.engine(getBaseContext()).halt();
				loginError.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putString(PREF_USERNAME, "");
						editor.putString(PREF_PASSWORD, "");
						editor.putString(PREF_SERVER, "");
						editor.putString(PREF_DOMAIN, "");
						editor.putString(PREF_DNS, "");
						editor.commit();
						finish();
						startActivity(getIntent());
					}
				});
				loginError.create();
				loginError.show();
			}
		}

	}*/

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
     * @param
     * account
     */




    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(Intent.ACTION_MAIN);
        intent2.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent2);
//        try {
//            stopService(SipHome.intentSSS);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }

//        SipHome.SipHome_Activity.finish();

        //int pid = android.os.Process.myPid();
        //android.os.Process.killProcess(pid);
        //finish();
    }

//    public class loginTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            try {
//                XMLParse parse = new XMLParse();
//                xmlLogin = parse.getXmlFromUrl(loginURL);
//                docLogin = parse.getDomElement(xmlLogin);
//
//                Element eLI = (Element) docLogin.getElementsByTagName(KEY_LOGIN).item(0);
//                loginCheck = parse.getElementValue(eLI);
//                if (loginCheck.equals("1")) {
//                    Element eEX = (Element) docLogin.getElementsByTagName(KEY_EXT).item(0);
//                    ext = parse.getElementValue(eEX);
//
//                    Element eEP = (Element) docLogin.getElementsByTagName(KEY_EXT_PWD).item(0);
//                    ext_pwd = parse.getElementValue(eEP);
//                    Element eDN = (Element) docLogin.getElementsByTagName(KEY_DISPLAY_NAME).item(0);
//                    displayname = parse.getElementValue(eDN);
//                    Element eUI = (Element) docLogin.getElementsByTagName(KEY_USERID).item(0);
//                    userid = parse.getElementValue(eUI);
//                    Element eSK = (Element) docLogin.getElementsByTagName(KEY_SESSION).item(0);
//                    sessionkey = parse.getElementValue(eSK);
//                } else if (loginCheck.equals("0")) {
//                    Element eEC = (Element) docLogin.getElementsByTagName(KEY_ERR_CODE).item(0);
//                    err_code = parse.getElementValue(eEC);
//                }
//            } catch (Exception e) {
//                Log.e("LOGIN", "Login Task", e);
//                loginCheck = "-1";
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            //Log.e("TEST LOGIN", loginCheck);
//            if (dialogLogin.isShowing())
//                dialogLogin.cancel();
//            if (loginCheck.equals("1")) {
//                loginEditor.putString(KEY_USERID, userid);
//                loginEditor.putString(KEY_SESSION, sessionkey);
//                loginEditor.putString(KEY_DISPLAY_NAME, displayname);
//
////                SipHome.severStr = et_idct.getText().toString();
//                loginEditor.commit();
//
//                Basic.accDisplayName = displayname;
//                Basic.accServer = et_idct.getText().toString();
//                Basic.accUserName = ext;
//                Basic.accPassword = ext_pwd;
//                saveAccount("BASIC");
//
//                new sendSttTask().execute();
//                //Intent intent = new Intent(Activity_Login.this, SipHome.class);
//                //startActivity(intent);
//            } else if (loginCheck.equals("0")) {
//                switch (err_code) {
//                    case "100":
//                        messegeDialog("Vui lng nh?p ??y ?? th�ng tin");
//                        break;
//
//                    case "101":
//                        messegeDialog("L?i x�c nh?n th�ng tin v?i m�y ch?");
//                        break;
//
//                    case "102":
//                        messegeDialog("T�n ??ng nh?p ho?c m?t kh?u kh�ng ch�nh x�c");
//                        break;
//
//                    case "104":
//                        messegeDialog("L?i d? li?u m�y ch?");
//                        break;
//
//                    default:
//                        messegeDialog("L?i kh�ng x�c ??nh: " + err_code);
//                        break;
//                }
//            } else {
//                messegeDialog("Kh�ng th? k?t n?i t?i m�y ch?");
//            }
//        }
//
//    }

//    public class sendSttTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            String serverStr = loginPreferences.getString("server", "");
//            String userid = loginPreferences.getString(LoginActivity.KEY_USERID, "");
//            String sessionkey = loginPreferences.getString(LoginActivity.KEY_SESSION, "");
//            String sign = StringtoMD5(userid + sessionkey + "1" + LoginActivity.KEY_SIGN);
//
//            sendSttURL = LoginActivity.KEY_PROTOCOL + serverStr + KEY_FUNC_URL
//                    + "username=" + userid
//                    + "&password=" + sessionkey
//                    + "&connectionstt=" + "1"
//                    + "&sign=" + sign;
//            //Log.e("URL", sendSttURL);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            try {
//                listAllUsers = new ArrayList<OtherUser>();
//                XMLParse parse = new XMLParse();
//                xmlStt = parse.getXmlFromUrl(sendSttURL);
//                //	Log.e("XML", xmlStt);
//                docStt = parse.getDomElement(xmlStt);
//
////                Element eLI = (Element) docStt.getElementsByTagName(SendSttService.KEY_RESULT).item(0);
////                sendSttCheck = parse.getElementValue(eLI);
////                if(sendSttCheck.equals("1")){
////                    Element eUO = (Element) docStt.getElementsByTagName(SendSttService.KEY_USER_ONLINE_STT).item(0);
////                    NodeList nlUser = eUO.getElementsByTagName(SendSttService.KEY_USER);
////                    for (int i = 0; i < nlUser.getLength(); i++) {
////                        OtherUser user = new OtherUser();
////                        Element element = (Element) nlUser.item(i);
////                        user.setExt(parse.getValue(element, SendSttService.KEY_EXT));
////
////                        user.setDispName(parse.getValue(element, SendSttService.KEY_DISP_NAME));
////                        user.setOnline(parse.getValue(element, SendSttService.KEY_ONLINE));
//
//
////                        if(!user.getDispName().equals(loginPreferences.getString
////                                (LoginActivity.KEY_DISPLAY_NAME, "")))
////                            listAllUsers.add(user);
////                        else{
////                            Log.e("EXT", user.getExt());
////                            loginEditor.putString(KEY_EXT, ext);
////                            loginEditor.commit();
////                        }
////
////                    }
////                    editor2.putString("SIPID", ext);
////                    editor2.commit();
//
////                }
////                else if(sendSttCheck.equals("0")){
//////                    Element eEC = (Element) docStt.getElementsByTagName(SendSttService.KEY_ERR_CODE).item(0);
//////                    err_code = parse.getElementValue(eEC);
////                }
//            } catch (Exception e) {
//                android.util.Log.e("LOGIN", "Login Task", e);
//                sendSttCheck = "-1";
//            }
//
//	/*
//			*/
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            ArrayList<HashMap<String, String>> alContact = new ArrayList<HashMap<String, String>>();
//            if (sendSttCheck.equals("1")) {
//                Log.e("CHECK SEND STT", "Thanh Cong");
//                for (OtherUser user : listAllUsers) {
//                    HashMap<String, String> itemContact = new HashMap<String, String>();
//                    itemContact.put(Contacts_Fragment.KEY_EXT, user.getExt());
//                    itemContact.put(Contacts_Fragment.KEY_DPN, user.getDispName());
//                    itemContact.put(Contacts_Fragment.KEY_STT, user.getOnline());
//                    alContact.add(itemContact);
//
//                }
////                SipHome.saveAContact = alContact;
//                //dataToAct("ListContact");
//            } else if (sendSttCheck.equals("0")) {
//                switch (err_code) {
//                    case "100":
//                        Log.e("SEND STT", "Thi?u tham s?");
//                        break;
//
//                    case "101":
//                        Log.e("SEND STT", "Ch? k� MD5 sai");
//                        break;
//
//                    case "102":
//                        Log.e("SEND STT", "C� ng??i ?ang ??ng nh?p t�i kho?n n�y");
//                        //dataToAct("OtherLogin");
//                        break;
//
//                    case "104":
//                        Log.e("SEND STT", "L?i database m�y ch?");
//                        break;
//
//                    default:
//                        Log.e("SEND STT", "L?i kh�ng x�c ?inh: " + err_code);
//                        break;
//                }
//            } else {
//                Log.e("SEND STT", "Kh�ng th? k?t n?i t?i m�y ch?");
//            }
//
//            finish();
//            UtilConfig.check_pause_activity = 0;
//        }
//
//    }

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



}
