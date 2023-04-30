package com.jentsch.voicegpt;

import android.content.Context;

import com.jentsch.voicegpt.db.AppDatabase;
import com.jentsch.voicegpt.db.DbSingleton;
import com.jentsch.voicegpt.db.entity.ChatMessage;

import java.util.List;

public class ChatMessageStore {
    private final AppDatabase db;
    private List<ChatMessage> messages;

    public ChatMessageStore(Context context) {
        db = DbSingleton.instance(context).getAppDatabase();
        messages = db.chatMessageDao().loadAll();
        messages.size();
    }

    public void add(ChatMessage message) {
        messages.add(message);
        db.chatMessageDao().insert(message);
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
