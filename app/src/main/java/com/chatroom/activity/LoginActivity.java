package com.chatroom.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.chatroom.R;
import com.chatroom.model.User;
import com.chatroom.util.Helper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "Login";
    private final Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Login");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            // already signed in
            Helper.startActivity((Activity) context, MainActivity.class);

        } else {

            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Collections.singletonList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                    ))
                            .build(),
                    RC_SIGN_IN);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference mDatabaseUser;
                if (currentUser != null) {
                    mDatabaseUser = FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(currentUser.getUid());

                    mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            Log.d("LoginActivity-Log","listener called");
                            User model = dataSnapshot.getValue(User.class);
                            if (model == null || !dataSnapshot.exists()) {
                                Log.d("LoginActivity-Log","null");
                                model = new User();
                                model.setUid(currentUser.getUid());
                                model.setName(getString(R.string.default_name));
                                model.setBio(getString(R.string.default_status));
                                model.setImage("default");
                                model.setPhoneNumber(currentUser.getPhoneNumber());
                            }

                            final User model1 = model;
                            mDatabaseUser.setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent(LoginActivity.this, ProfileSettingActivity.class);
                                        intent.putExtra("model", model1);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, Helper.getMessage(task.getException(), "Something went wrong"), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    Helper.userNotFound(context);
                }


            } else {
                // Sign in failed
                String msg;
                if (response == null) {
                    // User pressed back button
                    msg = "Login canceled by User";
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    msg = "No Internet Connection";
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    msg = "Unknown Error";
                } else msg = "Unknown sign in response";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                Log.d(TAG, msg);
            }

        }
    }
}