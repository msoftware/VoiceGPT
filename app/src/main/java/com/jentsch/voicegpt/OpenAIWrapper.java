package com.jentsch.voicegpt;

import android.content.SharedPreferences;

import com.jentsch.voicegpt.db.entity.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class OpenAIWrapper {

    public static void doAsyncChatCompletionRequest(SharedPreferences prefs, List<ChatMessage> messages, OpenAI.ChatCompletionChunkResponseListener listener)
    {
        List<com.theokanning.openai.completion.chat.ChatMessage> openAIMessages = new ArrayList<>();

        // Convert entity.ChatMessage to openai....ChatMessage
        int anz = messages.size();

        // TODO Count tokens (MAX 4096)
        int start = Math.max(0, anz - 4);
        for (int i = start; i < anz; i++) {
            com.theokanning.openai.completion.chat.ChatMessage chatMessage = new com.theokanning.openai.completion.chat.ChatMessage();
            chatMessage.setContent(messages.get(i).message);
            chatMessage.setRole(messages.get(i).role);
            openAIMessages.add(chatMessage);
        }

        OpenAI.doAsyncChatCompletionRequest(prefs, openAIMessages, listener);
    }
}
