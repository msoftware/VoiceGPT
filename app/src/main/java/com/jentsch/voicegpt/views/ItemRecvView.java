package com.jentsch.voicegpt.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.cardview.widget.CardView;

import com.jentsch.voicegpt.R;

public class ItemRecvView extends MessageView {

    private CardView bubble;
    private TextView messageTextView, timestampTextView;

    public void setMessage(String message) {
        if (messageTextView == null) {
            messageTextView = findViewById(R.id.message_text_view);
        }
        messageTextView.setText(message);
    }

    public void setTimestamp(String timestamp) {
        if (timestampTextView == null) {
            timestampTextView = findViewById(R.id.timestamp_text_view);
        }
        timestampTextView.setText(timestamp);
    }

    public void setBackground(@ColorInt int background) {
        if (bubble == null) {
            this.bubble = findViewById(R.id.bubble);
        }
        bubble.setCardBackgroundColor(background);
    }

    public void setElevation(float elevation) {
        if (bubble == null) {
            this.bubble = findViewById(R.id.bubble);
        }
        bubble.setCardElevation(elevation);
    }

    public ItemRecvView(Context context) {
        super(context);
        initializeView(context);
    }

    public ItemRecvView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chat_item_rcv, this);

        this.bubble = findViewById(R.id.bubble);
        this.messageTextView = findViewById(R.id.message_text_view);
        this.timestampTextView = findViewById(R.id.timestamp_text_view);
    }
}