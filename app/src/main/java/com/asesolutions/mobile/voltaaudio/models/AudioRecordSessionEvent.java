package com.asesolutions.mobile.voltaaudio.models;

public class AudioRecordSessionEvent {
    private long startTime;
    private long stopTime;

    public AudioRecordSessionEvent() {
        startTime = System.currentTimeMillis();
    }

    public long getDuration() {
        return stopTime - startTime;
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
}
