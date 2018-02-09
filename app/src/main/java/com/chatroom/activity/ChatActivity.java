package com.chatroom.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chatroom.R;
import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.chatroom.recycler.adapter.MessagesAdapter;
import com.chatroom.util.Helper;
import com.chatroom.util.MyProgressDialog;
import com.chatroom.util.PermissionChecker;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int RC_CODE = 27;
    private final Context context = this;
    private RecyclerView mMessagesRecycler;
    private EditText mNewMessageET;
    private ImageButton mAttachFileBtn, mSendBtn;
    private DatabaseReference mDatabaseMessages,mDatabaseChats;
    private FirebaseUser mUser;
    private String mRecipientUID;
    private String mSenderUID;
    private Uri mAttachedFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        mRecipientUID = getIntent().getStringExtra("recipient_uid");

        if (mRecipientUID != null) {

            ImageButton backBtn = findViewById(R.id.back_btn);
            final CircleImageView profileImage = findViewById(R.id.user_profile_image);
            final TextView nameText = findViewById(R.id.name_text);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChatActivity.super.onBackPressed();
                }
            });
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mRecipientUID);
            dbRef.keepSynced(true);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User model = dataSnapshot.getValue(User.class);
                    if (model != null) {
                        Helper.setImage(context, model.getImage(), profileImage);
                        nameText.setText(model.getName());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                init();

                findViewById(R.id.profile_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,ViewProfileActivity.class);
                        intent.putExtra("uid",mRecipientUID);
                        startActivity(intent);
                    }
                });

                Query query = mDatabaseMessages.child(mSenderUID).child(mRecipientUID);
                query.keepSynced(true);

                FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class).build();
                MessagesAdapter adapter = new MessagesAdapter(context, options, mSenderUID, mRecipientUID) {
                    @Override
                    public void onDataChanged() {
                        mMessagesRecycler.scrollToPosition(getItemCount()-1);
                    }
                };
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setStackFromEnd(true);

                mMessagesRecycler.setLayoutManager(layoutManager);
                mMessagesRecycler.setHasFixedSize(true);
                mMessagesRecycler.setAdapter(adapter);
                adapter.startListening();

                mNewMessageET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        String text = charSequence.toString();
                        if (!Helper.isEmpty(text) || mAttachedFile != null) {
                            updateSendBtn(true);
                        } else {
                            updateSendBtn(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                mAttachFileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (PermissionChecker.checkExternalStoragePermission(context)) {
                            chooseFile();

                        }
                    }
                });
                mSendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String msg = mNewMessageET.getText().toString();
                        if (!Helper.isEmpty(msg) || mAttachedFile != null) {
                            sendMessage(msg);
                            mNewMessageET.setText("");
                            mAttachedFile = null;
                            mAttachFileBtn.setImageDrawable(getDrawable(R.drawable.ic_attach_file));
                        }
                    }
                });
            } else {
                Helper.userNotFound(context);
            }
        } else {
            finish();
        }
    }

    private void sendMessage(final String msg) {

        final String key = mDatabaseMessages.push().getKey();

        if (mAttachedFile != null) {
            final MyProgressDialog dialog = new MyProgressDialog(context, false);
            dialog.show("Uploading file...");
            FirebaseStorage.getInstance().getReference()
                    .child("Attachments").child(mSenderUID).child(key).child(Helper.getFileName(context, mAttachedFile)).putFile(mAttachedFile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            if (taskSnapshot.getDownloadUrl() != null)
                                saveMessage(key, msg, taskSnapshot.getDownloadUrl().toString());
                            else {
                                dialog.dismiss();
                                saveMessage(key, msg, "nil");
                                Toast.makeText(context, "Cannot attach the file...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveMessage(key, msg, "nil");
        }
    }

    private void saveMessage(String key, String msg, String image) {

        Message message = new Message(mSenderUID, mRecipientUID, msg, image, "sent", System.currentTimeMillis());

        DatabaseReference databaseSenderChats = mDatabaseChats.child(mSenderUID).child(mRecipientUID);
        DatabaseReference databaseRecipientChats = mDatabaseChats.child(mRecipientUID).child(mSenderUID);

        DatabaseReference databaseSenderMessages = mDatabaseMessages.child(mSenderUID).child(mRecipientUID);
        DatabaseReference databaseRecipientMessages = mDatabaseMessages.child(mRecipientUID).child(mSenderUID);

        databaseSenderChats.child("timestamp").setValue(message.getTimestamp());
        databaseRecipientChats.child("timestamp").setValue(message.getTimestamp());

        databaseSenderMessages.child(key).setValue(message);
        databaseRecipientMessages.child(key).setValue(message);

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

                        chooseFile();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot choose file", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Permission denied. Cannot choose file.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, RC_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RC_CODE) {
            mAttachedFile = data.getData();
            updateSendBtn(true);
            Toast.makeText(context, "File attached " + Helper.getFileName(context, mAttachedFile), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateSendBtn(boolean enable) {
        if (enable) {
            mSendBtn.setImageDrawable(getDrawable(R.drawable.ic_send));
            mSendBtn.setEnabled(true);
        } else {
            mSendBtn.setImageDrawable(getDrawable(R.drawable.ic_send_gray));
            mSendBtn.setEnabled(false);
        }
    }

    private void init() {

        mMessagesRecycler = findViewById(R.id.reyclerview_message_list);
        mNewMessageET = findViewById(R.id.new_msg_ET);
        mAttachFileBtn = findViewById(R.id.attach_file_btn);
        mSendBtn = findViewById(R.id.send_msg_btn);
        mSenderUID = mUser.getUid();
        mDatabaseChats = FirebaseDatabase.getInstance().getReference().child("Chats");
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("Messages");
    }
}
