package com.chatroom.recycler.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chatroom.R;
import com.chatroom.activity.ChatActivity;
import com.chatroom.activity.ViewProfileActivity;
import com.chatroom.util.Helper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private CircleImageView profileImage;
    private int count = 0;
    private Context context;
    private Set<String> unreadMsg;

    public ChatViewHolder(View itemView, final Context context) {
        super(itemView);
        mView = itemView;
        this.context = context;
        profileImage = mView.findViewById(R.id.user_profile_image);
        unreadMsg = new HashSet<>();
    }

    public void setOnClickListeners(final String uid) {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unreadMsg = new HashSet<>();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("recipient_uid", uid);
                context.startActivity(intent);
            }
        });

    }

    public void setOnLongClickListener(final DatabaseReference messagesRef, final DatabaseReference chatsRef) {
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new CharSequence[]{"Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setNegativeButton("No", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        messagesRef.removeValue();
                                        chatsRef.removeValue();
                                    }
                                });
                        builder1.show();
                    }
                }).show();
                return true;
            }
        });

    }

    public void setProfileImage(String url) {

        Helper.setImage(context, url, profileImage);
    }

    public void setName(String name) {
        TextView text = mView.findViewById(R.id.name_text);
        text.setText(name);
    }

    public void setMessage(String msg, boolean b, String status) {
        TextView text = mView.findViewById(R.id.content_text);

        if (b) {
            int drawable = 0;
            switch (status) {
                case "sent":
                    drawable = R.drawable.tick;
                    break;
                case "delivered":
                    drawable = R.drawable.double_tick_black;
                    break;
                case "seen":
                    drawable = R.drawable.double_tick_read;
                    break;
            }
            text.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
            msg = " " + msg;
        } else
            text.setCompoundDrawables(null, null, null, null);
        text.setText(msg);
    }

    public void setTime(long timestamp) {

        TextView text = mView.findViewById(R.id.timestamp_text);
        text.setText(Helper.getTimeAgo(timestamp));
    }

    public void addUnreadMsg(String msgId) {

        unreadMsg.add(msgId);
        updateBadge();
    }

    @SuppressLint("SetTextI18n")
    private void updateBadge() {
        int count = unreadMsg.size();
        TextView timeText = mView.findViewById(R.id.timestamp_text);
        TextView badge = mView.findViewById(R.id.msg_info);
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(count + "");
            if (count > 10)
                badge.setText("10+");
            timeText.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            badge.setVisibility(View.INVISIBLE);
            timeText.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    public void removeUnreadMsg(String msgId) {

        unreadMsg.remove(msgId);
        updateBadge();
    }
}
