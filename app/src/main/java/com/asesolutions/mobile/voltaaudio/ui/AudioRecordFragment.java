package com.asesolutions.mobile.voltaaudio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.R;
import com.asesolutions.mobile.voltaaudio.models.events.AudioRecordServiceEvent;
import com.asesolutions.mobile.voltaaudio.services.AudioRecordService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioRecordFragment extends Fragment {
    // Static variables and constants
    private static final int CHART_UPDATE_PERIOD = 100;
    private static float YAXIS_MIN = 0;
    private static float YAXIS_MAX = 0;

    // UI Components via butterknife
    @Bind(R.id.chart)
    LineChart audioLevelChart;
    @Bind(R.id.fab_record)
    FloatingActionButton recordButton;

    // General fields
    private Bus bus;
    private Handler handler;
    private Random randomeGen;

    // Chart related fields
    private LineData lineData;
    private LineDataSet lineDataSet;

    public AudioRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize fields
        bus = MainApplication.getBus();
        handler = new Handler();
        randomeGen = new Random();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_record, container, false);

        // Inject views
        ButterKnife.bind(this, view);

        initializeChart();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Register for events
        bus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Synchronize the UI
        syncUi();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister for events
        bus.unregister(this);

        // Remove all handler callbacks
        handler.removeCallbacksAndMessages(null);
    }

    @OnClick(R.id.fab_record)
    public void toggleAudioRecording() {
        if (!MainApplication.getState().isRecording()) {
            getActivity().startService(new Intent(getActivity(), AudioRecordService.class));
        } else {
            getActivity().stopService(new Intent(getActivity(), AudioRecordService.class));
        }

        syncUi();
    }

    @Subscribe
    public void processAudioRecordServiceEvent(AudioRecordServiceEvent event) {
        syncUi();
    }

    private void initializeChart() {
        // Only display 30 xticks worth of data
        audioLevelChart.setVisibleXRangeMaximum(30);

        // Don't display the legend and description (single line chart)
        audioLevelChart.setDescription("");
        Legend legend = audioLevelChart.getLegend();
        legend.setEnabled(false);

        // Remove the xaxis (clutter)
        XAxis xAxis = audioLevelChart.getXAxis();
        xAxis.setEnabled(false);

        // Format the yaxis. This will be automatically updated depending on the data
        YAxis yAxis = audioLevelChart.getAxisLeft();
        yAxis.setStartAtZero(false);
        yAxis.setAxisMinValue(YAXIS_MIN);
        yAxis.setAxisMaxValue(YAXIS_MAX);

        // Remove the right yaxis (unnecessary)
        audioLevelChart.getAxisRight().setEnabled(false);

        // Create the label/entry pair for a single line chart with some initial values (0,0)
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        labels.add(String.valueOf(0));
        entries.add(new Entry(0, 0));

        // Create the actual line data set
        lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Set the value labels to an empty string (we don't want to display the number)
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });

        // Create a line data object and add label/entry pairs
        lineData = new LineData(labels, lineDataSet);
        audioLevelChart.setData(lineData);
    }

    private void syncUi() {
        if (MainApplication.getState().isRecording()) {
            recordButton.setImageResource(R.drawable.ic_mic_off_white_24dp);
            handler.postDelayed(new AudioLevelUpdater(), CHART_UPDATE_PERIOD);
        } else {
            recordButton.setImageResource(R.drawable.ic_mic_white_24dp);
            handler.removeCallbacksAndMessages(null);
        }
    }

    private class AudioLevelUpdater implements Runnable {
        @Override
        public void run() {
            // Generate some new data
            float val = 4 * (randomeGen.nextFloat() - 0.5f);
            if (lineDataSet.getEntryCount() > 0) {
                val = val + lineDataSet.getEntryForXIndex(lineDataSet.getEntryCount()).getVal();
            }

            // Add a new entry and x-value
            lineDataSet.addEntry(new Entry(val, lineDataSet.getEntryCount()));
            lineData.addXValue(String.valueOf(lineDataSet.getEntryCount()));

            // Automagically update axis
            YAXIS_MIN = (float) (Math.floor(Math.min(YAXIS_MIN, val) * 10) * 0.1);
            YAXIS_MAX = (float) (Math.ceil(Math.max(YAXIS_MAX, val) * 10) * 0.1);
            YAxis yAxis = audioLevelChart.getAxisLeft();
            yAxis.setAxisMinValue(YAXIS_MIN);
            yAxis.setAxisMaxValue(YAXIS_MAX);

            // Refresh the chart. We need to reset the x-range maximum since the chart does not
            // maintain those values
            audioLevelChart.notifyDataSetChanged();
            audioLevelChart.setVisibleXRangeMaximum(30);

            // Manually scroll to an x position
            audioLevelChart.moveViewToX(Math.max(lineData.getXValCount() - 30, 0));

            handler.postDelayed(this, CHART_UPDATE_PERIOD);
        }
    }
}
