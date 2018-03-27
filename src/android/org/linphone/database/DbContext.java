package org.linphone.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.linphone.MyCallLogs;
import org.linphone.network.models.AboutRespon;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.DSCongTyResponse;
import org.linphone.network.models.LoginRespon;
import org.linphone.network.models.NonTodaContactsResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hanhi on 11/22/2017.
 */

public class DbContext {
    public static final String PREFS_CALL_lOG = "CallLog";
    private static final String Pref_String_DB = "DbContext";

    private static SharedPreferences callLogsPref, DBDsCongTyResponse, DBaboutRespon, DBloginResponse, DBcontactResponse, DBcuscontactResponse, DBlistContactTodaName, DBnonTodaContacts, DBsearchcontactResponse;
    private SharedPreferences.Editor callLogsPrefEditor, DBDsCongTyResponseEditor, DBaboutResponEditor, DBloginResponseEditor, DBcontactResponseEditor, DBcuscontactResponseEditor, DBlistContactTodaNameEditor, DBnonTodaContactsEditor, DBsearchcontactResponseEditor;
    private Context context;
    private static final DbContext instance = new DbContext();
    private AboutRespon aboutRespon;
    private LoginRespon loginRespon;
    private MyCallLogs myCallLogs;
    private NonTodaContactsResponse nonTodaContactsResponse;
    private DSCongTyResponse dsCongTyResponse;
    private ContactResponse contactResponse,cusContactResponse,searchContactResponse;
    private HashMap<String, String> listContactTodaName;
    private HashMap<String, String> listContactTodaJob;
    java.lang.reflect.Type hashmapType;
    private Gson gson = new Gson();
    private String TAG = "DbContext";

    public DbContext() {
        this.loginRespon = new LoginRespon();
        this.contactResponse = new ContactResponse();
        this.cusContactResponse = new ContactResponse();
        this.searchContactResponse = new ContactResponse();
        this.listContactTodaName = new HashMap<>();
        this.listContactTodaJob = new HashMap<>();
        this.aboutRespon = new AboutRespon();
        this.myCallLogs = new MyCallLogs();
        hashmapType = new TypeToken<HashMap<String, String>>() {
        }.getType();
    }

