package com.jentsch.voicegpt.util;

import android.util.Log;

import com.jentsch.voicegpt.db.entity.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenManager
{

    private static final String TAG = "TokenManager";

    /*
     * Simple word count solution
     */
    public static int countTokens(String message) {
        if (message == null) return 0;
        String alphaAndDigits = message.replaceAll("[^a-zA-Z0-9]+"," ");
        String[] words = alphaAndDigits.split("\\s+");
        return words.length * 10;
    }

    public static List<ChatMessage> reduceMessages(List<ChatMessage> messages, int maxTokenAnz) {
        List<ChatMessage> ret = new ArrayList<>();
        if (messages ==null) return null;
        if (maxTokenAnz < 1) return messages;

        int anzTokens = 0;
        Collections.reverse(messages);
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            anzTokens += countTokens(message.getMessage());
            if (anzTokens < maxTokenAnz) {
                ret.add(message);
            } else {
                Log.d(TAG, "maxTokenAnz reached");
                break;
            }
        }
        Collections.reverse(ret);
        return ret;
    }
}
