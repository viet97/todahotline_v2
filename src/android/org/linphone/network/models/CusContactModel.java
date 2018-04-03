package org.linphone.network.models;

/**
 * Created by QuocVietDang1 on 4/3/2018.
 */

public class CusContactModel {
    private String tenlienhe;
    private String sodienthoai;

    public CusContactModel(String tenlienhe, String sodienthoai) {
        this.tenlienhe = tenlienhe;
        this.sodienthoai = sodienthoai;
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
