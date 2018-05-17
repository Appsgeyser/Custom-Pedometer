package de.j4velin.pedometer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.config.Achievement;
import de.j4velin.pedometer.config.Config;

/**
 * Created by roma on 03.05.2018.
 */

public class AchievementService {

    public static void checkAchievements(final Activity context, int stepsToday, int stepsTotal) {

        Config config = ((PedometerApp) context.getApplication()).getConfig();


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("Achievement", "total: "+stepsTotal);
        for (Achievement achievement: config.getAchievementList()){
            if(prefs.getLong("achievement_"+achievement.getName().hashCode(), 0) == 0){
                if(achievement.getType().equals(Achievement.Type.stepsDaily)){
                    if (stepsToday >= achievement.getValue()) {
                        unlockAchievement(context, achievement);
                        prefs.edit().putLong("achievement_"+achievement.getName().hashCode(), System.currentTimeMillis()).apply();
                    }
                }else if(achievement.getType().equals(Achievement.Type.stepsTotal)){
                    if (stepsTotal > achievement.getValue()) {
                        unlockAchievement(context, achievement);
                        prefs.edit().putLong("achievement_"+achievement.getName().hashCode(), System.currentTimeMillis()).apply();
                    }
                }
            }
        }
    }


    private static void unlockAchievement(Activity context, Achievement achievement) {
        Config config = ((PedometerApp) context.getApplication()).getConfig();
        LayoutInflater toastInflanter = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = toastInflanter.inflate(R.layout.achievement_toast, (ViewGroup)
                null);
        ImageView imageView = (ImageView) layout.findViewById(R.id.image);
        imageView.setImageDrawable(config.createDrawable(context, achievement.getImageUrl()));
        Toast toast = new Toast(context.getApplicationContext());

        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.show();
    }
}
