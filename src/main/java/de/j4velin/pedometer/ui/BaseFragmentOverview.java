package de.j4velin.pedometer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.eazegraph.lib.models.BarModel;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.j4velin.pedometer.BuildConfig;
import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.SensorListener;
import de.j4velin.pedometer.chart.MyBarChart;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.util.Logger;
import de.j4velin.pedometer.util.SensorFilter;
import de.j4velin.pedometer.util.Util;

import static de.j4velin.pedometer.config.Config.STEPS_CHART_BARS;
import static de.j4velin.pedometer.config.Config.STEPS_CHART_MP_BARS;
import static de.j4velin.pedometer.config.Config.STEPS_CHART_MP_LINE;

/**
 * Created by roma on 30.04.2018.
 */

public abstract class BaseFragmentOverview extends Fragment implements SensorEventListener {

    protected TextView stepsView, totalView, averageView;
    protected int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    protected boolean showSteps = true;

    protected Config config;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        config = ((PedometerApp) getActivity().getApplication()).getConfig();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(config.getName());
    }

    /**
     * Call this method if the Fragment should update the "steps"/"km" text in
     * the pie graph as well as the pie and the bars graphs.
     */
    protected void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
        } else {
            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                    .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }

        updateProgress();
        updateStepsChart();
    }

    protected abstract void updateProgress();

    boolean finishing = false;

    @Override
    public void onResume() {
        super.onResume();
        if (finishing) {
            return;
        }
        if (config.getNavigation().equals(Config.Navigation.MENU.getName())) {
            ((Activity_Main) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        Database db = Database.getInstance(getActivity());

        if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor == null) {
                Sensor as=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if(as!=null)
                {
                    sm.registerListener(this, as, SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    finishing = true;
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                            .setMessage(R.string.no_sensor_explain)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(final DialogInterface dialogInterface) {
                                    getActivity().finish();
                                }
                            }).setNeutralButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                }
            } else {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            }
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        stepsDistanceChanged();
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't happen
    }


    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;

    // change this threshold according to your sensitivity preferences
    private static final float STEP_THRESHOLD = 50f;

    private static final int STEP_DELAY_NS = 250000000;

    private int accelRingCounter = 0;
    private float[] accelRingX = new float[ACCEL_RING_SIZE];
    private float[] accelRingY = new float[ACCEL_RING_SIZE];
    private float[] accelRingZ = new float[ACCEL_RING_SIZE];
    private int velRingCounter = 0;
    private float[] velRing = new float[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

    public void updateAccel(long timeNs, float x, float y, float z) {
        float[] currentAccel = new float[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        // First step is to update our guess of where the global z vector is.
        accelRingCounter++;
        accelRingX[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        accelRingY[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        accelRingZ[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        float[] worldZ = new float[3];
        worldZ[0] = SensorFilter.sum(accelRingX) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = SensorFilter.sum(accelRingY) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = SensorFilter.sum(accelRingZ) / Math.min(accelRingCounter, ACCEL_RING_SIZE);

        float normalization_factor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
        velRingCounter++;
        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = SensorFilter.sum(velRing);

        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
            since_boot++;
            updateProgress();
            lastStepTimeNs = timeNs;
        }
        oldVelocityEstimate = velocityEstimate;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (BuildConfig.DEBUG)
            Logger.log("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        int steps =0;
        final float[] values = event.values;
        final Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
        if(event.sensor.getType()==Sensor.TYPE_STEP_COUNTER) {
            steps = (int) event.values[0];
            if(since_boot < steps) {
                since_boot = steps;
                updateProgress();
            }
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -steps;
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) steps);
            db.close();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (config.getNavigation().equals(Config.Navigation.DRAWER.getName())) {
            inflater.inflate(R.menu.main_menu_drawer, menu);
        } else if (config.getNavigation().equals(Config.Navigation.TABS.getName())) {
            inflater.inflate(R.menu.tabs_menu, menu);
        } else {
            inflater.inflate(R.menu.main, menu);
            if (config.getAchievementList().size() == 0) {
                MenuItem achievements = menu.findItem(R.id.action_achievements);
                achievements.setVisible(false);
            } else {
                Drawable d = getResources().getDrawable(R.drawable.trophy);
                MenuItem achievements = menu.findItem(R.id.action_achievements);
                d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                achievements.setIcon(d);
            }
            config.isAboutEnabled(getActivity(), new AppsgeyserSDK.OnAboutDialogEnableListener() {
                @Override
                public void onDialogEnableReceived(boolean enabled) {
                    menu.findItem(R.id.action_about).setVisible(enabled);
                }
            });
            if (config.getTipsList().size() == 0) {
                menu.findItem(R.id.tips).setVisible(false);
            }
        }
        MenuItem pause = menu.getItem(0);
        Drawable d;
        if (getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                .contains("pauseCount")) { // currently paused
            pause.setTitle(R.string.resume);
            d = getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp);
        } else {
            pause.setTitle(R.string.pause);
            d = getResources().getDrawable(R.drawable.ic_pause_black_24dp);
        }

        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        pause.setIcon(d);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_split_count:
                Dialog_Split.getDialog(getActivity(),
                        total_start + Math.max(todayOffset + since_boot, 0)).show();
                return true;
            case R.id.action_pause:
                SensorManager sm =
                        (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                Drawable d;
                if (getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                        .contains("pauseCount")) { // currently paused -> now resumed

                    Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    if (sensor == null) {
                        Sensor as=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        if(as!=null)
                        {
                            sm.registerListener(this, as, SensorManager.SENSOR_DELAY_FASTEST);
                        } else {
                            finishing = true;
                            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                                    .setMessage(R.string.no_sensor_explain)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(final DialogInterface dialogInterface) {
                                            getActivity().finish();
                                        }
                                    }).setNeutralButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create().show();
                        }
                    } else {
                        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
                    }
                    item.setTitle(R.string.pause);
                    d = getResources().getDrawable(R.drawable.ic_pause_black_24dp);
                } else {
                    sm.unregisterListener(this);
                    item.setTitle(R.string.resume);
                    d = getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp);
                }
                d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                item.setIcon(d);
                getActivity().startService(new Intent(getActivity(), SensorListener.class)
                        .putExtra("action", SensorListener.ACTION_PAUSE));
                return true;
            default:
                return ((Activity_Main) getActivity()).optionsItemSelected(item);
        }
    }

    protected void updateStepsChart() {
        if (config.getStepsChart().equals(STEPS_CHART_MP_LINE)) {
            updateMpLine();
        } else if (config.getStepsChart().equals(STEPS_CHART_BARS)) {
            updateBars();
        } else if (config.getStepsChart().equals(STEPS_CHART_MP_BARS)) {
            updateMpBars();
        }
    }

    protected void updateMpLine() {
        final SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        LineChart valueLineChart = (LineChart) getView().findViewById(R.id.linechart);
        valueLineChart.setVisibility(View.VISIBLE);
        valueLineChart.clear();
        valueLineChart.getLegend().setTextColor(config.getTextColor());
        valueLineChart.getXAxis().setTextColor(config.getTextColor());
        valueLineChart.getAxisLeft().setTextColor(config.getTextColor());
        int steps;
        float distance, stepsize = Fragment_Settings.DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!showSteps) {
            // load some more settings if distance is needed
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        Entry bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        while (last.size() < 8) {
            if (last.size() > 0) {
                last.add(new Pair<Long, Integer>(last.get(last.size() - 1).first - 24 * 60 * 60 * 1000, 0));
            } else {
                last.add(new Pair<Long, Integer>(System.currentTimeMillis(), 0));
            }
        }
        db.close();

        List<Entry> entries = new ArrayList<Entry>();

        List<Entry> entriesGoal = new ArrayList<Entry>();

        final Map<Integer, Date> integerDateMap = new HashMap<>();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            int x = last.size() - i;
            if (steps < 0) {
                steps = 0;
            }
            if (showSteps) {
                bm = new Entry(x, steps);
            } else {
                distance = steps * stepsize;
                if (stepsize_cm) {
                    distance /= 100000;
                } else {
                    distance /= 5280;
                }
                distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                bm = new Entry(x, distance);
            }
            integerDateMap.put(x, new Date(current.first));
            entries.add(bm);
        }
        if (showSteps) {
            entriesGoal.add(new Entry(1, goal));
            entriesGoal.add(new Entry(last.size() - 1, goal));
        } else {
            float distanceGoal = goal * stepsize;
            if (stepsize_cm) {
                distanceGoal /= 100000;
            } else {
                distanceGoal /= 5280;
            }
            distanceGoal = Math.round(distanceGoal * 1000) / 1000f; // 3 decimals
            entriesGoal.add(new Entry(1, distanceGoal));
            entriesGoal.add(new Entry(last.size() - 1, distanceGoal));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.steps)); // add entries to dataset
        dataSet.setColor(config.getStepsChartColorSteps());
        dataSet.setValueTextColor(config.getTextColor());

        LineDataSet goalDataSet = new LineDataSet(entriesGoal, getString(R.string.goal)); // add entries to dataset
        goalDataSet.setColor(config.getStepsChartColorGoal());
        goalDataSet.setValueTextColor(config.getTextColor());

        LineData lineData = new LineData(dataSet, goalDataSet);
        valueLineChart.setData(lineData);

        valueLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = integerDateMap.get((int) value);
                if (date == null) {
                    return "";
                }
                return df.format(date);
            }
        });
        valueLineChart.invalidate(); // refresh
        valueLineChart.getLegend().setWordWrapEnabled(true);
        valueLineChart.getLegend().setTextSize(10);
        valueLineChart.getAxisRight().setEnabled(false);
        valueLineChart.getDescription().setEnabled(false);
        valueLineChart.setNoDataText(getActivity().getString(R.string.noChartData));
    }

    protected void updateMpBars() {
        final SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        com.github.mikephil.charting.charts.BarChart valueLineChart = (com.github.mikephil.charting.charts.BarChart) getView().findViewById(R.id.barchart);
        valueLineChart.setVisibility(View.VISIBLE);
        valueLineChart.clear();
        int steps;
        float distance, stepsize = Fragment_Settings.DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!showSteps) {
            // load some more settings if distance is needed
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        BarEntry bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        while (last.size() < 8) {
            if (last.size() > 0) {
                last.add(new Pair<Long, Integer>(last.get(last.size() - 1).first - 24 * 60 * 60 * 1000, 0));
            } else {
                last.add(new Pair<Long, Integer>(System.currentTimeMillis(), 0));
            }
        }
        db.close();

        List<BarEntry> entries = new ArrayList<BarEntry>();

        List<BarEntry> entriesGoal = new ArrayList<BarEntry>();

        final Map<Integer, Date> integerDateMap = new HashMap<>();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            int x = last.size() - i;
            if (steps < 0) {
                steps = 0;
            }
            if (showSteps) {
                bm = new BarEntry(x, steps);
            } else {
                distance = steps * stepsize;
                if (stepsize_cm) {
                    distance /= 100000;
                } else {
                    distance /= 5280;
                }
                distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                bm = new BarEntry(x, distance);
            }
            integerDateMap.put(x, new Date(current.first));
            entries.add(bm);
        }
        if (showSteps) {
            entriesGoal.add(new BarEntry(1, goal));
            entriesGoal.add(new BarEntry(last.size() - 1, goal));
        } else {
            float distanceGoal = goal * stepsize;
            if (stepsize_cm) {
                distanceGoal /= 100000;
            } else {
                distanceGoal /= 5280;
            }
            distanceGoal = Math.round(distanceGoal * 1000) / 1000f; // 3 decimals
            entriesGoal.add(new BarEntry(1, distanceGoal));
            entriesGoal.add(new BarEntry(last.size() - 1, distanceGoal));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.steps)); // add entries to dataset
        dataSet.setColor(config.getStepsChartColorSteps());
        dataSet.setValueTextColor(config.getTextColor());
