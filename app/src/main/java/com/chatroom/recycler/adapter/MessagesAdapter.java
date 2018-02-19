package com.chatroom.recycler.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatroom.R;
import com.chatroom.model.Message;
import com.chatroom.recycler.viewholder.RecipientMessageVH;
import com.chatroom.recycler.viewholder.SenderMessageVH;
import com.chatroom.util.Helper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public abstract class MessagesAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private final int SENDER = 0;
    private final int RECEIVER = 1;
    private final DatabaseReference senderMessagesRef;
    private final DatabaseReference senderChatsRef;
    private final String senderUid;
    private final Context context;

    public MessagesAdapter(Context context, @NonNull FirebaseRecyclerOptions<Message> options, String senderUid, String receiverUid) {
        super(options);
        this.senderUid = senderUid;
        this.context = context;

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        senderChatsRef = chatsRef.child(senderUid).child(receiverUid);
        senderMessagesRef = messagesRef.child(senderUid).child(receiverUid);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull final Message model) {


        final int currPos = holder.getAdapterPosition();
        final String messageId = getRef(currPos).getKey();

        if (holder.getItemViewType() == SENDER) {


            final SenderMessageVH mHolder = (SenderMessageVH) holder;

            mHolder.setAttachment(context, model.getFile());
            mHolder.setText(model.getText());
            mHolder.setMessageInfo(model.getStatus(), model.getTimestamp());
            mHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDialog(currPos,messageId,model);
                    return true;
                }
            });

        } else if(holder.getItemViewType()==RECEIVER){

            final RecipientMessageVH mHolder = (RecipientMessageVH) holder;
            mHolder.setAttachment(context, model.getFile());
            mHolder.setText(model.getText());
            mHolder.setMessageInfo(model.getTimestamp());
            if (!model.getStatus().equals("seen")) {
                model.setStatus("seen");
                Helper.setMessageStatus(messageId, model);
            }
            mHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDialog(currPos,messageId,model);
                    return true;
                }
            });
        }

    }

    private void showDialog(final int currPos, final String messageId, final Message model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence items[] = new CharSequence[]{"Copy", "Delete"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Helper.copyText(context, model.getText());
                } else if (i == 1) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle("Delete");
                    builder1.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            delete(currPos, getItemCount(), messageId, model.getFile());
                        }
                    }).setNegativeButton("No", null).show();
                }
            }
        }).show();
    }

    private void delete(int currPos, int size, String messageKey, String file) {

        OnCompleteListener<Void> completeListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task task) {
                    notifyDataSetChanged();
            }
        };
        if (size == 1) {
            senderChatsRef.removeValue();
        } else if (currPos == size - 1) {
            senderChatsRef.child("timestamp").setValue(getItem(currPos - 1).getTimestamp());
        }
        senderMessagesRef.child(messageKey).removeValue().addOnCompleteListener(completeListener);
        Helper.deleteMedia(context, file);
    }

    @Override
    public int getItemViewType(int position) {
        String uid = getItem(position).getSenderUID();
        return senderUid.equals(uid) ? SENDER : RECEIVER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_item, parent, false);
            return new SenderMessageVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipient_message_item, parent, false);
            return new RecipientMessageVH(view);
        }
    }

    @Override
    public abstract void onDataChanged() ;
}
