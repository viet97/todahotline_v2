package org.linphone.network.models;

import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 4/26/2018.
 */

public class MessagesListResponse {
    @SerializedName("status")
    boolean status;
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    ArrayList<Data> datas = new ArrayList<>();

    public class Data {
        @SerializedName("ID_TinNhan")
        int ID_TinNhan;
        @SerializedName("CoFile")
        boolean CoFile;
        @SerializedName("SoTinNhanChuaDoc")
        int SoTinNhanChuaDoc;
        @SerializedName("TieuDe")
        String TieuDe;
        @SerializedName("ID_TinNhan_NoiDung")
        int ID_TinNhan_NoiDung;
        @SerializedName("NoiDungCuoi")
        String NoiDungCuoi;
        @SerializedName("ThoiGianTinNhanCuoi")
        String ThoiGianTinNhanCuoi;

        @Override
        public boolean equals(Object obj) {
            return this.getID_TinNhan() == ((Data) obj).getID_TinNhan();
        }

        public int getSoTinNhanChuaDoc() {
            return SoTinNhanChuaDoc;
        }

        public boolean isCoFile() {
            return CoFile;
        }

        public void setCoFile(boolean coFile) {
            CoFile = coFile;
        }

        public void setSoTinNhanChuaDoc(int soTinNhanChuaDoc) {
            SoTinNhanChuaDoc = soTinNhanChuaDoc;
        }

        public class NguoiGui {
            @SerializedName("ID_NguoiGui")
            int ID_NguoiGui;
            @SerializedName("TenNguoiGui")
            String TenNguoiGui;
            @SerializedName("UserToda")
            String UserToda;
            @SerializedName("ChucVu")
            String ChucVu;

            public int getID_NguoiGui() {
                return ID_NguoiGui;
            }

            public void setID_NguoiGui(int ID_NguoiGui) {
                this.ID_NguoiGui = ID_NguoiGui;
            }

            public String getTenNguoiGui() {
                return TenNguoiGui;
            }

            public void setTenNguoiGui(String tenNguoiGui) {
                TenNguoiGui = tenNguoiGui;
            }

            public String getUserToda() {
                return UserToda;
            }

            public void setUserToda(String userToda) {
                UserToda = userToda;
            }

            public String getChucVu() {
                return ChucVu;
            }

            public void setChucVu(String chucVu) {
                ChucVu = chucVu;
            }
        }

        @SerializedName("NguoiGui")
        NguoiGui NguoiGui;
        @SerializedName("TrangThaiDoc")
        int TrangThaiDoc;

        public class DSNguoiNhan implements Serializable {
            @SerializedName("ID_NguoiNhan")
            int ID_NguoiNhan;
            @SerializedName("TenNguoiNhan")
            String TenNguoiNhan;
            @SerializedName("UserToda")
            String UserToda;
            @SerializedName("ChucVu")
            String ChucVu;

            public int getID_NguoiNhan() {
                return ID_NguoiNhan;
            }

            public void setID_NguoiNhan(int ID_NguoiNhan) {
                this.ID_NguoiNhan = ID_NguoiNhan;
            }

            public String getTenNguoiNhan() {
                return TenNguoiNhan;
            }

            public void setTenNguoiNhan(String tenNguoiNhan) {
                TenNguoiNhan = tenNguoiNhan;
            }

            public String getUserToda() {
                return UserToda;
            }

            public void setUserToda(String userToda) {
                UserToda = userToda;
            }

            public String getChucVu() {
                return ChucVu;
            }

            public void setChucVu(String chucVu) {
                ChucVu = chucVu;
            }
        }

        public int getID_TinNhan() {
            return ID_TinNhan;
        }

        public void setID_TinNhan(int ID_TinNhan) {
            this.ID_TinNhan = ID_TinNhan;
        }

        public String getTieuDe() {
            return TieuDe;
        }

        public void setTieuDe(String tieuDe) {
            TieuDe = tieuDe;
        }

        public int getID_TinNhan_NoiDung() {
            return ID_TinNhan_NoiDung;
        }

        public void setID_TinNhan_NoiDung(int ID_TinNhan_NoiDung) {
            this.ID_TinNhan_NoiDung = ID_TinNhan_NoiDung;
        }

        public String getNoiDungCuoi() {
            return NoiDungCuoi;
        }

        public void setNoiDungCuoi(String noiDungCuoi) {
            NoiDungCuoi = noiDungCuoi;
        }

        public String getThoiGianTinNhanCuoi() {
            return ThoiGianTinNhanCuoi;
        }

        public void setThoiGianTinNhanCuoi(String thoiGianTinNhanCuoi) {
            ThoiGianTinNhanCuoi = thoiGianTinNhanCuoi;
        }

        public Data.NguoiGui getNguoiGui() {
            return NguoiGui;
        }

        public void setNguoiGui(Data.NguoiGui nguoiGui) {
            NguoiGui = nguoiGui;
        }

        public int getTrangThaiDoc() {
            return TrangThaiDoc;
        }

        public void setTrangThaiDoc(int trangThaiDoc) {
            TrangThaiDoc = trangThaiDoc;
        }

        public ArrayList<DSNguoiNhan> getDsNguoiNhans() {
            return dsNguoiNhans;
        }

        public void setDsNguoiNhans(ArrayList<DSNguoiNhan> dsNguoiNhans) {
            this.dsNguoiNhans = dsNguoiNhans;
        }

        @SerializedName("DSNguoiNhan")
        ArrayList<DSNguoiNhan> dsNguoiNhans = new ArrayList<>();

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

    public ArrayList<Data> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<Data> datas) {
        this.datas = datas;
    }
}
