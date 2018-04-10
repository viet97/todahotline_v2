package org.linphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.linphone.database.DbContext;
import org.linphone.network.models.DSCongTyResponse;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 3/22/2018.
 */
public class MyCallLogs {

    private ArrayList<CallLog> callLogs = new ArrayList<>();
    public static final int MAX_LOG = 100;
    public MyCallLogs() {
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
        private String TAG = "MyCallLogs";

        public CallLog(int id, String Name, String phoneNumber, long time, int duration, int status) {
            this.phoneNumber = phoneNumber;
            this.name = Name;
            this.time = time;
            this.status = status;
            this.duration = duration;
            this.id = id;
        }

        public CallLog() {
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

        @Override
        public String toString() {
            return "CallLog{" +
                    "phoneNumber='" + phoneNumber + '\'' +
                    ", time=" + time +
                    ", status=" + status +
                    ", duration=" + duration +
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
