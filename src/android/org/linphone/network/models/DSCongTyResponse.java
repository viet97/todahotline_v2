package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 3/20/2018.
 */

public class DSCongTyResponse {
    @SerializedName("status")

    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("dscongty")
    private ArrayList<DsCongTy> dscongty= new ArrayList<>();

    public class DsCongTy{
        @SerializedName("macongty")
        private String macongty;
        @SerializedName("baseURL")
        private String baseURL;

        public String getMacongty() {
            return macongty;
        }

        public void setMacongty(String macongty) {
            this.macongty = macongty;
        }

        public String getBaseURL() {
            return baseURL;
        }

        public void setBaseURL(String baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        public String toString() {
            return "DsCongTy{" +
                    "macongty='" + macongty + '\'' +
                    ", baseURL='" + baseURL + '\'' +
                    '}';
        }
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<DsCongTy> getDscongty() {
        return dscongty;
    }

    public void setDscongty(ArrayList<DsCongTy> dscongty) {
        this.dscongty = dscongty;
    }

    @Override
    public String toString() {
        return "DSCongTyResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", dscongty=" + dscongty +
                '}';
    }
}
