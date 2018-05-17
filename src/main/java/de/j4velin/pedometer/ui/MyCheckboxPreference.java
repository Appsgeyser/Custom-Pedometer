package de.j4velin.pedometer.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by roma on 02.05.2018.
 */

public class MyCheckboxPreference extends CheckBoxPreference {
    private int textColor = Color.parseColor("#000000");
    private int checkboxColor = Color.parseColor("#000000");

    public MyCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MyCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCheckboxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(isEnabled() ? textColor : Color.parseColor("#999999"));

        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        summaryView.setTextColor(isEnabled() ? textColor : Color.parseColor("#999999"));

        CheckBox checkBox = (CheckBox) holder.findViewById(android.R.id.checkbox);

        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {isEnabled() ? checkboxColor : Color.parseColor("#999999"), isEnabled() ? textColor : Color.parseColor("#999999")};
        CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
    }

    public void setTextColor(int color){
        textColor = color;
    }

    public void setCheckboxColor(int checkboxColor) {
        this.checkboxColor = checkboxColor;
    }
}