    public NonTodaContactsResponse getNonTodaContactsResponse(Context context) {
        try {
            if (context != null) {
                DBnonTodaContacts = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String nonTodaContactsResponStr = DBnonTodaContacts.getString("DBnonTodaContacts", null);
                if (nonTodaContactsResponStr != null) {
                    this.nonTodaContactsResponse = gson.fromJson(nonTodaContactsResponStr, NonTodaContactsResponse.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return nonTodaContactsResponse;
    }
    public DSCongTyResponse getDsCongTy(Context context) {
        try {
            if (context != null) {
                DBDsCongTyResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String DBDsCongTyResponseString = DBDsCongTyResponse.getString("DBDsCongTyResponse", null);
                if (DBDsCongTyResponseString != null) {
                    this.dsCongTyResponse = gson.fromJson(DBDsCongTyResponseString, DSCongTyResponse.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return dsCongTyResponse;
    }

    public void setMyCallLogs(MyCallLogs myCallLogs, Context context) {
        try {
            ArrayList<MyCallLogs.CallLog> callLogs = myCallLogs.getCallLogs();
            if (callLogs.size() > MyCallLogs.MAX_LOG) {
                callLogs.subList(MyCallLogs.MAX_LOG, callLogs.size()).clear();
            }
            if (context != null) {
                callLogsPref = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                callLogsPrefEditor = callLogsPref.edit();
                String callLogsStr = gson.toJson(myCallLogs);
                callLogsPrefEditor.putString(PREFS_CALL_lOG, callLogsStr);
                callLogsPrefEditor.commit();
                this.myCallLogs = myCallLogs;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    public MyCallLogs getMyCallLogs(Context context) {
        try {
            if (context != null) {
                callLogsPref = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String callLogsString = callLogsPref.getString(PREFS_CALL_lOG, null);
                if (callLogsString != null) {
                    this.myCallLogs = gson.fromJson(callLogsString, MyCallLogs.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return myCallLogs;
    }

    public void setDsCongty(DSCongTyResponse dsCongTyResponse, Context context) {
        try {
            if (context != null) {
                DBDsCongTyResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBDsCongTyResponseEditor = DBDsCongTyResponse.edit();
                String DBDsCongTyResponseStr = gson.toJson(dsCongTyResponse);

                DBDsCongTyResponseEditor.putString("DBDsCongTyResponse", DBDsCongTyResponseStr);
                DBDsCongTyResponseEditor.commit();
                Log.d("DBContext", "setDsCongty: "+DBDsCongTyResponse.getString("DBDsCongTyResponse",""));
                this.dsCongTyResponse = dsCongTyResponse;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }
    public void setSearchContactResponse(ContactResponse searchContactResponse, Context context) {
        try {
            if (context != null) {
                DBsearchcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBsearchcontactResponseEditor = DBcuscontactResponse.edit();
                String searchContactResponseStr = gson.toJson(searchContactResponse);
                DBsearchcontactResponseEditor.putString("DBsearchcontactResponse", searchContactResponseStr);
                DBsearchcontactResponseEditor.commit();
                this.contactResponse = contactResponse;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

    }
    public ContactResponse getSearchContactResponse(Context context) {
        try {
            if (context != null) {
                DBsearchcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String searchContactResponseStr = DBsearchcontactResponse.getString("DBsearchcontactResponse", null);
                if (searchContactResponseStr != null) {
                    this.searchContactResponse= gson.fromJson(searchContactResponseStr, ContactResponse.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return searchContactResponse;
    }

    public void setNonTodaContactsResponse(NonTodaContactsResponse nonTodaContactsResponse, Context context) {
        try {

            if (context != null) {
                DBnonTodaContacts = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBnonTodaContactsEditor = DBnonTodaContacts.edit();
                String nonTodaContactsResponStr = gson.toJson(nonTodaContactsResponse);
                DBnonTodaContactsEditor.putString("DBaboutRespon", nonTodaContactsResponStr);
                DBnonTodaContactsEditor.commit();
                this.nonTodaContactsResponse = nonTodaContactsResponse;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        this.nonTodaContactsResponse = nonTodaContactsResponse;
    }

    public HashMap<String, String> getListContactTodaName(Context context) {
        try {
            if (context != null) {
                DBlistContactTodaName = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String listcontactTodaname = DBlistContactTodaName.getString("DBlistContactTodaName", null);
                if (listcontactTodaname != null) {
                    this.listContactTodaName = gson.fromJson(listcontactTodaname, hashmapType);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return listContactTodaName;

    }

    public void setListContactTodaName(HashMap<String, String> listContactTodaName, Context context) {
        try {
            if (context != null) {
                DBlistContactTodaName = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBlistContactTodaNameEditor = DBlistContactTodaName.edit();
                String contactTodaName = gson.toJson(listContactTodaName);
                DBlistContactTodaNameEditor.putString("DBlistContactTodaName", contactTodaName);
                DBlistContactTodaNameEditor.commit();
                this.listContactTodaName = listContactTodaName;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    public HashMap<String, String> getListContactTodaJob() {
        return listContactTodaJob;
    }

    public void setListContactTodaJob(HashMap<String, String> listContactTodaJob) {
        try {
            this.listContactTodaJob = listContactTodaJob;
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

    }

    public AboutRespon getAboutRespon(Context context) {
        try {
            if (context != null) {
                DBaboutRespon = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String aboutResponStr = DBaboutRespon.getString("DBaboutRespon", null);
                if (aboutResponStr != null) {
                    this.aboutRespon = gson.fromJson(aboutResponStr, AboutRespon.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return aboutRespon;
    }

    public void setAboutRespon(AboutRespon aboutRespon, Context context) {
        try {

            if (context != null) {
                DBaboutRespon = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBaboutResponEditor = DBaboutRespon.edit();
                String aboutResponStr = gson.toJson(aboutRespon);
                DBaboutResponEditor.putString("DBaboutRespon", aboutResponStr);
                DBaboutResponEditor.commit();
                this.aboutRespon = aboutRespon;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }
    public static DbContext getInstance() {
        return instance;
    }

    public ContactResponse getContactResponse(Context context) {
        try {
            if (context != null) {
                DBcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String contactResponseStr = DBcontactResponse.getString("DBcontactResponse", null);
                if (contactResponseStr != null) {
                    this.contactResponse = gson.fromJson(contactResponseStr, ContactResponse.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return contactResponse;
    }

    public void setCusContactResponse(ContactResponse cusContactResponse, Context context) {
        try {
            if (context != null) {
                DBcuscontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBcuscontactResponseEditor = DBcuscontactResponse.edit();
                String cusContactResponseStr = gson.toJson(cusContactResponse);
                DBcuscontactResponseEditor.putString("DBcuscontactResponse", cusContactResponseStr);
                DBcuscontactResponseEditor.commit();
                this.contactResponse = contactResponse;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

    }
    public ContactResponse getCusContactResponse(Context context) {
        try {
            if (context != null) {
                DBcuscontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String cusContactResponseStr = DBcuscontactResponse.getString("DBcuscontactResponse", null);
                if (cusContactResponseStr != null) {
                    this.cusContactResponse= gson.fromJson(cusContactResponseStr, ContactResponse.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return cusContactResponse;
    }

    public void setContactResponse(ContactResponse contactResponse, Context context) {
        try {
            if (context != null) {
                Log.d("DBContext", "setContactResponse: " + context);
                DBcontactResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBcontactResponseEditor = DBcontactResponse.edit();
                String contactResponseStr = gson.toJson(contactResponse);
                DBcontactResponseEditor.putString("DBcontactResponse", contactResponseStr);
                DBcontactResponseEditor.commit();
                this.contactResponse = contactResponse;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }

    }

    public LoginRespon getLoginRespon(Context context) {
        try {
            if (context != null) {
                DBloginResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                String loginResponseStr = DBloginResponse.getString("DBloginResponse", null);
                if (loginResponseStr != null) {
                    this.loginRespon = gson.fromJson(loginResponseStr, LoginRespon.class);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return loginRespon;
    }

    public void setLoginRespon(LoginRespon loginRespon, Context context) {
        try {
            if (context != null) {
                DBloginResponse = context.getSharedPreferences(Pref_String_DB, Context.MODE_PRIVATE);
                DBloginResponseEditor = DBloginResponse.edit();
                String loginResponseStr = gson.toJson(loginRespon);
                DBloginResponseEditor.putString("DBloginResponse", loginResponseStr);
                DBloginResponseEditor.commit();
                this.loginRespon = loginRespon;
            }

        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }
}
