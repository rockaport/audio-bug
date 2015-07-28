package com.asesolutions.mobile.voltaaudio.ui;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asesolutions.mobile.voltaaudio.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AudioRecordFragment extends Fragment {

    private static final int CHART_UPDATE_PERIOD = 100;
    private static float YAXIS_MIN = 0;
    private static float YAXIS_MAX = 0;

    private Handler handler;

    @Bind(R.id.chart)
    LineChart audioLevelChart;
    @Bind(R.id.fab_record)
    FloatingActionButton recordButton;

    boolean isRecording;

    private LineData lineData;
    private LineDataSet lineDataSet;

    private Random randomeGen;

    public AudioRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        // Initialize a RNG
        randomeGen = new Random();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_record, container, false);

        // Inject views
        ButterKnife.bind(this, view);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudioRecording();

            }
        });

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

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void toggleAudioRecording() {
        isRecording = !isRecording;

        if (isRecording) {
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
