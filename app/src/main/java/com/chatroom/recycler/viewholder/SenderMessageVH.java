package com.chatroom.recycler.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chatroom.R;
import com.chatroom.util.Helper;


public class SenderMessageVH extends RecyclerView.ViewHolder {
    private View mView;

    public SenderMessageVH(View itemView) {
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
    public void setMessageInfo(String status, long timestamp) {

        TextView text = mView.findViewById(R.id.message_info);
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
        text.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
        text.setText(Helper.getTimeAgo(timestamp) + " ");
    }
}
