package com.chatroom.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatroom.R;
import com.chatroom.activity.ProfileSettingActivity;
import com.chatroom.activity.SplashScreen;
import com.chatroom.model.User;
import com.chatroom.util.Helper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    private static final String UID = "param1";

    private String mUid;

    public static ProfileFragment newInstance(String uid) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(UID, uid);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUid = getArguments().getString(UID);
        }
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final ImageView profileImage = view.findViewById(R.id.user_profile_image);
        final TextView nameText = view.findViewById(R.id.name_text);
        final TextView numberText = view.findViewById(R.id.phone_number_text);
        final TextView bioText = view.findViewById(R.id.bio_text);
        final Button updateBtn = view.findViewById(R.id.update_profile_btn);
        final Button logoutBtn = view.findViewById(R.id.logout_btn);
        updateBtn.setVisibility(View.GONE);
        logoutBtn.setVisibility(View.GONE);

        final Context context = getContext();
        DatabaseReference databaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUid);
        databaseUser.keepSynced(true);
        databaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final User model = dataSnapshot.getValue(User.class);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Helper.userNotFound(context);
                } else if (model != null) {

                    Helper.setImage(context, model.getImage(), profileImage);
                    nameText.setText(model.getName());
                    numberText.setText(model.getPhoneNumber());
                    bioText.setText(model.getBio());


                    if (user.getUid().equals(model.getUid())) {
                        updateBtn.setVisibility(View.VISIBLE);
                        logoutBtn.setVisibility(View.VISIBLE);
                    }
                    updateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(context, ProfileSettingActivity.class);
                            intent.putExtra("model", model);
                            startActivity(intent);
                        }
                    });
                    logoutBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (context == null) return;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Log out");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseAuth.getInstance().signOut();
                                    Helper.startActivity((Activity) context, SplashScreen.class);
                                }
                            });
                            builder.setNegativeButton("No", null);
                            builder.show();
                        }
                    });
                } else {
                    Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                    if (context != null) {
                        ((Activity) context).finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }

}
