package org.linphone.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.linphone.R;
import org.linphone.myactivity.LoginActivity;
import org.linphone.network.NetContext;

/**
 * Created by QuocVietDang1 on 5/23/2018.
 */

public class MessageDialog {
    TextView title;
    TextView message;
    ImageView cancel;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    DialogAction dialogAction;
    public static final String TAG = "MessageDialog";

    public void setDialogAction(DialogAction dialogAction) {
        this.dialogAction = dialogAction;
    }

    public MessageDialog(Context context, LayoutInflater layoutInflater) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
            } else {
                builder = new AlertDialog.Builder(context);
            }

            View view = layoutInflater.inflate(R.layout.message_dialog, null);
            builder.setView(view);
            builder.setPositiveButton(context.getString(R.string.confirm_dialog), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialogAction.onPositive();

                }
            }).setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialogAction.onNegative();
                }
            });
            title = view.findViewById(R.id.title);
            message = view.findViewById(R.id.message);
            cancel = view.findViewById(R.id.img_close);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancel();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Exception:" + e.toString());
        }
    }

    public void setTitle(String title) {
        try {
            this.title.setText(title);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void show() {
        try {
            dialog = builder.show();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    public void cancel() {
        try {
            dialog.cancel();
        } catch (Exception e) {
            Log.d("MessageDialog", "cancel: " + e.toString());
        }
    }
}
