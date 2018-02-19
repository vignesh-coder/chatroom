package com.chatroom.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.chatroom.R;
import com.chatroom.fragment.ProfileFragment;

public class ViewProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        String uid = getIntent().getStringExtra("uid");
        if (uid != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.activity_view_profile, ProfileFragment.newInstance(uid));
            ft.commit();
        } else {
            Toast.makeText(this, "User for found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
