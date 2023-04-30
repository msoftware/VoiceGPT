package com.jentsch.voicegpt.listener;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechRecognitionListener implements RecognitionListener
{

    private final SpeechResponseListener listener;

    private static final String TAG = "SpeechRecognitionListen";
    private final Context context;
    private String lastContent = null;

    public SpeechRecognitionListener(Context context, SpeechResponseListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(context, "Info: " + "Ready for speech", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        String msg = "Undefined error code " + error;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                msg = "Audio recording error.";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                msg = "Other client side errors.";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                msg = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                msg = "Other network related errors.";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                msg = "Network operation timed out.";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                msg = "No recognition result matched.";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                msg = "RecognitionService busy.";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                msg = "Server sends error status.";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                msg = "No speech input.";
                break;
        }
        Log.d(TAG, "Error " + msg);
    }

    @Override
    public void onResults(Bundle bundle)
    {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "Recognition log");
        String recognition = data.get(0);
        Log.d(TAG, recognition);
        listener.onSpeechResponse(recognition);
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "Part: " + data.get(0));
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    public interface SpeechResponseListener {
        void onSpeechResponse(String content);
    }
}
