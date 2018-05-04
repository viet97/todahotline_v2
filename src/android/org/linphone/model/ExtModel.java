package org.linphone.model;

import java.io.Serializable;

/**
 * Created by QuocVietDang1 on 4/28/2018.
 */

public class ExtModel implements Serializable {
    private String name;
    private String phoneNumber;

    public ExtModel(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return name + "\n" + phoneNumber;
    }
}
