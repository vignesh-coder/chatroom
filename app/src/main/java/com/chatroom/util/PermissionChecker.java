package com.chatroom.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;


public class PermissionChecker {
    public static final int REQUEST_CODE = 27;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkExternalStoragePermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {

            ArrayList<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionList.isEmpty()) {
                ActivityCompat.requestPermissions((Activity) context, permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean checkContactsPermission(Context context) {

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
                return false;
            }
            return true;
        } else return true;
    }
}



