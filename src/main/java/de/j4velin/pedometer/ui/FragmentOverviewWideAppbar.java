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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.j4velin.pedometer.BuildConfig;
import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.util.AchievementService;
import de.j4velin.pedometer.util.Logger;

public class FragmentOverviewWideAppbar extends BasePieFragmentOverview  {


    private TextView stepsGraphView;

    private LinearLayout topPanel;

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_overview_wide_appbar, null);
        stepsView = (TextView) v.findViewById(R.id.steps);
        stepsGraphView = (TextView) v.findViewById(R.id.steps_graph);
        totalView = (TextView) v.findViewById(R.id.total);
        averageView = (TextView) v.findViewById(R.id.average);
        TextView dataNotice = (TextView) v.findViewById(R.id.dataNotice);
        dataNotice.setTextColor(config.getTextColor());
        stepsGraphView.setTextColor(config.getTextColor());
        ((TextView)v.findViewById(R.id.unit_graph)).setTextColor(config.getTextColor());


        topPanel = v.findViewById(R.id.topPanel);

        topPanel.setBackgroundColor(config.getPrimaryColor());


        stepsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });
        stepsGraphView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });
        createPie(v);
        ((Activity_Main) getActivity()).showFullscreen();

        return v;
    }

    @Override
    protected void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
            ((TextView) getView().findViewById(R.id.unit_graph)).setText(getString(R.string.steps));
        } else {
            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                    .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
            ((TextView) getView().findViewById(R.id.unit_graph)).setText(unit);
        }

        updateProgress();
        updateStepsChart();
    }

    @Override
    protected void updateStepsCount(float steps_today) {
        stepsView.setText(formatter.format(steps_today));
        stepsGraphView.setText(formatter.format(steps_today));
    }
}
