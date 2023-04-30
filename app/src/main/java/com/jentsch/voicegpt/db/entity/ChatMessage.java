package com.jentsch.voicegpt.db.entity;

import android.text.format.DateFormat;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.concurrent.TimeUnit;

@Entity(tableName = "chat_message")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String message;
    public long timestamp;
    public String role;

    @Ignore
    public ChatMessage() {
    }

    public ChatMessage(String role, String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormattedTime() {

        long oneDayInMillis = TimeUnit.DAYS.toMillis(1); // 24 * 60 * 60 * 1000;

        long timeDifference = System.currentTimeMillis() - timestamp;

        return timeDifference < oneDayInMillis
                ? DateFormat.format("hh:mm a", timestamp).toString()
                : DateFormat.format("dd MMM - hh:mm a", timestamp).toString();
    }
}
