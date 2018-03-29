package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 3/7/2018.
 */

public class NonTodaContactsResponse {
    @SerializedName("status")
    private boolean status;
    @SerializedName("endlist")
    private boolean endlist;
    @SerializedName("msg")
    private String msg;
    @SerializedName("lastid")
    private int lastid;

    public class DSDanhBaNonToda {
        @SerializedName("idrow")
        private int idrow;
        @SerializedName("idqllh")
        private int idqllh;
        @SerializedName("idnhanvien")
        private int idnhanvien;
        @SerializedName("tennhanvien")
        private String tennhanvien;
        @SerializedName("sodienthoai")
        private String sodienthoai;
        @SerializedName("chucvu")
        private String chucvu;
        @SerializedName("isChoose")
        private boolean isChoose;

        public boolean isChoose() {
            return isChoose;
        }

        public void setChoose(boolean choose) {
            isChoose = choose;
        }

        public int getIdrow() {
            return idrow;
        }

        public void setIdrow(int idrow) {
            this.idrow = idrow;
        }

        public int getIdqllh() {
            return idqllh;
        }

        public void setIdqllh(int idqllh) {
            this.idqllh = idqllh;
        }

        public int getIdnhanvien() {
            return idnhanvien;
        }

        public void setIdnhanvien(int idnhanvien) {
            this.idnhanvien = idnhanvien;
        }

        public String getTennhanvien() {
            return tennhanvien;
        }

        public void setTennhanvien(String tennhanvien) {
            this.tennhanvien = tennhanvien;
        }

        public String getSodienthoai() {
            return sodienthoai;
        }

        public void setSodienthoai(String sodienthoai) {
            this.sodienthoai = sodienthoai;
        }

        public String getChucvu() {
            return chucvu;
        }

        public void setChucvu(String chucvu) {
            this.chucvu = chucvu;
        }

        @Override
        public String toString() {
            return "{" +
                    "idrow=" + idrow +
                    ", idqllh=" + idqllh +
                    ", idnhanvien=" + idnhanvien +
                    ", tennhanvien='" + tennhanvien + '\'' +
                    ", sodienthoai='" + sodienthoai + '\'' +
                    ", chucvu='" + chucvu + '\'' +
                    ", isChoose=" + isChoose +
                    '}';
        }
    }

    @SerializedName("dsdanhba")
    private ArrayList<DSDanhBaNonToda> dsdanhba;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isEndlist() {
        return endlist;
    }

    public void setEndlist(boolean endlist) {
        this.endlist = endlist;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getLastid() {
        return lastid;
    }

    public void setLastid(int lastid) {
        this.lastid = lastid;
    }

    public ArrayList<DSDanhBaNonToda> getDsdanhba() {
        return dsdanhba;
    }

    public void setDsdanhba(ArrayList<DSDanhBaNonToda> dsdanhba) {
        this.dsdanhba = dsdanhba;
    }

}
