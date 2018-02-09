package com.chatroom.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chatroom.R;
import com.chatroom.activity.LoginActivity;
import com.chatroom.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class Helper {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static boolean isEmpty(String... arr) {

        for (String s : arr) {
            if (TextUtils.isEmpty(s)) return true;
        }
        return false;
    }

    public static String getTimeAgo(long timestamp) {


        long now = Calendar.getInstance().getTimeInMillis();
        if (timestamp > now || timestamp <= 0) {
            return "";
        }
        long diff = now - timestamp;
        if (diff < MINUTE_MILLIS) {
            return "now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "1d";
        } else {
            long days = diff / DAY_MILLIS;
            if (days < 7)
                return diff / DAY_MILLIS + "d";
            else
                return days / 7 + "w";
        }

    }

    public static void startActivity(Activity activity, Class<?> c) {
        Intent intent = new Intent(activity, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static String getMessage(Exception e, String msg) {

        if (e != null)
            msg = e.getMessage();
        return msg;
    }

    public static void userNotFound(Context context) {

        Toast.makeText(context, "Please login to continue", Toast.LENGTH_SHORT).show();
        startActivity((Activity) context, LoginActivity.class);
    }

    public static void setImage(Context context, String url, ImageView imageView) {


        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.profile);
        Glide
                .with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(url)
                .into(imageView);
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void copyText(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData
                .newPlainText("Chatroom message", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        }

    }

    public static void setMessageStatus(String key, final Message model) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        final DatabaseReference currentUserMessages = messagesRef.child(model.getSenderUID())
                .child(model.getRecipientUID());

        final DatabaseReference senderMessages = messagesRef.child(model.getRecipientUID())
                .child(model.getSenderUID());


        currentUserMessages.child(key).setValue(model);
        final DatabaseReference currentMsgRef = senderMessages.child(key);
        currentMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentMsgRef.setValue(model);
                    currentMsgRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void deleteMedia(Context context, String file) {

        if (!file.equals("nil")) {
            final MyProgressDialog dialog = new MyProgressDialog(context, false);
            dialog.show("deleting media...");
            FirebaseStorage.getInstance().getReferenceFromUrl(file).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dialog.dismiss();
                }
            });
        }
    }
}
