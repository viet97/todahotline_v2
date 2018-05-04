package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Quoc Viet Dang on 11/23/2017.
 */

public class ContactResponse {
    @Override
    public String toString() {
        return "ContactResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", dsdanhba=" + dsdanhba +
                '}';
    }


    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
     @SerializedName("lastid")
    private int lastid;
    @SerializedName("dsdanhba")
    private ArrayList<DSDanhBa> dsdanhba;
    @SerializedName("endlist")
    private boolean endlist;

    public ContactResponse() {
        this.dsdanhba = new ArrayList<>();
    }

    public boolean isStatus() {
        return status;
    }

    public int getLastid() {
        return lastid;
    }

    public void setLastid(int lastid) {
        this.lastid = lastid;
    }

    public boolean isEndlist() {
        return endlist;
    }

    public void setEndlist(boolean endlist) {
        this.endlist = endlist;
    }

    public boolean getStatus() {
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

    public ArrayList<DSDanhBa> getDsdanhba() {
        return dsdanhba;
    }

    public void setDsdanhba(ArrayList<DSDanhBa> dsdanhba) {
        this.dsdanhba = dsdanhba;
    }

    public void addDsdsba(ArrayList<DSDanhBa> dsdanhba) {
        for (DSDanhBa ds : dsdanhba) {
            this.dsdanhba.add(ds);
        }

    }

    public static class DSDanhBa implements Serializable {
        public int getIddanhba() {
            return iddanhba;
        }

        public void setIddanhba(int iddanhba) {
            this.iddanhba = iddanhba;
        }

        @SerializedName("idrow")
        private int idrow;
        @SerializedName("iddanhba")
        private int iddanhba;
        @SerializedName("idqllh")
        private String idqllh;
        @SerializedName("idnhanvien")
        private String idnhanvien;
        @SerializedName("tenlienhe")
        private String tenlienhe;
        @SerializedName("sodienthoai")
        private String sodienthoai;
        @SerializedName("chucvu")
        private String job;
        @SerializedName("mamau")
        private String mamau;
        @SerializedName("status")
        private boolean status;

        public int getIdrow() {
            return idrow;
        }

        public void setIdrow(int idrow) {
            this.idrow = idrow;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getJob() {
            return job;
        }

        public String getMamau() {
            return mamau;
        }

        public void setMamau(String mamau) {
            this.mamau = mamau;
        }

        public void setJob(String job) {
            this.job = job;
        }



        public String getIdqllh() {
            return idqllh;
        }

        public void setIdqllh(String idqllh) {
            this.idqllh = idqllh;
        }

        public String getIdnhanvien() {
            return idnhanvien;
        }

        public void setIdnhanvien(String idnhanvien) {
            this.idnhanvien = idnhanvien;
        }

        public String getTenlienhe() {
            return tenlienhe;
        }

        public void setTenlienhe(String tenlienhe) {
            this.tenlienhe = tenlienhe;
        }

        public String getSodienthoai() {
            return sodienthoai;
        }

        public void setSodienthoai(String sodienthoai) {
            this.sodienthoai = sodienthoai;
        }

        @Override
        public boolean equals(Object obj) {
            return (this.tenlienhe.equals(((DSDanhBa) obj).getTenlienhe()) ? true : false);
        }

        @Override
        public String toString() {
            return "DSDanhBa{" +
                    "iddanhba='" + iddanhba + '\'' +
                    ", idqllh='" + idqllh + '\'' +
                    ", idnhanvien='" + idnhanvien + '\'' +
                    ", tenlienhe='" + tenlienhe + '\'' +
                    ", sodienthoai='" + sodienthoai + '\'' +
                    '}';
        }
    }
}
