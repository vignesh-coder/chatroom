package com.chatroom.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.chatroom.R;
import com.chatroom.task.BackgroundService;
import com.google.firebase.auth.FirebaseAuth;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(27);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {

                    Button button = findViewById(R.id.get_started);
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                            finish();
                        }
                    });
                } else {

                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }
}
