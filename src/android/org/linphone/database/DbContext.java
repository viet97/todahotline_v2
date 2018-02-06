package org.linphone.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.linphone.network.models.AboutRespon;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.LoginRespon;

import java.util.HashMap;

/**
 * Created by hanhi on 11/22/2017.
 */

public class DbContext {
    private static final String Pref_String_DB = "DbContext";
    private static SharedPreferences DBaboutRespon, DBloginResponse, DBcontactResponse, DBlistContactTodaName;
    private SharedPreferences.Editor DBaboutResponEditor, DBloginResponseEditor, DBcontactResponseEditor, DBlistContactTodaNameEditor;
    private Context context;
    private static final DbContext instance = new DbContext();
    private AboutRespon aboutRespon;
    private LoginRespon loginRespon;
    private ContactResponse contactResponse;
    private HashMap<String, String> listContactTodaName;
    private HashMap<String, String> listContactTodaJob;
    java.lang.reflect.Type hashmapType;
    private Gson gson = new Gson();

    public DbContext() {
        this.loginRespon = new LoginRespon();
        this.contactResponse = new ContactResponse();
        this.listContactTodaName = new HashMap<>();
        this.listContactTodaJob = new HashMap<>();
        this.aboutRespon = new AboutRespon();
        hashmapType = new TypeToken<HashMap<String, String>>() {
        }.getType();
    }

    public HashMap<String, String> getListContactTodaName(Context context) {
        if (context!=null) {
            DBlistContactTodaName = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            String listcontactTodaname = DBlistContactTodaName.getString("DBlistContactTodaName", null);
            if (listcontactTodaname != null) {
                this.listContactTodaName = gson.fromJson(listcontactTodaname, hashmapType);
            }
        }
        return listContactTodaName;
    }

    public void setListContactTodaName(HashMap<String, String> listContactTodaName, Context context) {
        if (context!=null) {
            DBlistContactTodaName = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            DBlistContactTodaNameEditor = DBlistContactTodaName.edit();
            String contactTodaName = gson.toJson(listContactTodaName);
            DBlistContactTodaNameEditor.putString("DBlistContactTodaName", contactTodaName);
            DBlistContactTodaNameEditor.commit();
            this.listContactTodaName = listContactTodaName;
        }
    }

    public HashMap<String, String> getListContactTodaJob() {
        return listContactTodaJob;
    }

    public void setListContactTodaJob(HashMap<String, String> listContactTodaJob) {
        this.listContactTodaJob = listContactTodaJob;
    }

    public AboutRespon getAboutRespon(Context context) {
        if (context!=null) {
            DBaboutRespon = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            String aboutResponStr = DBaboutRespon.getString("DBaboutRespon", null);
            if (aboutResponStr != null) {
                this.aboutRespon = gson.fromJson(aboutResponStr, AboutRespon.class);
            }
        }
        return aboutRespon;
    }

    public void setAboutRespon(AboutRespon aboutRespon, Context context) {
        if (context!=null) {
            DBaboutRespon = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            DBaboutResponEditor = DBaboutRespon.edit();
            String aboutResponStr = gson.toJson(aboutRespon);
            DBaboutResponEditor.putString("DBaboutRespon", aboutResponStr);
            DBaboutResponEditor.commit();
            this.aboutRespon = aboutRespon;
        }
    }
    public static DbContext getInstance() {
        return instance;
    }

    public ContactResponse getContactResponse(Context context) {
        if (context!=null) {
            DBcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            String contactResponseStr = DBcontactResponse.getString("DBcontactResponse", null);
            if (contactResponseStr != null) {
                this.contactResponse = gson.fromJson(contactResponseStr, ContactResponse.class);
            }
        }
        return contactResponse;
    }

    public void setContactResponse(ContactResponse contactResponse, Context context) {
        if (context!=null) {
            Log.d("DBContext", "setContactResponse: " + context);
            DBcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            DBcontactResponseEditor = DBcontactResponse.edit();
            String contactResponseStr = gson.toJson(contactResponse);
            DBcontactResponseEditor.putString("DBcontactResponse", contactResponseStr);
            DBcontactResponseEditor.commit();
            this.contactResponse = contactResponse;
        }
    }

    public LoginRespon getLoginRespon(Context context) {
        if (context!=null) {
            DBloginResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            String loginResponseStr = DBloginResponse.getString("DBloginResponse", null);
            if (loginResponseStr != null) {
                this.loginRespon = gson.fromJson(loginResponseStr, LoginRespon.class);
            }
        }
        return loginRespon;
    }

    public void setLoginRespon(LoginRespon loginRespon, Context context) {
        if (context!=null) {
            DBloginResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
            DBloginResponseEditor = DBloginResponse.edit();
            String loginResponseStr = gson.toJson(loginRespon);
            DBloginResponseEditor.putString("DBloginResponse", loginResponseStr);
            DBloginResponseEditor.commit();
            this.loginRespon = loginRespon;
        }
    }
}
