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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import de.j4velin.pedometer.Database;
import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.PowerReceiver;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.SensorListener;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.util.API23Wrapper;
import de.j4velin.pedometer.util.PlaySettingsWrapper;

public class Fragment_Settings extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    final static int DEFAULT_GOAL = 10000;
    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";
    private Config config;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*addPreferencesFromResource(R.xml.settings);
        findPreference("import").setOnPreferenceClickListener(this);
        findPreference("export").setOnPreferenceClickListener(this);

        findPreference("notification")
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference,
                                                      final Object newValue) {
                        getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                                .putBoolean("notification", (Boolean) newValue).commit();

                        getActivity().startService(new Intent(getActivity(), SensorListener.class)
                                .putExtra(SensorListener.ACTION_UPDATE_NOTIFICATION, true));
                        return true;
                    }
                });

        findPreference("pause_on_power")
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference,
                                                      final Object newValue) {
                        getActivity().getPackageManager().setComponentEnabledSetting(
                                new ComponentName(getActivity(), PowerReceiver.class),
                                ((Boolean) newValue) ?
                                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                        return true;
                    }
                });

        Preference account = findPreference("account");
        PlaySettingsWrapper
                .setupAccountSetting(account, savedInstanceState, (Activity_Main) getActivity());

        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        Preference goal = findPreference("goal");
        goal.setOnPreferenceClickListener(this);
        goal.setSummary(getString(R.string.goal_summary, prefs.getInt("goal", DEFAULT_GOAL)));

        Preference stepsize = findPreference("stepsize");
        stepsize.setOnPreferenceClickListener(this);
        stepsize.setSummary(getString(R.string.step_size_summary,
                prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE),
                prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)));
