package com.jentsch.voicegpt.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.jentsch.voicegpt.db.dao.ChatMessageDao;
import com.jentsch.voicegpt.db.entity.ChatMessage;

@Database(entities = {ChatMessage.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();
}
