package com.chatroom.fragment;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chatroom.R;
import com.chatroom.model.User;
import com.chatroom.recycler.adapter.ContactsAdapter;
import com.chatroom.util.PermissionChecker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ContactsFragment extends Fragment {

    private View mView;
    private RecyclerView mContactsRecycler;
    private TextView mPlaceholderText;
    private FirebaseUser mUser;
    private Context context;
    private Set<String> contacts = new HashSet<>();

    public ContactsFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chats_contacts, container, false);

        init();

        mContactsRecycler.setLayoutManager(new LinearLayoutManager(context));
        mContactsRecycler.setHasFixedSize(true);
        mContactsRecycler.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        mPlaceholderText.setText("No Contacts found");

        if (mUser != null) {
            loadContacts();
            final ArrayList<User> users = new ArrayList<>();

            final Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("name");
            query.keepSynced(true);
            final ChildEventListener eventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    User user = dataSnapshot.getValue(User.class);
                    if(user!=null && contacts.contains(user.getPhoneNumber()) && !user.getUid().equals(mUser.getUid())){
                        users.add(user);
                        mPlaceholderText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            query.addChildEventListener(eventListener);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ContactsAdapter adapter = new ContactsAdapter(users,context);
                    mContactsRecycler.setAdapter(adapter);
                    query.removeEventListener(eventListener);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        return mView;
    }

    private void init() {

        context = getContext();
        mContactsRecycler = mView.findViewById(R.id.recycler);
        mPlaceholderText = mView.findViewById(R.id.placeholder_text);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionChecker.REQUEST_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts();
                } else
                    Toast.makeText(context, "Cannot load contacts...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loadContacts() {
        if (context != null && PermissionChecker.checkContactsPermission(context)) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry());
                    phoneNumber = phoneNumber.replaceAll(" ", "");
                    if (!phoneNumber.contains("+91"))
                        phoneNumber = "+91" + phoneNumber;

                    contacts.add(phoneNumber);
                }
                cursor.close();
            }

        }
    }
}
