package com.jentsch.voicegpt.adapters;

import static com.theokanning.openai.completion.chat.ChatMessageRole.*;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jentsch.voicegpt.R;
import com.jentsch.voicegpt.listener.ChatClickListener;
import com.jentsch.voicegpt.db.entity.ChatMessage;

import java.util.ArrayList;


public class ChatViewListAdapter extends RecyclerView.Adapter<ChatViewListAdapter.ViewHolder> implements ChatClickListener {

    private static final String TAG = "ChatViewListAdapter";

    public final int UNDEFINED_TYPE = 0;
    public final int SENT_TYPE = 1;
    public final int RECEIVED_TYPE = 2;
    private final Context context;

    ArrayList<ChatMessage> chatMessages;
    private ChatClickListener chatClickListener = null;

    public ChatViewListAdapter (Context context, ChatClickListener chatClickListener) {
        this.context = context;
        this.chatClickListener = chatClickListener;
        this.chatMessages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == SENT_TYPE) {
            View chatItemRcvView = inflater.inflate(R.layout.chat_item_rcv, parent, false);
            ViewHolder viewHolder = new ViewHolder(chatItemRcvView, viewType);
            return viewHolder;
        } else if (viewType == RECEIVED_TYPE) {
            View chatItemSentView = inflater.inflate(R.layout.chat_item_sent, parent, false);
            ViewHolder viewHolder = new ViewHolder(chatItemSentView, viewType);
            return viewHolder;
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (chatMessage.role.equals(ASSISTANT.value()))
        {
            return RECEIVED_TYPE;
        } else if (chatMessage.role.equals(USER.value()))
        {
            return SENT_TYPE;
        } else {
            return UNDEFINED_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);

        holder.senderTextView.setText(chatMessage.role);
        holder.messageTextView.setText(chatMessage.message);
        holder.timestampTextView.setText(chatMessage.getFormattedTime());
        holder.clickListener = this;
        holder.chatMessage = chatMessage;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
    }

    public void clearMessages() {
        chatMessages.clear();
    }

    @Override
    public void onChatItemClick(ChatMessage chatMessage) {
        if (chatClickListener != null) {
            chatClickListener.onChatItemClick(chatMessage);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView senderTextView;
        public TextView messageTextView;
        public TextView timestampTextView;

        public ChatMessage chatMessage;
        public ChatClickListener clickListener;
        public FrameLayout chatLayout;

        public int viewType;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            chatLayout = itemView.findViewById(R.id.chat_layout);
            chatLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onChatItemClick(chatMessage);
                }
            });
            chatLayout.setLongClickable(true);
            chatLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Text", chatMessage.message);
                    clipboard.setPrimaryClip(clip);
                    return true;
                }
            });
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }
}