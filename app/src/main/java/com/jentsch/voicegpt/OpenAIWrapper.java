package com.jentsch.voicegpt;

import android.content.SharedPreferences;

import com.jentsch.voicegpt.db.entity.ChatMessage;
import com.jentsch.voicegpt.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class OpenAIWrapper {

    public static void doAsyncChatCompletionRequest(SharedPreferences prefs, ChatMessageStore messageStore, OpenAI.ChatCompletionChunkResponseListener listener)
    {
        List<ChatMessage> allMessages = messageStore.getMessages();
        List<ChatMessage> chatMessages = TokenManager.reduceMessages(allMessages,4000);

        markChatMessages (allMessages, chatMessages);

        List<com.theokanning.openai.completion.chat.ChatMessage> openAIMessages = new ArrayList<>();

        for (int i = 0; i < chatMessages.size(); i++) {
            com.theokanning.openai.completion.chat.ChatMessage chatMessage = new com.theokanning.openai.completion.chat.ChatMessage();
            chatMessage.setContent(chatMessages.get(i).message);
            chatMessage.setRole(chatMessages.get(i).role);
            openAIMessages.add(chatMessage);
        }

        OpenAI.doAsyncChatCompletionRequest(prefs, openAIMessages, listener);
    }

    private static void markChatMessages(List<ChatMessage> allMessages, List<ChatMessage> chatMessages) {
        for (ChatMessage message : allMessages) {
            if (chatMessages.contains(message)) {
                message.setChatMessage(true);
            } else {
                message.setChatMessage(false);
            }
        }
    }
}
