package org.linphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.linphone.network.models.DSCongTyResponse;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 3/22/2018.
 */
public class MyCallLogs {
    private ArrayList<CallLog> callLogs = new ArrayList<>();

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
        String phoneNumber;
        long time;
        int status;
        int duration;

        public CallLog(String phoneNumber, long time, int status, int duration) {
            this.phoneNumber = phoneNumber;
            this.time = time;
            this.status = status;
            this.duration = duration;
        }

        public CallLog() {
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
