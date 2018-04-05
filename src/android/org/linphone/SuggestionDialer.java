package org.linphone;

/**
 * Created by QuocVietDang1 on 4/5/2018.
 */

public class SuggestionDialer {
    private String name;
    private String ext;

    public SuggestionDialer(String name, String ext) {
        this.name = name;
        this.ext = ext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
