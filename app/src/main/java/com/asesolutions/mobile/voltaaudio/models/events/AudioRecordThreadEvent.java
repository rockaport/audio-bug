package com.asesolutions.mobile.voltaaudio.models.events;

import com.asesolutions.mobile.voltaaudio.threads.AudioRecordThread;

public class AudioRecordThreadEvent {
    AudioRecordThread.STATE state;
    String message;

    public AudioRecordThreadEvent(AudioRecordThread.STATE state) {
        this(state, "");
    }

    public AudioRecordThreadEvent(AudioRecordThread.STATE state, String message) {
        this.state = state;
        this.message = message;
    }

    public AudioRecordThread.STATE getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
