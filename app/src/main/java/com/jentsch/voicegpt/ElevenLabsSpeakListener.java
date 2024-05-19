package com.jentsch.voicegpt;

public interface ElevenLabsSpeakListener {
    public enum State {
        DOWNLOAD,
        SPEAKING,
        FINISHED
    }
    void updateState(State state);
}
