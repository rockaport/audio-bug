package com.asesolutions.mobile.voltaaudio.util;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.R;

import java.util.ArrayList;

public class AudioUtil {
    public static ArrayList<Integer> getSupportedSampleRatesIndex() {
        int[] desiredSampleRates =
                MainApplication.getAppResources().getIntArray(R.array.sample_rate_ints);

        ArrayList<Integer> supportedSampleRatesIndex = new ArrayList<>();

        for (int i = 0; i < desiredSampleRates.length; i++) {
            int bufferSize = AudioRecord.getMinBufferSize(
                    desiredSampleRates[i],
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                continue;
            }

            supportedSampleRatesIndex.add(i);
        }

        return supportedSampleRatesIndex;
    }
}
