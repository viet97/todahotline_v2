package org.linphone.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hanhi on 5/26/2017.
 */

public class VoidRespon {

    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;

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

    @Override
    public String toString() {
        return "VoidRespon{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
