package com.chatroom.recycler.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chatroom.R;
import com.chatroom.util.Helper;


public class RecipientMessageVH extends RecyclerView.ViewHolder {
    private View mView;

    public RecipientMessageVH(View itemView) {
        super(itemView);
        mView = itemView;
    }
    public View getView() {
        return mView.findViewById(R.id.message_view);
    }

    public void setAttachment(final Context context, final String url) {
        Button btn = mView.findViewById(R.id.attachment_button);
        if (!url.equals("nil")) {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                }
            });
        } else {
            btn.setVisibility(View.GONE);
        }
    }

    public void setText(String text) {
        TextView textView = mView.findViewById(R.id.message_text);
        textView.setText(text);
        if(text.isEmpty())
            textView.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    public void setMessageInfo(long timestamp) {

        TextView text = mView.findViewById(R.id.message_info);
        text.setText(Helper.getTimeAgo(timestamp));
    }
}
