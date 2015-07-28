package com.asesolutions.mobile.voltaaudio;

import android.app.Application;

import com.squareup.otto.Bus;

public class MainApplication extends Application {
    private static State state;
    private static Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();

        if (state == null) {
            state = new State();
        }

        if (bus == null) {
            bus = new Bus();
        }
    }

    public static State getState() {
        return state;
    }

    public static Bus getBus() {
        return bus;
    }

    public class State {
        boolean isRecording;

        public State() {
        }

        public boolean isRecording() {
            return isRecording;
        }

        public void setIsRecording(boolean isRecording) {
            this.isRecording = isRecording;
        }
    }
}
