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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SuggestionDialer) {
            if (((SuggestionDialer) obj).getName().equals(this.getName()) && ((SuggestionDialer) obj).getExt().equals(this.getExt())) {
                return true;
            }
        }
        return false;
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
