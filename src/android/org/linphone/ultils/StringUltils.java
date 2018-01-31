package org.linphone.ultils;

import android.text.Spannable;
import android.text.style.URLSpan;

/**
 * Created by Quoc Viet Dang on 12/15/2017.
 */

public class StringUltils {
    public static StringUltils instance = new StringUltils();

    public static StringUltils getInstance() {
        return instance;
    }

    public static void setInstance(StringUltils instance) {
        StringUltils.instance = instance;
    }

    public void removeUnderlines(Spannable p_Text) {
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = p_Text.getSpanStart(span);
            int end = p_Text.getSpanEnd(span);
            p_Text.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            p_Text.setSpan(span, start, end, 0);
        }
    }
}
