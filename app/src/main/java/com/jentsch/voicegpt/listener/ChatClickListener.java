package com.jentsch.voicegpt.listener;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.jentsch.voicegpt.db.entity.ChatMessage;

public interface ChatClickListener {
    void onChatItemClick(ChatMessage chatMessage, LinearProgressIndicator progressBar);
}
