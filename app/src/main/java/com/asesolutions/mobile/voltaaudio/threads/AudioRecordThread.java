package com.asesolutions.mobile.voltaaudio.threads;

import android.media.AudioRecord;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.models.AudioDetector;
import com.asesolutions.mobile.voltaaudio.models.AudioRecordConfig;
import com.asesolutions.mobile.voltaaudio.models.events.AudioRecordThreadEvent;
import com.asesolutions.mobile.wav.WavFile;
import com.squareup.otto.Bus;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioRecordThread extends Thread {
    private boolean running;
    private STATE state;
    private AudioRecordConfig audioRecordConfig;
    private Bus bus;

    AudioRecordThread() {
        audioRecordConfig = new AudioRecordConfig();
        bus = MainApplication.getBus();
    }

    public void stopRecording() {
        running = false;
    }

    @Override
    public void run() {
        // Register to produce events
        bus.register(this);

        sendEvent(STATE.INITIALIZING);

        // Set thread priority to high audio
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int minBufferSize = AudioRecord.getMinBufferSize(
                audioRecordConfig.getSampleRate(),
                audioRecordConfig.getChannelConfig(),
                audioRecordConfig.getAudioFormat());

        // AudioRecord.ERROR or AudioRecord.ERROR_BAD_VALUE
        if (minBufferSize <= 0) {
            sendEvent(STATE.STOPPED, "Error getting audio buffer size");
            cleanup();
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
            sendEvent(STATE.STOPPED, "Error initializing audio record session: " + e.toString());
            cleanup();
            return;
        }

        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            sendEvent(STATE.STOPPED, "Error initializing audio record session");
            cleanup();
            return;
        }

        // TODO: Properly handle file creation
        String fileUri = MainApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + String.format("/%d.wav", System.currentTimeMillis());

        File file = new File(fileUri);
        file.mkdirs();

        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(fileUri, "rw");
        } catch (FileNotFoundException e) {
            sendEvent(STATE.STOPPED, "Error creating file: " + e.toString());
            cleanup();
            return;
        }

        WavFile wavFile = new WavFile(audioRecordConfig.getSampleRate(), Short.SIZE / Byte.SIZE);
        try {
            wavFile.writeFile(randomAccessFile);
        } catch (IOException e) {
            sendEvent(STATE.STOPPED, "Error creating wav file: " + e.toString());
            cleanup();
            return;
        }

        AudioDetector audioDetector = new AudioDetector(audioRecordConfig.getSensitivity());

        short[] audioBuffer = new short[bufferSize];
        short[] processingBuffer = new short[audioRecordConfig.getSampleRate()];
        int samplesRead = 0;

        audioRecord.startRecording();

        sendEvent(STATE.LISTENING);
        // Create a new session

        try {
            while (running) {
                int bytesRead = audioRecord.read(audioBuffer, 0, bufferSize);

                for (int i = 0; i < bytesRead; i++) {
                    // Fill the processing buffer
                    processingBuffer[samplesRead++] = audioBuffer[i];

                    // Keep going if we don't have enough sampples (1 seconds worth)
                    if (samplesRead < audioRecordConfig.getSampleRate()) {
                        continue;
                    }

                    // Reset the number of samples read
                    samplesRead = 0;

                    // Process this buffer and get the result
                    AudioDetector.Result result = audioDetector.process(processingBuffer);

                    if (result.isRecording()) {
                        if (state == STATE.LISTENING) {
                            sendEvent(STATE.RECORDING);

                            // Create a new event
                        }

                        // Write samples to file
                        wavFile.writeData(randomAccessFile, processingBuffer);
                    } else {
                        if (state == STATE.RECORDING) {
                            sendEvent(STATE.LISTENING);

                            // Terminate this event
                        }
                    }
                }
            }

            // Close the file
            randomAccessFile.close();
        } catch (IOException e) {
            sendEvent(STATE.STOPPED, "Error saving audio to file: " + e.toString());
        }

        // Stop/release the audioRecord instance
        audioRecord.stop();
        audioRecord.release();

        // Send event and cleanup
        sendEvent(STATE.STOPPED);
        cleanup();
    }

    private void setState(STATE state) {
        this.state = state;
    }

    private void sendEvent(STATE state) {
        sendEvent(state, "");
    }

    private void sendEvent(STATE state, String message) {
        setState(state);

        bus.post(new AudioRecordThreadEvent(state, message));
    }

    private void cleanup() {
        bus.unregister(this);
    }

    public enum STATE {
        INITIALIZING,
        LISTENING,
        RECORDING,
        STOPPED,
    }
}
