package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Quoc Viet Dang on 11/26/2017.
 */

public class QuyenReponse {
    @SerializedName("id")
    private int id;
    @SerializedName("idcauhinh")
    private int idcauhinh;
    @SerializedName("tencauhinh")
    private String tencauhinh;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdcauhinh() {
        return idcauhinh;
    }

    public void setIdcauhinh(int idcauhinh) {
        this.idcauhinh = idcauhinh;
    }

    public String getTencauhinh() {
        return tencauhinh;
    }

    public void setTencauhinh(String tencauhinh) {
        this.tencauhinh = tencauhinh;
    }

    @Override
    public String toString() {
        return "QuyenReponse{" +
                "id=" + id +
                ", idcauhinh=" + idcauhinh +
                ", tencauhinh='" + tencauhinh + '\'' +
                '}';
    }
}
