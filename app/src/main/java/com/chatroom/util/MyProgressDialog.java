package com.chatroom.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.chatroom.R;


public class MyProgressDialog {

    private ProgressDialog dialog;

    public MyProgressDialog(final Context context, final boolean b) {

        dialog = new ProgressDialog(context, R.style.Theme_MyDialog) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                if (b) {
                    dismiss();
                    ((Activity) context).finish();
                }
            }
        };
    }

    public void show(String msg) {
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