//        LineDataSet goalDataSet = new LineDataSet(entriesGoal, "Goal"); // add entries to dataset
//        goalDataSet.setColor(Color.parseColor("#0000AA"));

        BarData barData = new BarData(dataSet);
        valueLineChart.setData(barData);

        valueLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = integerDateMap.get((int) value);
                if (date == null) {
                    return "";
                }
                return df.format(date);
            }
        });
        valueLineChart.getXAxis().setTextColor(config.getTextColor());
        valueLineChart.getAxisLeft().setTextColor(config.getTextColor());
        valueLineChart.getLegend().setTextColor(config.getTextColor());

        valueLineChart.invalidate(); // refresh
        valueLineChart.getLegend().setWordWrapEnabled(true);
        valueLineChart.getLegend().setTextSize(10);
        valueLineChart.getAxisRight().setEnabled(false);
        valueLineChart.getDescription().setEnabled(false);
        valueLineChart.setNoDataText(getActivity().getString(R.string.noChartData));
    }

    /**
     * Updates the bar graph to show the steps/distance of the last week. Should
     * be called when switching from step count to distance.
     */
    protected void updateBars() {
        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        MyBarChart barChart = (MyBarChart) getView().findViewById(R.id.bargraph);
        barChart.setVisibility(View.VISIBLE);
        barChart.setTextColor(config.getTextColor());

        if (barChart.getData().size() > 0) barChart.clearChart();
        int steps;
        float distance, stepsize = Fragment_Settings.DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!showSteps) {
            // load some more settings if distance is needed
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        barChart.setShowDecimal(!showSteps); // show decimal in distance view only
        BarModel bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        while (last.size() < 8 && last.size() > 0) {
            last.add(new Pair<Long, Integer>(last.get(last.size() - 1).first - 24 * 60 * 60 * 1000, 0));
        }
        db.close();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            if (steps < 0) {
                steps = 0;
            }
            bm = new BarModel(df.format(new Date(current.first)), 0,
                    steps > goal ? config.getStepsChartColorSteps() : config.getStepsChartColorGoal());
            if (showSteps) {
                bm.setValue(steps);
            } else {
                distance = steps * stepsize;
                if (stepsize_cm) {
                    distance /= 100000;
                } else {
                    distance /= 5280;
                }
                distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                bm.setValue(distance);
            }
            barChart.addBar(bm);
        }
        if (barChart.getData().size() > 0) {
            /*barChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Dialog_Statistics.getDialog(getActivity(), since_boot).show();
                }
            });*/
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }
    }

}
