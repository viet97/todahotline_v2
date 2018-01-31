package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hanhi on 7/25/2017.
 */

public class LoginRespon {

    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private Data data;


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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("idct")
        private int idct;
        @SerializedName("idnhanvien")
        private int idnhanvien;
        @SerializedName("anhdaidien")
        private String anhdaidien;
        @SerializedName("tendangnhap")
        private String tendangnhap;
        @SerializedName("matkhau")
        private String matkhau;
        @SerializedName("tennhanvien")
        private String tennhanvien;
        @SerializedName("usertoda")
        private String usertoda;
        @SerializedName("tennhom")
        private String tennhom;
        @SerializedName("thoigiandangnhapcuoicung")
        private String thoigiandangnhapcuoicung;
        @SerializedName("dangtructuyen")
        private int dangtructuyen;
        @SerializedName("device")
        private String device;
        @SerializedName("os")
        private int os;
        @SerializedName("ver")
        private String ver;
        @SerializedName("idpush")
        private String idpush;
        @SerializedName("hinhthucdangxuat")
        private int hinhthucdangxuat;
        @SerializedName("idnhom")
        private int idnhom;
        @SerializedName("ThongBaoPhienBanMoi")
        private int thongbaophienbanmoi;
        @SerializedName("newversion")
        private String newversion;
        @SerializedName("msg_newversion")
        private String msgNewversion;
        @SerializedName("Min_Version")
        private String minVersion;
        @SerializedName("Max_Version")
        private String maxVersion;
        @SerializedName("devicename")
        private String devicename;
        @SerializedName("osversion")
        private String osversion;
        @SerializedName("dongmay")
        private String dongmay;
        @SerializedName("doimay")
        private String doimay;
        @SerializedName("imei")
        private String imei;
        @SerializedName("ChiCaiDatPhanMem1Lan")
        private int chicaidatphanmem1lan;
        @SerializedName("ngaycaidat")
        private String ngaycaidat;
        @SerializedName("dsquyen")
        private ArrayList<QuyenReponse> dsquyen = new ArrayList<>();
        @SerializedName("chucvu")
        private String chucvu;
        @SerializedName("somayle")
        private String somayle;
        @SerializedName("chophepxemonoffext")
        private String chophepxemonoffext;


        public ArrayList<QuyenReponse> getDsquyen() {
            return dsquyen;
        }

        public String getChophepxemonoffext() {
            return chophepxemonoffext;
        }

        public void setChophepxemonoffext(String chophepxemonoffext) {
            this.chophepxemonoffext = chophepxemonoffext;
        }

        public void setDsquyen(ArrayList<QuyenReponse> dsquyen) {
            this.dsquyen = dsquyen;
        }

        public String getChucvu() {
            return chucvu;
        }

        public void setChucvu(String chucvu) {
            this.chucvu = chucvu;
        }

        public String getSomayle() {
            return somayle;
        }

        public void setSomayle(String somayle) {
            this.somayle = somayle;
        }

        public int getIdct() {
            return idct;
        }

        public void setIdct(int idct) {
            this.idct = idct;
        }

        public int getIdnhanvien() {
            return idnhanvien;
        }

        public void setIdnhanvien(int idnhanvien) {
            this.idnhanvien = idnhanvien;
        }

        public String getAnhdaidien() {
            return anhdaidien;
        }

        public void setAnhdaidien(String anhdaidien) {
            this.anhdaidien = anhdaidien;
        }

        public String getTendangnhap() {
            return tendangnhap;
        }

        public void setTendangnhap(String tendangnhap) {
            this.tendangnhap = tendangnhap;
        }

        public String getMatkhau() {
            return matkhau;
        }

        public void setMatkhau(String matkhau) {
            this.matkhau = matkhau;
        }

        public String getTennhanvien() {
            return tennhanvien;
        }

        public void setTennhanvien(String tennhanvien) {
            this.tennhanvien = tennhanvien;
        }

        public String getUsertoda() {
            return usertoda;
        }

        public void setUsertoda(String usertoda) {
            this.usertoda = usertoda;
        }

        public String getTennhom() {
            return tennhom;
        }

        public void setTennhom(String tennhom) {
            this.tennhom = tennhom;
        }

        public String getThoigiandangnhapcuoicung() {
            return thoigiandangnhapcuoicung;
        }

