package com.jentsch.voicegpt;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class ElevenLabsWrapper {

    private static final String TAG = "ElevenLabsWrapper";

    private static final String ELEVENLABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech/";

    private static MediaPlayer mediaPlayer = null;

    private static boolean isRunning = false;

    public static synchronized void speakAsync(Context context, String apiKey, String voiceId, String msg, boolean cache, ElevenLabsSpeakListener listener) {
        (new Thread() {
            @Override
            public void run() {
                speak(context, apiKey, voiceId, msg, cache, listener);
            }
        }).start();
    }

    public static void speak(Context context, String apiKey, String voiceId, String msg, boolean cache, ElevenLabsSpeakListener listener) {
        isRunning = true;
        int responseCode= 0;
        try {
            String hash = getMD5Hash(msg);
            File filesDir = context.getFilesDir ();
            File downloadFile = new File(filesDir, "/" + hash + ".mpeg");
            if (cache) {
                if (downloadFile.exists()) {
                    // Use Cache File
                    if (listener != null) {
                        listener.updateState(ElevenLabsSpeakListener.State.SPEAKING);
                    }
                    playMediaFile(downloadFile);
                    if (listener != null) {
                        listener.updateState(ElevenLabsSpeakListener.State.FINISHED);
                    }
                    return;
                }
            }

            if (listener != null) {
                listener.updateState(ElevenLabsSpeakListener.State.DOWNLOAD);
            }
            URL url = new URL(ELEVENLABS_API_URL+voiceId);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "audio/mpeg");
            con.setRequestProperty("xi-api-key", apiKey);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("text", msg);
            payload.put("model_id", "eleven_multilingual_v1");
            payload.put("language_id", "de-de");
            JSONObject voiceSettings = new JSONObject();
            voiceSettings.put("stability", 0.15); // TODO Settings
            voiceSettings.put("similarity_boost", 0.65); // TODO Settings
            payload.put("voice_settings", voiceSettings);

            OutputStream os = con.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();

            responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = con.getInputStream().read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();
                if (listener != null) {
                    listener.updateState(ElevenLabsSpeakListener.State.SPEAKING);
                }
                playMediaFile(downloadFile);
                if (!cache) {
                    downloadFile.delete();
                }
            } else {
                String responseMessage = con.getResponseMessage();
                Log.d(TAG, responseMessage);

                int finalResponseCode = responseCode;
                ContextCompat.getMainExecutor(context).execute(()  -> {
                    Toast.makeText(context, "ElevenLabs Error Code " + finalResponseCode, Toast.LENGTH_LONG).show();
                });
            }
            con.disconnect();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (listener != null) {
            listener.updateState(ElevenLabsSpeakListener.State.FINISHED);
        }
        isRunning = false;
    }

    private static void playMediaFile(File file) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource("file://"+ file.getAbsolutePath());
        mediaPlayer.prepare();
        mediaPlayer.start();
        isRunning = mediaPlayer.isPlaying();
        while(isRunning) {
            isRunning = mediaPlayer.isPlaying();
        }
    }

    public static String getMD5Hash(String md5) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(md5.getBytes("UTF-8"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static boolean isSpeaking() {
        return isRunning;
    }

    public static void stopSpeaking() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        isRunning = false;
    }
}
