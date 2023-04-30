package com.jentsch.voicegpt;

import static com.jentsch.voicegpt.SettingsActivity.MAX_TOKENS;
import static com.jentsch.voicegpt.SettingsActivity.OPENAI_API_KEY;

import android.content.Context;
import android.content.SharedPreferences;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.functions.Consumer;

public class OpenAI
{

    public static synchronized void doAsyncChatCompletionRequest(SharedPreferences prefs, List<ChatMessage> messages, ChatCompletionChunkResponseListener listener)
    {
        String token = prefs.getString(OPENAI_API_KEY, null);
        int maxTokens = prefs.getInt(MAX_TOKENS, 500);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<ChatCompletionChunk> chatCompletionChunks = OpenAI.doChatCompletionRequest(token, maxTokens, messages);
                    if (listener != null) {
                        String content = "";
                        for (ChatCompletionChunk chatCompletionChunk : chatCompletionChunks) {
                            List<ChatCompletionChoice> choices = chatCompletionChunk.getChoices();
                            if (choices != null) {
                                ChatCompletionChoice choice = choices.get(0);
                                if (choice != null) {
                                    ChatMessage message = choice.getMessage();
                                    if (message != null) {
                                        if (message.getContent() != null) {
                                            content += message.getContent();
                                        }
                                    }
                                }
                            }
                        }
                        listener.publishResponse (content);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.publishResponse (e.getMessage());
                    }
                }
            }
        };
        t.start();
    }

    private static ArrayList<ChatCompletionChunk> doChatCompletionRequest(String token, int maxTokens, List<ChatMessage> messages)
    {
        ArrayList<ChatCompletionChunk> ret = new ArrayList<>();
        OpenAiService service = new OpenAiService(token);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(maxTokens)
                .temperature(0.8)
                .logitBias(new HashMap<>())
                .n(1)
                .build();

        Consumer<? super ChatCompletionChunk> appendResult = new Consumer<ChatCompletionChunk>() {
            @Override
            public void accept(ChatCompletionChunk chatCompletionChunk) throws Exception {
                ret.add(chatCompletionChunk);
            }
        };
        service.streamChatCompletion(chatCompletionRequest)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(appendResult);

        service.shutdownExecutor();
        return ret;
    }

    public interface ChatCompletionChunkResponseListener {

        void publishResponse(String content);
    }
}
