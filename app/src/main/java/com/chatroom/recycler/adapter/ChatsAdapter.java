package com.chatroom.recycler.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatroom.R;
import com.chatroom.model.Chat;
import com.chatroom.model.Message;
import com.chatroom.model.User;
import com.chatroom.recycler.viewholder.ChatViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChatsAdapter extends FirebaseRecyclerAdapter<Chat, ChatViewHolder> {


    private Context context;
    private TextView placeholderText;
    private String currentUserId;

    @SuppressLint("SetTextI18n")
    public ChatsAdapter(FirebaseRecyclerOptions<Chat> options, View view, Context context, String currentUserId) {
        super(options);
        this.context = context;
        placeholderText = view.findViewById(R.id.placeholder_text);
        placeholderText.setText("No recent chats");
        this.currentUserId = currentUserId;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_item, parent, false);
        return new ChatViewHolder(view, context);
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() <= 0) {
            placeholderText.setVisibility(View.VISIBLE);
        } else {
            placeholderText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final Chat model) {
        final String uid = getRef(holder.getAdapterPosition()).getKey();
        final DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages")
                .child(currentUserId)
                .child(uid);
        final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(currentUserId)
                .child(uid);
        messagesRef.keepSynced(true);
        chatsRef.keepSynced(true);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userRef.keepSynced(true);

        holder.setOnLongClickListener(messagesRef, chatsRef);
        holder.setOnClickListeners(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    holder.setProfileImage(user.getImage());
                    holder.setName(user.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Query recentMessageRef = messagesRef.limitToLast(1);
        recentMessageRef.keepSynced(true);

        holder.setTime(model.getTimestamp());
        recentMessageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    String text = message.getText();
                    boolean b = message.getSenderUID().equals(currentUserId);
                    if (text == null) {
                        text = b ? "You sent a file" : "Sent you a file";
                    }
                    holder.setMessage(text, b, message.getStatus());
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
        });

        Query currentUserMessages = messagesRef.orderByChild("recipientUID").equalTo(currentUserId);

        currentUserMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && !message.getStatus().equals("seen")) {
                    holder.addUnreadMsg(dataSnapshot.getRef().getKey());
                    Log.d("ChatsAdapter", message.getText());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && message.getStatus().equals("seen")) {
                    holder.removeUnreadMsg(dataSnapshot.getRef().getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    holder.removeUnreadMsg(dataSnapshot.getRef().getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
