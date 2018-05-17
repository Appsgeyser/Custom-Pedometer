package de.j4velin.pedometer.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.List;

import de.j4velin.pedometer.BuildConfig;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.util.AchievementService;
import de.j4velin.pedometer.util.Logger;

/**
 * Created by roma on 01.05.2018.
 */

public abstract class BasePieFragmentOverview extends BaseFragmentOverview{

    private PieModel sliceGoal, sliceCurrent;
    private PieChart pg;

    protected void updateStepsCount(float steps_today){
        stepsView.setText(formatter.format(steps_today));
    }

    protected void createPie(View view){
        pg = (PieChart) view.findViewById(R.id.graph);

        // slice for the steps taken today
        sliceCurrent = new PieModel("", 0, config.getPieStepsColor());
        pg.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        sliceGoal = new PieModel("", Fragment_Settings.DEFAULT_GOAL, config.getPieGoalColor());
        pg.addPieSlice(sliceGoal);

        pg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });
        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();
        if (config.getProgressWidth().equals(Config.PROGRESS_THIN)){
            pg.setInnerPadding(90);
        }
    }

    /**
     * Updates the pie graph to show todays steps/distance as well as the
     * yesterday and total values. Should be called when switching from step
     * count to distance.
     */
    protected void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        pg.setVisibility(View.VISIBLE);

        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(steps_today);
        if (goal - steps_today > 0) {
            // goal not reached yet
            if (pg.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal - steps_today);
        } else {
            // goal reached
            pg.clearChart();
            pg.addPieSlice(sliceCurrent);
        }
        pg.update();
        if (showSteps) {
            updateStepsCount(steps_today);
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
            updateStepsCount(distance_today);
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
        AchievementService.checkAchievements(getActivity(), steps_today, total_start + steps_today);

    }

    private void updateMpPie(){
        com.github.mikephil.charting.charts.PieChart mpPieChart = (com.github.mikephil.charting.charts.PieChart) getView().findViewById(R.id.mpPieChart);
        mpPieChart.setVisibility(View.VISIBLE);
        mpPieChart.clear();

        List<PieEntry> pieEntries = new ArrayList<>();

        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);


        if (goal - steps_today > 0) {
            pieEntries.add(new PieEntry(steps_today, ""));
            pieEntries.add(new PieEntry(goal - steps_today, ""));
        } else {
            pieEntries.add(new PieEntry(steps_today, ""));
        }
        int colors[] = {config.getPieStepsColor(), config.getPieGoalColor()};
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        //pieDataSet.setDrawValues(false);
        PieData pieData = new PieData(pieDataSet);
        mpPieChart.setData(pieData);
        mpPieChart.setDescription(new Description());
        mpPieChart.getLegend().setEnabled(false);
        mpPieChart.getDescription().setEnabled(false);
        if(config.getProgressWidth().equals(Config.PROGRESS_THICK)) {
            mpPieChart.setHoleRadius(80);
        }else if (config.getProgressWidth().equals(Config.PROGRESS_THIN)){
            mpPieChart.setHoleRadius(90);
        }
        mpPieChart.setHoleColor(Color.parseColor("#00ffffff"));


        if (showSteps) {
            updateStepsCount(steps_today);
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
            updateStepsCount(distance_today);
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
        AchievementService.checkAchievements(getActivity(), steps_today, total_start + steps_today);

    }


    @Override
    protected void updateProgress() {
        if(config.getPieChart().equals(Config.PIE_CHART_MP_PIE)){
            updateMpPie();
        }else if(config.getPieChart().equals(Config.PIE_CHART_BASIC)){
            updatePie();
        }

    }

}
