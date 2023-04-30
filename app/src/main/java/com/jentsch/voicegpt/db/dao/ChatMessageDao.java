package com.jentsch.voicegpt.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jentsch.voicegpt.db.entity.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Query("SELECT * FROM chat_message ORDER BY id")
    List<ChatMessage> loadAll();

    @Insert
    void insert(ChatMessage chatMessage);

    @Insert
    void insertAll(ChatMessage... chatMessage);

    @Update
    void updateAll(ChatMessage... chatMessage);

    @Delete
    void delete(ChatMessage chatMessage);

    @Query("DELETE FROM chat_message")
    void deleteAll();
}
