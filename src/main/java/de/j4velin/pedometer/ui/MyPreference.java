package de.j4velin.pedometer.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import de.j4velin.pedometer.R;

/**
 * Created by roma on 02.05.2018.
 */

public class MyPreference extends Preference {
    private int textColor = Color.parseColor("#000000");

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(isEnabled() ? textColor : Color.parseColor("#999999"));

        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        summaryView.setTextColor(isEnabled() ? textColor : Color.parseColor("#999999"));
    }

    public void setTextColor(int color){
        textColor = color;
    }
}
