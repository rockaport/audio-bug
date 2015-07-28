package com.asesolutions.mobile.voltaaudio.models;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.R;

public class AudioRecordConfig {
    private final int audioSource = MediaRecorder.AudioSource.MIC;
    private final int sampleRate;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private final float sensitivity;

    public AudioRecordConfig() {
        Resources resources = MainApplication.getAppResources();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext());

        // Set the sample rate
        String sampleRate = sharedPreferences.getString(
                resources.getString(R.string.pref_sample_rate_key),
                resources.getString(R.string.pref_sample_rate_default));

        this.sampleRate = Integer.valueOf(sampleRate);

        String sensitivity = sharedPreferences.getString(
                resources.getString(R.string.pref_sample_rate_key),
                resources.getString(R.string.pref_sensitivity_default));

        this.sensitivity = Float.valueOf(sensitivity);
    }

    public int getAudioSource() {
        return audioSource;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public float getSensitivity() {
        return sensitivity;
    }
}
