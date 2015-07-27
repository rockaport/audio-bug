package com.asesolutions.mobile.voltaaudio.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

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

public class MainActivity extends AppCompatActivity {

    public static final int CHART_UPDATE_PERIOD = 100;
    private Toolbar toolBar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private LineChart audioLevelChart;
    private Handler handler;
    private LineData lineData;
    private Random randomeGen;
    private LineDataSet lineDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Setup a drawer toggle for open/close events and the menu icon
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolBar,
                R.string.hello_world, R.string.hello_world
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        // Initialize the navigation view selection listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                // Swap views/fragments
                return true;
            }
        });

        // Initialize a RNG
        randomeGen = new Random();

        // Obtain a reference to the chart and initialize various things (data, axis, etc.)
        audioLevelChart = (LineChart) findViewById(R.id.chart);

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
        yAxis.setAxisMinValue(0);
        yAxis.setAxisMaxValue(1);
        yAxis.setLabelCount(6, true);

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

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new AudioLevelUpdater(), CHART_UPDATE_PERIOD);
    }

    private class AudioLevelUpdater implements Runnable {
        @Override
        public void run() {
            float val = randomeGen.nextFloat();
            if (lineDataSet.getEntryCount() > 0) {
                val = 0.5f * (val + lineDataSet.getEntryForXIndex(lineDataSet.getEntryCount()).getVal());
            }
            lineDataSet.addEntry(new Entry(val, lineDataSet.getEntryCount()));
            lineData.addXValue(String.valueOf(lineDataSet.getEntryCount()));
            audioLevelChart.notifyDataSetChanged();
            audioLevelChart.setVisibleXRangeMaximum(30);
            audioLevelChart.moveViewToX(Math.max(lineData.getXValCount() - 30, 0));
            handler.postDelayed(this, CHART_UPDATE_PERIOD);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
