package com.asesolutions.mobile.voltaaudio;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.squareup.otto.Bus;

public class MainApplication extends Application {
    private static Context context;
    private static Resources resources;

    private static State state;
    private static Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();

        if (context == null) {
            context = getApplicationContext();
        }

        if (resources == null) {
            resources = getResources();
        }

        if (state == null) {
            state = new State();
        }

        if (bus == null) {
            bus = new Bus();
        }
    }

    public static Context getContext() {
        return context;
    }

    public static Resources getAppResources() {
        return resources;
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
