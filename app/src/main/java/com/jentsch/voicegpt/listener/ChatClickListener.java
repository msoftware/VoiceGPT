package com.jentsch.voicegpt.listener;

import com.jentsch.voicegpt.db.entity.ChatMessage;

public interface ChatClickListener {
    void onChatItemClick(ChatMessage chatMessage);
}
