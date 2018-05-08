package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 4/26/2018.
 */

public class DetailMessageListResponse {
    @SerializedName("status")
    boolean status;
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    Data data;

    public class Data {
        @SerializedName("ID_TinNhan")
        int ID_TinNhan;
        @SerializedName("TieuDe")
        String TieuDe;
        @SerializedName("NgayTao")
        String NgayTao;
        @SerializedName("dstinnhan")
        ArrayList<TinNhan> dstinnhan = new ArrayList<>();

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

        public String getNgayTao() {
            return NgayTao;
        }

        public void setNgayTao(String ngayTao) {
            NgayTao = ngayTao;
        }

        public ArrayList<TinNhan> getDstinnhan() {
            return dstinnhan;
        }

        public void setDstinnhan(ArrayList<TinNhan> dstinnhan) {
            this.dstinnhan = dstinnhan;
        }
    }

    public class TinNhan {
        @SerializedName("ID_TinNhan_NoiDung")
        int ID_TinNhan_NoiDung;
        @SerializedName("NoiDung")
        String NoiDung;
        @SerializedName("ThoiGian")
        String ThoiGian;

        @SerializedName("TrangThaiDoc")
        int TrangThaiDoc;

        @Override
        public boolean equals(Object obj) {
            return (this.getID_TinNhan_NoiDung() == ((TinNhan) obj).getID_TinNhan_NoiDung());
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

        public class File {
            @SerializedName("ID_TinNhan_NoiDung_File")
            int ID_TinNhan_NoiDung_File;
            @SerializedName("PathFile")
            String PathFile;
            @SerializedName("ID_TinNhan_NoiDung")
            int ID_TinNhan_NoiDung;

            public int getID_TinNhan_NoiDung_File() {
                return ID_TinNhan_NoiDung_File;
            }

            public void setID_TinNhan_NoiDung_File(int ID_TinNhan_NoiDung_File) {
                this.ID_TinNhan_NoiDung_File = ID_TinNhan_NoiDung_File;
            }

            public String getPathFile() {
                return PathFile;
            }

            public void setPathFile(String pathFile) {
                PathFile = pathFile;
            }

            public int getID_TinNhan_NoiDung() {
                return ID_TinNhan_NoiDung;
            }

            public void setID_TinNhan_NoiDung(int ID_TinNhan_NoiDung) {
                this.ID_TinNhan_NoiDung = ID_TinNhan_NoiDung;
            }
        }

        @SerializedName("NguoiGui")
        NguoiGui NguoiGui;
        @SerializedName("DSFile")
        ArrayList<File> DSFile = new ArrayList<>();

        public int getID_TinNhan_NoiDung() {
            return ID_TinNhan_NoiDung;
        }

        public void setID_TinNhan_NoiDung(int ID_TinNhan_NoiDung) {
            this.ID_TinNhan_NoiDung = ID_TinNhan_NoiDung;
        }

        public String getNoiDung() {
            return NoiDung;
        }

        public void setNoiDung(String noiDung) {
            NoiDung = noiDung;
        }

        public String getThoiGian() {
            return ThoiGian;
        }

        public void setThoiGian(String thoiGian) {
            ThoiGian = thoiGian;
        }

        public int getTrangThaiDoc() {
            return TrangThaiDoc;
        }

        public void setTrangThaiDoc(int trangThaiDoc) {
            TrangThaiDoc = trangThaiDoc;
        }

        public TinNhan.NguoiGui getNguoiGui() {
            return NguoiGui;
        }

        public void setNguoiGui(TinNhan.NguoiGui nguoiGui) {
            NguoiGui = nguoiGui;
        }

        public ArrayList<File> getDSFile() {
            return DSFile;
        }

        public void setDSFile(ArrayList<File> DSFile) {
            this.DSFile = DSFile;
        }
    }


    public boolean isStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
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
}
