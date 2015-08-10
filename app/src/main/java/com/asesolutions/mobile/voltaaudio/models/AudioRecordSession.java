package com.asesolutions.mobile.voltaaudio.models;

import java.util.ArrayList;

public class AudioRecordSession {
    private long startTime;
    private long stopTime;
    private ArrayList<AudioRecordSessionEvent> audioRecordSessionEvents;

    public AudioRecordSession() {
        startTime = System.currentTimeMillis();
    }

    public long getDuration() {
        return stopTime - startTime;
    }

    public int getNumEvents() {
        if (audioRecordSessionEvents == null) {
            return 0;
        }

        return audioRecordSessionEvents.size();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public ArrayList<AudioRecordSessionEvent> getAudioRecordSessionEvents() {
        return audioRecordSessionEvents;
    }

    public void setAudioRecordSessionEvents(ArrayList<AudioRecordSessionEvent> audioRecordSessionEvents) {
        this.audioRecordSessionEvents = audioRecordSessionEvents;
    }
}