        public void setThoigiandangnhapcuoicung(String thoigiandangnhapcuoicung) {
            this.thoigiandangnhapcuoicung = thoigiandangnhapcuoicung;
        }

        public int getDangtructuyen() {
            return dangtructuyen;
        }

        public void setDangtructuyen(int dangtructuyen) {
            this.dangtructuyen = dangtructuyen;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public int getOs() {
            return os;
        }

        public void setOs(int os) {
            this.os = os;
        }

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }

        public String getIdpush() {
            return idpush;
        }

        public void setIdpush(String idpush) {
            this.idpush = idpush;
        }

        public int getHinhthucdangxuat() {
            return hinhthucdangxuat;
        }

        public void setHinhthucdangxuat(int hinhthucdangxuat) {
            this.hinhthucdangxuat = hinhthucdangxuat;
        }

        public int getIdnhom() {
            return idnhom;
        }

        public void setIdnhom(int idnhom) {
            this.idnhom = idnhom;
        }

        public int getThongbaophienbanmoi() {
            return thongbaophienbanmoi;
        }

        public void setThongbaophienbanmoi(int thongbaophienbanmoi) {
            this.thongbaophienbanmoi = thongbaophienbanmoi;
        }

        public String getNewversion() {
            return newversion;
        }

        public void setNewversion(String newversion) {
            this.newversion = newversion;
        }

        public String getMsgNewversion() {
            return msgNewversion;
        }

        public void setMsgNewversion(String msgNewversion) {
            this.msgNewversion = msgNewversion;
        }

        public String getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(String minVersion) {
            this.minVersion = minVersion;
        }

        public String getMaxVersion() {
            return maxVersion;
        }

        public void setMaxVersion(String maxVersion) {
            this.maxVersion = maxVersion;
        }

        public String getDevicename() {
            return devicename;
        }

        public void setDevicename(String devicename) {
            this.devicename = devicename;
        }

        public String getOsversion() {
            return osversion;
        }

        public void setOsversion(String osversion) {
            this.osversion = osversion;
        }

        public String getDongmay() {
            return dongmay;
        }

        public void setDongmay(String dongmay) {
            this.dongmay = dongmay;
        }

        public String getDoimay() {
            return doimay;
        }

        public void setDoimay(String doimay) {
            this.doimay = doimay;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public int getChicaidatphanmem1lan() {
            return chicaidatphanmem1lan;
        }

        public void setChicaidatphanmem1lan(int chicaidatphanmem1lan) {
            this.chicaidatphanmem1lan = chicaidatphanmem1lan;
        }

        public String getNgaycaidat() {
            return ngaycaidat;
        }

        public void setNgaycaidat(String ngaycaidat) {
            this.ngaycaidat = ngaycaidat;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "idct=" + idct +
                    ", idnhanvien=" + idnhanvien +
                    ", anhdaidien='" + anhdaidien + '\'' +
                    ", tendangnhap='" + tendangnhap + '\'' +
                    ", matkhau='" + matkhau + '\'' +
                    ", tennhanvien='" + tennhanvien + '\'' +
                    ", usertoda='" + usertoda + '\'' +
                    ", tennhom='" + tennhom + '\'' +
                    ", thoigiandangnhapcuoicung='" + thoigiandangnhapcuoicung + '\'' +
                    ", dangtructuyen=" + dangtructuyen +
                    ", device='" + device + '\'' +
                    ", os=" + os +
                    ", ver='" + ver + '\'' +
                    ", idpush='" + idpush + '\'' +
                    ", hinhthucdangxuat=" + hinhthucdangxuat +
                    ", idnhom=" + idnhom +
                    ", thongbaophienbanmoi=" + thongbaophienbanmoi +
                    ", newversion='" + newversion + '\'' +
                    ", msgNewversion='" + msgNewversion + '\'' +
                    ", minVersion='" + minVersion + '\'' +
                    ", maxVersion='" + maxVersion + '\'' +
                    ", devicename='" + devicename + '\'' +
                    ", osversion='" + osversion + '\'' +
                    ", dongmay='" + dongmay + '\'' +
                    ", doimay='" + doimay + '\'' +
                    ", imei='" + imei + '\'' +
                    ", chicaidatphanmem1lan=" + chicaidatphanmem1lan +
                    ", ngaycaidat='" + ngaycaidat + '\'' +
                    ", dsquyen=" + dsquyen +
                    '}';
        }
    }


}
