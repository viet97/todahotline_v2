package org.linphone.dialog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.linphone.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigDialog extends Fragment {


    public ConfigDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config_dialog, container, false);
    }

}
