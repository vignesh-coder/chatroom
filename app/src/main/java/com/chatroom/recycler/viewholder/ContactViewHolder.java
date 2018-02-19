package com.chatroom.recycler.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chatroom.R;
import com.chatroom.activity.ChatActivity;
import com.chatroom.activity.ViewProfileActivity;
import com.chatroom.util.Helper;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private CircleImageView profileImage;
    private Context context;

    public ContactViewHolder(View itemView, Context context) {
        super(itemView);
        mView = itemView;
        this.context = context;
        profileImage = mView.findViewById(R.id.user_profile_image);
        mView.findViewById(R.id.layout2).setVisibility(View.GONE);
    }

    public void setOnClickListeners(final String uid) {
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("recipient_uid", uid);
                context.startActivity(intent);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
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

    public void setBio(String bio) {
        TextView text = mView.findViewById(R.id.content_text);
        text.setText(bio);
    }
}
