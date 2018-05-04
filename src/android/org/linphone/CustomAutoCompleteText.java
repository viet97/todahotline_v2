package org.linphone;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.View;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import org.linphone.database.DbContext;
import org.linphone.model.ExtModel;
import org.linphone.network.models.ContactResponse;

/**
 * Created by QuocVietDang1 on 4/28/2018.
 */

public class CustomAutoCompleteText extends TokenCompleteTextView<ContactResponse.DSDanhBa> {
    public CustomAutoCompleteText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(ContactResponse.DSDanhBa danhba) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.new_message_token, (ViewGroup) getParent(), false);
        view.setText(danhba.getTenlienhe());
        return view;
    }

    @Override
    protected ContactResponse.DSDanhBa defaultObject(String s) {
        ContactResponse.DSDanhBa defaultValue = new ContactResponse.DSDanhBa();
        defaultValue.setTenlienhe(s);
        // object khong hop le set id = -1
        defaultValue.setIddanhba(-1);
        return defaultValue;
    }


}
