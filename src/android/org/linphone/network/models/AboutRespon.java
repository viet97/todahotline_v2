package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hanhi on 11/22/2017.
 */

public class AboutRespon {

    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private Data data;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("tenlienhe")
        private String tenlienhe;
        @SerializedName("diachi")
        private String diachi;
        @SerializedName("dienthoai1")
        private String dienthoai1;
        @SerializedName("dienthoai2")
        private String dienthoai2;
        @SerializedName("hotline")
        private String hotline;
        @SerializedName("cskh")
        private String cskh;
        @SerializedName("website")
        private String website;
        @SerializedName("email")
        private String email;

        public String getTenlienhe() {
            return tenlienhe;
        }

        public void setTenlienhe(String tenlienhe) {
            this.tenlienhe = tenlienhe;
        }

        public String getDiachi() {
            return diachi;
        }

        public void setDiachi(String diachi) {
            this.diachi = diachi;
        }

        public String getDienthoai1() {
            return dienthoai1;
        }

        public void setDienthoai1(String dienthoai1) {
            this.dienthoai1 = dienthoai1;
        }

        public String getDienthoai2() {
            return dienthoai2;
        }

        public void setDienthoai2(String dienthoai2) {
            this.dienthoai2 = dienthoai2;
        }

        public String getHotline() {
            return hotline;
        }

        public void setHotline(String hotline) {
            this.hotline = hotline;
        }

        public String getCskh() {
            return cskh;
        }

        public void setCskh(String cskh) {
            this.cskh = cskh;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

    }

    @Override
    public String toString() {
        return "AboutRespon{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
