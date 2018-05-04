package org.linphone.ultils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by QuocVietDang1 on 5/4/2018.
 */

public class KeyBoardUltils {
    public static KeyBoardUltils instance = new KeyBoardUltils();
    private String TAG = "KeyBoardUltils";

    public static KeyBoardUltils getInstance() {
        return instance;
    }

    public void hideKeyBoard(Activity activity) {
        try {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }
}
