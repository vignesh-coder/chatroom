package com.chatroom.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatroom.R;
import com.chatroom.model.Chat;
import com.chatroom.recycler.adapter.ChatsAdapter;
import com.chatroom.util.Helper;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_contacts, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Context context = getContext();
        if (user != null) {

            Query query = FirebaseDatabase.getInstance().getReference().child("Chats").child(user.getUid()).orderByChild("timestamp");
            FirebaseRecyclerOptions<Chat> options = new FirebaseRecyclerOptions.Builder<Chat>().setQuery(query, Chat.class).build();
            ChatsAdapter chatsAdapter = new ChatsAdapter(options, view, context, user.getUid());

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);

            RecyclerView chatsRecycler = view.findViewById(R.id.recycler);
            chatsRecycler.setLayoutManager(layoutManager);
            chatsRecycler.setHasFixedSize(true);
            chatsRecycler.setAdapter(chatsAdapter);

            chatsAdapter.startListening();

            if (context != null) {
                chatsRecycler.addItemDecoration(new DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL));
            }

        } else {
            Helper.userNotFound(context);
        }
        return view;
    }

}
