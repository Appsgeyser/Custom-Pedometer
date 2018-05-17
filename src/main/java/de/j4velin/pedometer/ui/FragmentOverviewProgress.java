/*
 * Copyright 2014 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.j4velin.pedometer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.j4velin.pedometer.BuildConfig;
import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.SensorListener;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.util.AchievementService;
import de.j4velin.pedometer.util.Logger;
import de.j4velin.pedometer.util.Util;

public class FragmentOverviewProgress extends BaseFragmentOverview  {


    private ProgressBar progressBar;

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_overview_progress, null);
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalView = (TextView) v.findViewById(R.id.total);
        averageView = (TextView) v.findViewById(R.id.average);
        TextView dataNotice = (TextView) v.findViewById(R.id.dataNotice);
        dataNotice.setTextColor(config.getTextColor());
        stepsView.setTextColor(config.getTextColor());
        totalView.setTextColor(config.getTextColor());
        averageView.setTextColor(config.getTextColor());
        ((TextView)v.findViewById(R.id.averageLabel)).setTextColor(config.getTextColor());
        ((TextView)v.findViewById(R.id.totalLabel)).setTextColor(config.getTextColor());
        ((TextView)v.findViewById(R.id.unit)).setTextColor(config.getTextColor());

        progressBar = v.findViewById(R.id.progress);
        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        progressBar.setMax(goal);
        progressBar.setProgress(0);
        if (config.getProgressWidth().equals(Config.PROGRESS_THIN)){
            progressBar.setScaleY(0.5f);
        }

        LayerDrawable layerDrawable = (LayerDrawable) getResources()
                .getDrawable(R.drawable.progress_theme_0);
        GradientDrawable progressBackground = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(android.R.id.background);
        progressBackground.setColor(config.getPieGoalColor());

        ClipDrawable progressSteps = (ClipDrawable) layerDrawable
                .findDrawableByLayerId(android.R.id.progress);
        progressSteps.setColorFilter(config.getPieStepsColor(), PorterDuff.Mode.SRC_IN);

        progressBar.setProgressDrawable(layerDrawable);

        OnClickListener changeUnitClickListener = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        };

        progressBar.setOnClickListener(changeUnitClickListener);
        stepsView.setOnClickListener(changeUnitClickListener);
        ((Activity_Main) getActivity()).showFullscreen();

        return v;
    }


    /**
     * Updates the pie graph to show todays steps/distance as well as the
     * yesterday and total values. Should be called when switching from step
     * count to distance.
     */
    protected void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        if (goal - steps_today > 0) {
            progressBar.setProgress(steps_today);
        } else {
            progressBar.setProgress(steps_today);
        }
        if (showSteps) {
            stepsView.setText(formatter.format(steps_today));
            totalView.setText(formatter.format(total_start + steps_today));
            averageView.setText(formatter.format((total_start + steps_today) / total_days));
        } else {
            // update only every 10 steps when displaying distance
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            float stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            float distance_today = steps_today * stepsize;
            float distance_total = (total_start + steps_today) * stepsize;
            if (prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
                distance_total /= 100000;
            } else {
                distance_today /= 5280;
                distance_total /= 5280;
            }
            stepsView.setText(formatter.format(distance_today));
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
        AchievementService.checkAchievements(getActivity(), steps_today, total_start + steps_today);
    }

    @Override
    protected void updateProgress() {
        updatePie();
    }
}
