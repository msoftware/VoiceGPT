package com.jentsch.voicegpt;

import static com.jentsch.voicegpt.SettingsActivity.ELEVENLABS_ENABLE;
import static com.jentsch.voicegpt.SettingsActivity.OPENAI_API_KEY;
import static com.theokanning.openai.completion.chat.ChatMessageRole.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.jentsch.voicegpt.db.entity.ChatMessage;
import com.jentsch.voicegpt.listener.ChatClickListener;
import com.jentsch.voicegpt.listener.SpeechRecognitionListener;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ChatClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 213123;

    ChatView chatView = null;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    public static final Integer RecordAudioRequestCode = 1;
    private Intent speechRecognizerIntent;
    private ChatMessageStore messages;

    // TODO Settings
    String voiceId = "21m00Tcm4TlvDq8ikWAM";
    private static final Locale LANGUAGE = Locale.GERMANY;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission();
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        messages = new ChatMessageStore(this);
        chatView = findViewById(R.id.chat_view);
        chatView.setMicrophoneClickListener(() -> {
            if (speechRecognizerIntent != null) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });

        chatView.setSendClickListener(message -> {
            long stamp = System.currentTimeMillis();
            if (!TextUtils.isEmpty(message)) {
                String apiKey = prefs.getString(OPENAI_API_KEY, null);
                if (apiKey != null) {
                    sendMessage(message, stamp);
                } else {
                    String msg = "Please enter your OpenAI API Token first";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });

        chatView.setChatClickListener(this);

        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {}

            @Override
            public void userStoppedTyping() {}
        });

        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true);
            speechRecognizer.setRecognitionListener(new SpeechRecognitionListener(this, new SpeechRecognitionListener.SpeechResponseListener() {
                @Override
                public void onSpeechResponse(String recognition) {
                    chatView.setMessage(recognition);
                }
            }));

            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        textToSpeech.setLanguage(LANGUAGE);
                    }
                }
            });
        } else {
            chatView.disableMicButton();
        }

        for (ChatMessage message: messages.getMessages()) {
            chatView.addMessage(message);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                showSettings();
                break;
            case R.id.menu_about:
                showAbout();
                break;
            default:
                return false;
        }
        return false;
    }

    public void speakWithElevenLabs(String msg) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String elevenLabsApiKey = prefs.getString(SettingsActivity.ELEVENLABS_API_KEY, null);
            if (elevenLabsApiKey == null) {
                Toast.makeText(this, "ELEVENLABS_API_KEY missing.", Toast.LENGTH_LONG).show();
            } else {

                if (ElevenLabsWrapper.isSpeaking()) {
                    ElevenLabsWrapper.stopSpeaking();
                } else {
                    try {
                        ElevenLabsWrapper.speakAsync(MainActivity.this, elevenLabsApiKey, voiceId, msg, true);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAbout() {
        Intent i = new Intent(this, InfoActivity.class);
        startActivity(i);
    }

    private void showSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
        ActivityCompat.startActivityForResult(this, i, REQUEST_CODE, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshChatList();
    }

    private void refreshChatList() {
        messages = new ChatMessageStore(this);
        chatView.clearMessages();
        for (ChatMessage message: messages.getMessages()) {
            chatView.addMessage(message);
        }
    }

    private void sendMessage(String message, long stamp) {
        chatView.sendMessage(message,stamp);
        ChatMessage userMessage = new ChatMessage(USER.value(), message, System.currentTimeMillis());
        messages.add(userMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatView.showProgress(true);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        OpenAIWrapper.doAsyncChatCompletionRequest(prefs, messages.getMessages(), content -> {
            ChatMessage assistantMessage = new ChatMessage(ASSISTANT.value(), content, System.currentTimeMillis());
            messages.add(assistantMessage);
            Log.d(TAG, "Recognition log ...");
            Log.d(TAG, content);
            String lastContent = content;

            chatView.addMessage(assistantMessage);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update recycler
                    chatView.showProgress(false);
                    chatView.chatViewListAdapter.notifyDataSetChanged();
                    chatView.chatListView.scrollToPosition(chatView.chatViewListAdapter.getItemCount() - 1);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChatItemClick(com.jentsch.voicegpt.db.entity.ChatMessage chatMessage) {

        if (prefs.getInt(ELEVENLABS_ENABLE, 0) == 1) {
            speakWithElevenLabs(chatMessage.message);
        } else {
            HashMap<String, String> params = null;
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            } else {
                (new Thread() {
                    @Override
                    public void run() {
                        textToSpeech.speak(chatMessage.message, TextToSpeech.QUEUE_FLUSH, params);
                    }
                }).start();
            }
        }
    }
}
