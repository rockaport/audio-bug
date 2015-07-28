package com.asesolutions.mobile.voltaaudio.ui;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.asesolutions.mobile.voltaaudio.R;
import com.asesolutions.mobile.voltaaudio.util.AudioUtil;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        ListPreference sampleRatesListPreference =
                (ListPreference)findPreference(getString(R.string.pref_sample_rate_key));

        ArrayList<Integer> sampleRatesIndex = AudioUtil.getSupportedSampleRatesIndex();

        String[] sampleRates =
                getResources().getStringArray(R.array.sample_rate_strings);
        String[] sampleRateDescriptions =
                getResources().getStringArray(R.array.sample_rate_descriptions);

        CharSequence[] sampleRateEntries = new CharSequence[sampleRatesIndex.size()];
        CharSequence[] sampleRateEntryValues = new CharSequence[sampleRatesIndex.size()];
        for (int i = 0; i < sampleRatesIndex.size() ; i++) {
            sampleRateEntries[i] = sampleRateDescriptions[sampleRatesIndex.get(i)];
            sampleRateEntryValues[i] = sampleRates[sampleRatesIndex.get(i)];
        }

        sampleRatesListPreference.setEntries(sampleRateEntries);
        sampleRatesListPreference.setEntryValues(sampleRateEntryValues);
        sampleRatesListPreference.setDefaultValue(sampleRateEntryValues[0]);
    }
}
