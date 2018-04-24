package org.linphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.telecom.Call;
import android.util.Log;

import com.google.gson.Gson;

import org.linphone.database.DbContext;
import org.linphone.network.models.DSCongTyResponse;
import org.linphone.ultils.DateUltils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by QuocVietDang1 on 3/22/2018.
 */
public class MyCallLogs {

    private ArrayList<CallLog> callLogs = new ArrayList<>();
    public static final int MAX_LOG = 100;

    public MyCallLogs() {
    }

    public ArrayList<CallLog> stackHistory() {
        ArrayList<CallLog> callLogs = new ArrayList<>();
        CallLog callLog = null;
        for (int i = 0; i < this.getCallLogs().size(); i++) {
            if (i == 0) {
                callLogs.add(this.getCallLogs().get(i));
            } else {
                CallLog currentCallLog = this.getCallLogs().get(i);
                int index = 0;
                for (CallLog c : callLogs) {
                    Calendar c1 = Calendar.getInstance();
                    c1.setTimeInMillis(c.getTime());
                    Calendar c2 = Calendar.getInstance();
                    c2.setTimeInMillis(currentCallLog.getTime());
                    if (c.getPhoneNumber().equals(currentCallLog.getPhoneNumber()) &&
                            c.getName().equals(currentCallLog.getName()) &&
                            DateUltils.instance.isSameDay(c1, c2)) {
                        c.setCount(c.getCount() + 1);
                        break;
                    } else {
                        index++;
                    }
                }
                if (index == callLogs.size()) {
                    callLogs.add(this.getCallLogs().get(i));
                }
            }
        }
        return callLogs;
    }

    public ArrayList<CallLog> getCallLogs() {
        return callLogs;
    }

    public void setCallLogs(ArrayList<CallLog> callLogs) {
        this.callLogs = callLogs;
    }

    public static class CallLog {
        public static final int CUOC_GOI_NHO = 1;
        public static final int CUOC_GOI_DI = 2;
        public static final int CUOC_GOI_DEN = 3;
        public static final int MAY_BAN = 4;
        public static final int OFFLINE = 5;
        private int id;
        String phoneNumber;
        String name;
        long time;
        int status;
        int duration;
        int count;
        private String TAG = "MyCallLogs";

        public CallLog(int id, String Name, String phoneNumber, long time, int duration, int status) {
            this.phoneNumber = phoneNumber;
            this.name = Name;
            this.time = time;
            this.status = status;
            this.duration = duration;
            this.id = id;
            this.count = 1;
        }

        public CallLog() {
            this.count = 1;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                CallLog callLog = (CallLog) obj;
                if (this.id == callLog.getId()) return true;
            } catch (Exception e) {
                Log.d(TAG, "Exception" + e.toString());
                return false;
            }

            return false;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "CallLog{" +
                    "id=" + id +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", name='" + name + '\'' +
                    ", time=" + time +
                    ", status=" + status +
                    ", duration=" + duration +
                    ", count=" + count +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MyCallLogs{" +
                "callLogs=" + callLogs +
                '}';
    }
}
