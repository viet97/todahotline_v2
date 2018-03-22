package org.linphone.ultils;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by Quoc Viet Dang on 12/15/2017.
 */

public class URLSpanNoUnderline extends URLSpan {


    public URLSpanNoUnderline(String url) {
        super(url);
    }

    public void updateDrawState(TextPaint p_DrawState) {
        super.updateDrawState(p_DrawState);
        p_DrawState.setUnderlineText(false);
    }

}