*/
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        config = ((PedometerApp) getActivity().getApplication()).getConfig();


        MyCheckboxPreference notification = (MyCheckboxPreference) findPreference("notification");
        notification
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference,
                                                      final Object newValue) {
                        getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                                .putBoolean("notification", (Boolean) newValue).commit();

                        getActivity().startService(new Intent(getActivity(), SensorListener.class)
                                .putExtra(SensorListener.ACTION_UPDATE_NOTIFICATION, true));
                        return true;
                    }
                });
        notification.setTextColor(config.getTextColor());
        notification.setCheckboxColor(config.getAccentColor());

        MyCheckboxPreference pauseOnPower = (MyCheckboxPreference) findPreference("pause_on_power");
        pauseOnPower
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference preference,
                                                      final Object newValue) {
                        getActivity().getPackageManager().setComponentEnabledSetting(
                                new ComponentName(getActivity(), PowerReceiver.class),
                                ((Boolean) newValue) ?
                                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                        return true;
                    }
                });
        pauseOnPower.setTextColor(config.getTextColor());
        pauseOnPower.setCheckboxColor(config.getAccentColor());

        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        MyPreference goal = (MyPreference) findPreference("goal");
        goal.setOnPreferenceClickListener(this);
        goal.setSummary(getString(R.string.goal_summary, prefs.getInt("goal", DEFAULT_GOAL)));
        goal.setTextColor(config.getTextColor());

        MyPreference stepsize = (MyPreference) findPreference("stepsize");
        stepsize.setOnPreferenceClickListener(this);
        stepsize.setSummary(getString(R.string.step_size_summary,
                prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE),
                prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)));
        stepsize.setTextColor(config.getTextColor());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings);

        MyPreference license = (MyPreference) findPreference("license");
        license.setOnPreferenceClickListener(this);
        license.setTextColor(config.getTextColor());
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        PlaySettingsWrapper.onSavedInstance(outState, (Activity_Main) getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(config.getNavigation().equals(Config.Navigation.MENU.getName())) {
            ((Activity_Main) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (config.getNavigation().equals(Config.Navigation.DRAWER.getName())) {
            inflater.inflate(R.menu.main_menu_drawer, menu);
        } else if (config.getNavigation().equals(Config.Navigation.TABS.getName())) {
            inflater.inflate(R.menu.tabs_menu, menu);
        } else {
            inflater.inflate(R.menu.main, menu);
            if(config.getAchievementList().size() == 0) {
                MenuItem achievements = menu.findItem(R.id.action_achievements);
                achievements.setVisible(false);
            }else  {
                Drawable d = getResources().getDrawable(R.drawable.trophy);
                MenuItem achievements = menu.findItem(R.id.action_achievements);
                d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                achievements.setIcon(d);
            }
        }

    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(config.getNavigation().equals(Config.Navigation.MENU.getName())){
            menu.findItem(R.id.action_settings).setVisible(false);
            menu.findItem(R.id.action_split_count).setVisible(false);
            config.isAboutEnabled(getActivity(), new AppsgeyserSDK.OnAboutDialogEnableListener() {
                @Override
                public void onDialogEnableReceived(boolean enabled) {
                    menu.findItem(R.id.action_about).setVisible(enabled);
                }
            });
            if(config.getTipsList().size() == 0) {
                menu.findItem(R.id.tips).setVisible(false);
            }
        }
        if(config.getNavigation().equals(Config.Navigation.TABS.getName())){
            menu.findItem(R.id.action_split_count).setVisible(false);
        }
        menu.findItem(R.id.action_pause).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return ((Activity_Main) getActivity()).optionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        AlertDialog.Builder builder;
        View v;
        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        switch (preference.getKey()) {
            case "goal":
                builder = new AlertDialog.Builder(getActivity());
                final NumberPicker np = new NumberPicker(getActivity());
                np.setMinValue(1);
                np.setMaxValue(100000);
                np.setValue(prefs.getInt("goal", 10000));
                builder.setView(np);
                builder.setTitle(R.string.set_goal);
                builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        np.clearFocus();
                        prefs.edit().putInt("goal", np.getValue()).commit();
                        preference.setSummary(getString(R.string.goal_summary, np.getValue()));
                        dialog.dismiss();
                        getActivity().startService(new Intent(getActivity(), SensorListener.class)
                                .putExtra("updateNotificationState", true));
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
                break;
            case  "stepsize":
                builder = new AlertDialog.Builder(getActivity());
                v = getActivity().getLayoutInflater().inflate(R.layout.stepsize, null);
                final RadioGroup unit = (RadioGroup) v.findViewById(R.id.unit);
                final EditText value = (EditText) v.findViewById(R.id.value);
                unit.check(
                        prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm :
                                R.id.ft);
                value.setText(String.valueOf(prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE)));
                builder.setView(v);
                builder.setTitle(R.string.set_step_size);
                builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            prefs.edit().putFloat("stepsize_value",
                                    Float.valueOf(value.getText().toString()))
                                    .putString("stepsize_unit",
                                            unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft")
                                    .apply();
                            preference.setSummary(getString(R.string.step_size_summary,
                                    Float.valueOf(value.getText().toString()),
                                    unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft"));
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case "license":
                AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(getActivity());
                aboutBuilder.setTitle(R.string.about);
                TextView tv = new TextView(getActivity());
                tv.setPadding(10, 10, 10, 10);
                tv.setText(R.string.about_text_links);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                aboutBuilder.setView(tv);
                aboutBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                aboutBuilder.create().show();
        }
        return false;
    }

    private boolean hasWriteExternalPermission() {
        return getActivity().getPackageManager()
                .checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getActivity().getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Creates the CSV file containing data about past days and the steps taken on them
     * <p/>
     * Requires external storage to be writeable
     */
    private void exportCsv() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File f = new File(Environment.getExternalStorageDirectory(), "Pedometer.csv");
            if (f.exists()) {
                new AlertDialog.Builder(getActivity()).setMessage(R.string.file_already_exists)
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                writeToFile(f);
                            }
                        }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            } else {
                writeToFile(f);
            }
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.error_external_storage_not_available)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    /**
     * Imports previously exported data from a csv file
     * <p/>
     * Requires external storage to be readable. Overwrites days for which there is already an entry in the database
     */
    private void importCsv() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File f = new File(Environment.getExternalStorageDirectory(), "Pedometer.csv");
            if (!f.exists() || !f.canRead()) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.file_cant_read, f.getAbsolutePath()))
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return;
            }
            Database db = Database.getInstance(getActivity());
            String line;
            String[] data;
            int ignored = 0, inserted = 0, overwritten = 0;
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(f));
                while ((line = in.readLine()) != null) {
                    data = line.split(";");
                    try {
                        if (db.insertDayFromBackup(Long.valueOf(data[0]),
                                Integer.valueOf(data[1]))) {
                            inserted++;
                        } else {
                            overwritten++;
                        }
                    } catch (Exception nfe) {
                        ignored++;
                    }
                }
                in.close();
            } catch (IOException e) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.error_file, e.getMessage()))
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                e.printStackTrace();
                return;
            } finally {
                db.close();
            }
            String message = getString(R.string.entries_imported, inserted + overwritten);
            if (overwritten > 0)
                message += "\n\n" + getString(R.string.entries_overwritten, overwritten);
            if (ignored > 0) message += "\n\n" + getString(R.string.entries_ignored, ignored);
            new AlertDialog.Builder(getActivity()).setMessage(message)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.error_external_storage_not_available)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    private void writeToFile(final File f) {
        BufferedWriter out;
        try {
            f.createNewFile();
            out = new BufferedWriter(new FileWriter(f));
        } catch (IOException e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.error_file, e.getMessage()))
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            e.printStackTrace();
            return;
        }
        Database db = Database.getInstance(getActivity());
        Cursor c =
                db.query(new String[]{"date", "steps"}, "date > 0", null, null, null, "date", null);
        try {
            if (c != null && c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    out.append(c.getString(0)).append(";")
                            .append(String.valueOf(Math.max(0, c.getInt(1)))).append("\n");
                    c.moveToNext();
                }
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.error_file, e.getMessage()))
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            e.printStackTrace();
            return;
        } finally {
            if (c != null) c.close();
            db.close();
        }
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.data_saved, f.getAbsolutePath()))
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }
}
