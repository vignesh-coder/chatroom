package com.chatroom.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chatroom.R;
import com.chatroom.model.User;
import com.chatroom.util.Helper;
import com.chatroom.util.MyProgressDialog;
import com.chatroom.util.PermissionChecker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingActivity extends AppCompatActivity {

    private final Context context = this;
    private CircleImageView mProfilePic;
    private EditText nameET, statusET;
    private Button changeBtn, updateBtn;
    private Uri toUpload;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        init();
        final User model = (User) getIntent().getSerializableExtra("model");
        if (model != null) {

            nameET.setText(model.getName());
            statusET.setText(model.getBio());
            if (!model.getImage().equals("default")) {

                RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.profile);
                Glide
                        .with(context)
                        .setDefaultRequestOptions(requestOptions)
                        .load(model.getImage())
                        .into(mProfilePic);
            }
        } else {
            Toast.makeText(context, "Cannot load data.", Toast.LENGTH_SHORT).show();
            finish();
        }
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionChecker.checkExternalStoragePermission(context)) {
                    CropImage.startPickImageActivity((Activity) context);
                }
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameET.getText().toString().trim();
                String status = statusET.getText().toString().trim();
                if (name.isEmpty() || status.isEmpty())
                    Toast.makeText(ProfileSettingActivity.this, "Fields cannot be left blank.", Toast.LENGTH_LONG).show();
                else {
                    if (model != null) {
                        model.setName(name);
                        model.setBio(status);
                        if (mUser != null) {
                            if (toUpload != null) {

                                StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("Profile");

                                final MyProgressDialog dialog = new MyProgressDialog(context, false);
                                dialog.show("Uploading image...");
                                profileStorage.child(mUser.getUid()).putFile(toUpload)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                dialog.dismiss();
                                                Uri url = taskSnapshot.getDownloadUrl();
                                                if (url != null) {
                                                    model.setImage(url.toString());
                                                    saveData(model);
                                                } else {
                                                    Toast.makeText(context, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        dialog.dismiss();
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else
                                saveData(model);
                        } else {
                            Helper.userNotFound(context);
                        }
                    }

                }
            }
        });
    }

    private void saveData(User model) {

        final MyProgressDialog dialog = new MyProgressDialog(context, false);
        dialog.show("updating...");
        DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.child(mUser.getUid()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    Helper.startActivity((Activity) context, MainActivity.class);
                } else {
                    Toast.makeText(context, Helper.getMessage(task.getException(), "Cannot update your profile."), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {

        mProfilePic = findViewById(R.id.circleImageView);
        nameET = findViewById(R.id.name_et);
        statusET = findViewById(R.id.status_et);
        changeBtn = findViewById(R.id.change_pic);
        updateBtn = findViewById(R.id.update);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.REQUEST_CODE:
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        if (toUpload == null)
                            CropImage.startPickImageActivity((Activity) context);
                        else
                            startCropImageActivity(toUpload);
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot choose image", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Cannot choose image.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri imageUri = CropImage.getPickImageResultUri(context, data);

            if (CropImage.isReadExternalStoragePermissionsRequired(context, imageUri)) {

                toUpload = imageUri;
                PermissionChecker.checkExternalStoragePermission(context);
            } else {

                startCropImageActivity(imageUri);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                toUpload = result.getUri();
                mProfilePic.setImageURI(toUpload);
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowRotation(true)
                .setAspectRatio(1, 1)
                .start((Activity) context);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
