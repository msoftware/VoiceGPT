package com.jentsch.voicegpt.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jentsch.voicegpt.R;

public abstract class MessageView extends FrameLayout {

    private TextView senderTextView;

    public abstract void setMessage(String message);
    public abstract void setTimestamp(String timestamp);
    public abstract void setBackground(int background);
    public abstract void setElevation(float elevation);

    public void setSender(String sender) {
        if (senderTextView == null) {
            this.senderTextView = findViewById(R.id.sender_text_view);
        }
        senderTextView.setVisibility(VISIBLE);
        senderTextView.setText(sender);
    }

    public MessageView(Context context) {
        super(context);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
