package de.j4velin.pedometer.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.appsgeyser.sdk.AppsgeyserSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.j4velin.pedometer.R;


/**
 * Created by roma on 27.04.2017.
 */

public class Config {
    public static final String CONFIG_PREFERENCES = "ConfigPref";

    public static String STEPS_CHART_MP_LINE = "mpLineChart";
    public static String STEPS_CHART_BARS = "bars";
    public static String STEPS_CHART_MP_BARS = "mpBars";

    public static String PIE_CHART_MP_PIE = "mpPie";
    public static String PIE_CHART_BASIC = "basicPie";

    public static String LAYOUT_BASIC = "basicLayout";
    public static String LAYOUT_PROGRESS = "progressLayout";
    public static String LAYOUT_APPBAR = "appbarLayout";

    public static String PROGRESS_THIN = "thin";
    public static String PROGRESS_THICK = "thick";

    private LruCache<String, Bitmap> mMemoryCache;

    private Boolean aboutEnabled;

    private String name;

    public enum Navigation {

        DRAWER("drawer"),
        MENU("menu"),
        TABS("tabs");

        private String name;

        Navigation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private void manageCache() {
        Log.w("cache", "init");
        mMemoryCache = new LruCache<String, Bitmap>(20 * 1000 * 1000) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    private boolean useBackgroundImage;
    private Drawable backgroundImage;
    private Drawable achievementLockedImage;
    private Drawable drawerBackgroundImage;
    private Drawable drawerIcon;
    private Integer backgroundColor;
    private String navigation;
    private String layout;
    private String progressWidth;

    private String stepsChart;
    private String pieChart;

    private Integer primaryDarkColor;
    private Integer primaryColor;
    private Integer accentColor;
    private Integer textColor;
    private Integer pieStepsColor;
    private Integer pieGoalColor;

    private Integer stepsChartColorSteps;
    private Integer stepsChartColorGoal;

    List<Achievement> achievementList;
    List<Tips> tipsList;

    private Context context;

    public Config(Context context) {
        this.context = context;
        manageCache();
        try {
            JSONObject settings = new JSONObject(loadSettings(context));
            achievementLockedImage = createDrawable(context, settings.getString("achievementLockedImage"));

            drawerBackgroundImage = createDrawable(context, settings.getString("drawerBackgroundImage"));
            drawerIcon = createDrawable(context, settings.getString("drawerIcon"));

            JSONObject background = settings.getJSONObject("background");
            useBackgroundImage = background.getString("background").equals("backgroundImage");
            backgroundImage = createDrawable(context, background.getString("backgroundImage"));
            backgroundColor = readColor(background, "backgroundColor");

            textColor = readColor(settings, "textColor");

            JSONObject themeColors = settings.getJSONObject("themeColors");
            primaryDarkColor = readColor(themeColors, "colorPrimaryDark");
            primaryColor = readColor(themeColors, "colorPrimary");
            accentColor = readColor(themeColors, "colorAccent");

            navigation = settings.getJSONObject("navigationRandom").getString("navigation");
            layout = settings.getJSONObject("layoutRandom").getString("layout");

            JSONObject stepsChartRandom = settings.getJSONObject("stepsChartRandom");
            stepsChart = stepsChartRandom.getString("stepsChart");
            stepsChartColorSteps = readColor(stepsChartRandom, "stepsChartColorSteps");
            stepsChartColorGoal = readColor(stepsChartRandom, "stepsChartColorGoal");

            JSONObject pieChartRandom = settings.getJSONObject("pieChartRandom");
            pieChart = pieChartRandom.getString("pieChart");
            pieGoalColor = readColor(pieChartRandom, "pieGoalColor");
            pieStepsColor = readColor(pieChartRandom, "pieStepsColor");

            name = settings.getString("name");

            progressWidth = settings.getJSONObject("progressWidthRandom").getString("progressWidth");

            achievementList = new ArrayList<>();
            JSONArray achievementsArray = settings.getJSONArray("achievements");
            for(int i =0; i< achievementsArray.length(); i++){
                achievementList.add(readAchievement(achievementsArray.getJSONObject(i)));
            }

            tipsList = new ArrayList<>();
            JSONArray tipsArray = settings.getJSONArray("usefulTips");
            for(int i =0; i< tipsArray.length(); i++){
                tipsList.add(readTip(tipsArray.getJSONObject(i)));
            }

        } catch (JSONException e) {
            Log.e("Config", "Json parse error: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Config", "Json read error: " + e.getMessage());
        }
    }

    //------------------------------------------------------

    private Achievement readAchievement(JSONObject jsonAchievement) throws JSONException {
        Achievement achievement = new Achievement();
        achievement.setName(jsonAchievement.getString("achievementName"));
        achievement.setDescription(jsonAchievement.getString("achievementDescription"));
        achievement.setImageUrl(jsonAchievement.getString("achievementImage"));
        achievement.setType(Achievement.Type.valueOf(jsonAchievement.getString("stepsType")));
        achievement.setValue(jsonAchievement.getInt("achievementValue"));
        return achievement;
    }

    private Tips readTip(JSONObject jsonTip) throws JSONException {
        return new Tips(jsonTip.getString("tipsName"), jsonTip.getString("tipsUrl"));
    }

    public Drawable createDrawable(Context context, String link) {
        if (!link.equals("")) {
            try {
                Bitmap b = mMemoryCache.get(link);
                if (b == null) {
                    b = BitmapFactory.decodeStream(context.getAssets().open(link));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    mMemoryCache.put(link, b);
                }
                return new BitmapDrawable(context.getResources(), b);
            } catch (FileNotFoundException e) {
                Log.d("Config", "Image " + link + " not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Bitmap readBitmap(Context context, String link) {
        if (!link.equals("")) {
            try {
                Bitmap b = mMemoryCache.get(link);
                if (b == null) {
                    b = BitmapFactory.decodeStream(context.getAssets().open(link));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    mMemoryCache.put(link, b);
                }
                return b;
            } catch (FileNotFoundException e) {
                Log.d("Config", "Image " + link + " not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Integer readColor(JSONObject jsonTheme, String name) throws JSONException {
        String color = jsonTheme.getString(name);
        if (color == null || color.equals("")) {
            return null;
        }
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        return Color.parseColor(color);
    }


    public String loadSettings(Context context) throws IOException {
        String json = null;
        try {
            InputStream is = context.getAssets().open("settings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    //------------------------------------------------------

    public String getName() {
        return name;
    }

    public List<Achievement> getAchievementList() {
        return achievementList;
    }

    public String getProgressWidth() {
        return progressWidth;
    }

    public String getLayout() {
        return layout;
    }

    public String getPieChart() {
        return pieChart;
    }

    public boolean isUseBackgroundImage() {
        return useBackgroundImage;
    }

    public Drawable getBackgroundImage() {
        return backgroundImage;
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    public Drawable getDrawerBackgroundImage() {
        return drawerBackgroundImage;
    }

    public Drawable getDrawerIcon() {
        return drawerIcon;
    }

    public Integer getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public Integer getPrimaryColor() {
        return primaryColor;
    }

    public Integer getAccentColor() {
        return accentColor;
    }

    public String getStepsChart() {
        return stepsChart;
    }

    public void setStepsChart(String stepsChart) {
        this.stepsChart = stepsChart;
    }

    public Integer getPieStepsColor() {
        return pieStepsColor;
    }

    public Integer getPieGoalColor() {
        return pieGoalColor;
    }

    public Integer getStepsChartColorSteps() {
        return stepsChartColorSteps;
    }

    public Integer getStepsChartColorGoal() {
        return stepsChartColorGoal;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public Drawable getAchievementLockedImage() {
        return achievementLockedImage;
    }

    public void isAboutEnabled(Context context, final AppsgeyserSDK.OnAboutDialogEnableListener onAboutDialogEnableListener){
        if(aboutEnabled != null){
            onAboutDialogEnableListener.onDialogEnableReceived(aboutEnabled);
        }else {
            AppsgeyserSDK.isAboutDialogEnabled(context, new AppsgeyserSDK.OnAboutDialogEnableListener() {
                @Override
                public void onDialogEnableReceived(boolean enabled) {
                    aboutEnabled = enabled;
                    onAboutDialogEnableListener.onDialogEnableReceived(enabled);
                }
            });
        }
    }

    public List<Tips> getTipsList() {
        return tipsList;
    }
}
