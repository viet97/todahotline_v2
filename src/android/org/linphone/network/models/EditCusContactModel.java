package org.linphone.network.models;

/**
 * Created by QuocVietDang1 on 4/4/2018.
 */

public class EditCusContactModel {
    private String tenlienhe;
    private String sodienthoai;
    private int iddanhba;

    public EditCusContactModel(String tenlienhe, String sodienthoai, int iddanhba) {
        this.tenlienhe = tenlienhe;
        this.sodienthoai = sodienthoai;
        this.iddanhba = iddanhba;
    }

    public int getIddanhba() {
        return iddanhba;
    }

    public void setIddanhba(int iddanhba) {
        this.iddanhba = iddanhba;
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
}
