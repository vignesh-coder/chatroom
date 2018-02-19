package com.chatroom.recycler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatroom.R;
import com.chatroom.model.User;
import com.chatroom.recycler.viewholder.ContactViewHolder;

import java.util.List;


public class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private List<User> mUserList;
    private Context context;

    public ContactsAdapter(List<User> list, Context context) {
        this.mUserList = list;
        this.context = context;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_item, parent, false);
        return new ContactViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position) {

        final User model = mUserList.get(holder.getAdapterPosition());
        holder.setOnClickListeners(model.getUid());
        holder.setProfileImage(model.getImage());
        holder.setName(model.getName());
        holder.setBio(model.getBio());

    }


    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
