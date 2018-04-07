package org.linphone;

/**
 * Created by QuocVietDang1 on 4/5/2018.
 */

public class PhoneContact {
    private String name;
    private String number;

    public PhoneContact(String name, String number) {
        this.name = name;
        this.number = number;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
