package com.asesolutions.mobile.voltaaudio.models;

import java.util.Arrays;

public class AudioDetector {
    // sliding detection window size
    private static final int WINDOW_SIZE = 5;

    // Number of seconds to keep recording after the power drops below the threshold
    private static final int TAPER_DURATION = 3;

    private double threshold; // in dB
    private double recordThreshold; // in dB
    private double curLevel; // in dB

    // This is estimated to be the median value of the sliding window
    private double noiseFloor;

    private double[] powerBuffer;
    private double[] sortedPowerBuffer;

    private int count, taperCount;
    private int tempPower;

    public AudioDetector(double threshold) {
        this.threshold = threshold;

        powerBuffer = new double[WINDOW_SIZE];
        sortedPowerBuffer = new double[WINDOW_SIZE];
    }

    public Result process(short[] buffer) {
        for (short aBuffer : buffer) {
            tempPower = Math.abs(aBuffer);
        }

        curLevel = 10 * Math.log(tempPower / Short.MAX_VALUE);
        powerBuffer[count % WINDOW_SIZE] = curLevel;

        count++;

        if (count < WINDOW_SIZE) {
            return new Result(false, curLevel, curLevel + threshold);
        }

        System.arraycopy(powerBuffer, 0, sortedPowerBuffer, 0, WINDOW_SIZE);
        Arrays.sort(sortedPowerBuffer);

        noiseFloor = sortedPowerBuffer[WINDOW_SIZE >> 2];

        if (curLevel > noiseFloor + threshold) {
            recordThreshold = noiseFloor + threshold;

            taperCount = TAPER_DURATION;
            return new Result(true, curLevel, noiseFloor + threshold);
        }

        taperCount--;

        if (taperCount >= 0) {
            return new Result(true, curLevel, noiseFloor + threshold);
        }

        return new Result(false, curLevel, noiseFloor + threshold);
    }

    public class Result {
        private boolean isRecording;
        private double currentLevel;
        private double threshold;

        public Result(boolean isRecording, double currentLevel, double threshold) {
            this.isRecording = isRecording;
            this.currentLevel = currentLevel;
            this.threshold = threshold;
        }

        public boolean isRecording() {
            return isRecording;
        }

        public double getCurrentLevel() {
            return currentLevel;
        }

        public double getThreshold() {
            return threshold;
        }
    }
}
