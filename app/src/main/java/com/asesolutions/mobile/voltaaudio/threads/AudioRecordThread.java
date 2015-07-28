package com.asesolutions.mobile.voltaaudio.threads;

import android.media.AudioRecord;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.models.AudioRecordConfig;
import com.asesolutions.mobile.voltaaudio.services.AudioRecordService;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecordThread extends Thread {
    private boolean running;
    private AudioRecordService.STATE state;
    private AudioRecordConfig audioRecordConfig;

    AudioRecordThread() {
        state = AudioRecordService.STATE.LISTENING;
        audioRecordConfig = new AudioRecordConfig();
    }

    public void stopRecording() {
        running = false;
    }

    @Override
    public void run() {
        // Set thread priority to high audio
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int minBufferSize = AudioRecord.getMinBufferSize(
                audioRecordConfig.getSampleRate(),
                audioRecordConfig.getChannelConfig(),
                audioRecordConfig.getAudioFormat());

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return;
        }

        int bufferSize = 4 * minBufferSize;

        AudioRecord audioRecord;
        try {
            audioRecord = new AudioRecord(
                    audioRecordConfig.getAudioSource(),
                    audioRecordConfig.getSampleRate(),
                    audioRecordConfig.getChannelConfig(),
                    audioRecordConfig.getAudioFormat(),
                    bufferSize);
        } catch (IllegalArgumentException e) {
            // TODO: notify others of failure and maybe why
            return;
        }

        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            // TODO: notify others of failure and maybe why
            return;
        }

        // TODO: Properly handle file creation
        String fileUri = MainApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + String.format("/%d.wav", System.currentTimeMillis());

        File file = new File(fileUri);
        file.mkdirs();

        DataOutputStream dataOutputStream;
        try {
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fileUri))));
        } catch (FileNotFoundException e) {
            // TODO: notify others of failure and maybe why
            return;
        }

        short[] audioBuffer = new short[bufferSize];

        audioRecord.startRecording();

        try {
            while (running) {
                int bytesRead = audioRecord.read(audioBuffer, 0, bufferSize);
                // TODO: Process audio record samples

                for (int i = 0; i < bytesRead; i++) {
                    dataOutputStream.writeShort(audioBuffer[i]);
                }
            }

            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioRecord.stop();
        audioRecord.release();
    }
}
