package de.j4velin.pedometer;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import de.j4velin.pedometer.config.Config;

/**
 * Created by roma on 25.04.2018.
 */

public class PedometerApp extends Application {

    private Config config;

    public Config getConfig() {
        return config;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        config = new Config(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
