/*
 * Copyright 2013 Thomas Hoffmann
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.ads.AdView;
import com.appsgeyser.sdk.configuration.Constants;

import de.hdodenhof.circleimageview.CircleImageView;
import de.j4velin.pedometer.BuildConfig;
import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.SensorListener;
import de.j4velin.pedometer.config.Config;

public class Activity_Main extends AppCompatActivity {

    private Config config;

    DrawerLayout mDrawerLayout;

    TabLayout tabLayout;
    ViewPager viewPager;
    AdView adView;

    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        config = ((PedometerApp) getApplication()).getConfig();

        if (config.getNavigation().equals(Config.Navigation.DRAWER.getName())) {
            setContentView(R.layout.main_activity_drawer);
            AppsgeyserSDK.takeOff(this,
                    getString(R.string.widgetID),
                    getString(R.string.app_metrica_on_start_event),
                    getString(R.string.template_version));
            mDrawerLayout = findViewById(R.id.drawer_layout);

            final NavigationView navigationView = findViewById(R.id.nav_view);
            if(config.getAchievementList().size() == 0) {
                navigationView.getMenu().findItem(R.id.action_achievements).setVisible(false);
            }

            config.isAboutEnabled(this, new AppsgeyserSDK.OnAboutDialogEnableListener() {
                @Override
                public void onDialogEnableReceived(boolean enabled) {
                    navigationView.getMenu().findItem(R.id.action_about).setVisible(enabled);
                }
            });
            if(config.getTipsList().size() == 0) {
                navigationView.getMenu().findItem(R.id.tips).setVisible(false);
            }
            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            // set item as selected to persist highlight
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            optionsItemSelected(menuItem);
                            switch (menuItem.getItemId()) {
                                case R.id.overview:
                                case R.id.action_achievements:
                                case R.id.action_settings:
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);

            final Drawable menuButton = getResources().getDrawable(R.drawable.ic_menu_black_24dp);
            menuButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            actionbar.setHomeAsUpIndicator(menuButton);
            if (b == null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, getFragmentOverview(), "overview");
                transaction.commit();
            }

            View getHeaderView = navigationView.getHeaderView(0);
            RelativeLayout headerRoot = getHeaderView.findViewById(R.id.headerRoot);
            headerRoot.setBackground(config.getDrawerBackgroundImage());

            CircleImageView circleImageView = getHeaderView.findViewById(R.id.circleView);
            if(config.getDrawerIcon() != null) {
                circleImageView.setImageDrawable(config.getDrawerIcon());
            }

            TextView drawerTitle = getHeaderView.findViewById(R.id.name);
            drawerTitle.setText(config.getName());

        } else if (config.getNavigation().equals(Config.Navigation.TABS.getName())) {
            setContentView(R.layout.main_activity_tabs);
            AppsgeyserSDK.takeOff(this,
                    getString(R.string.widgetID),
                    getString(R.string.app_metrica_on_start_event),
                    getString(R.string.template_version));
            tabLayout = findViewById(R.id.tabs);
            viewPager = findViewById(R.id.pager);

            viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(0);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.getTabAt(0).setText(R.string.overview);
            tabLayout.getTabAt(1).setText(R.string.settings);
            if(config.getAchievementList().size() > 0) {
                tabLayout.getTabAt(2).setText(R.string.achievements);
                if(config.getTipsList().size() > 0) {
                    tabLayout.getTabAt(3).setText(R.string.useful_tips);
                }
            }else if(config.getTipsList().size() > 0){
                tabLayout.getTabAt(2).setText(R.string.useful_tips);
            }

            tabLayout.setSelectedTabIndicatorColor(config.getAccentColor());
            tabLayout.setTabTextColors(Color.parseColor("#eeeeee"),config.getAccentColor());

            tabLayout.setBackgroundColor(config.getPrimaryColor());

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);

            final Drawable menuButton = getResources().getDrawable(R.drawable.ic_more_vert_black_24dp);
            menuButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(menuButton);

            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(false);
        }else {
            setContentView(R.layout.main_activity);
            AppsgeyserSDK.takeOff(this,
                    getString(R.string.widgetID),
                    getString(R.string.app_metrica_on_start_event),
                    getString(R.string.template_version));
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(Color.WHITE);

            final Drawable menuButton = getResources().getDrawable(R.drawable.ic_more_vert_black_24dp);
            menuButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(menuButton);


            setSupportActionBar(toolbar);
            if (b == null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, getFragmentOverview(), "overview");
                transaction.commit();
            }

            final Drawable backButton = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
            backButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(backButton);
        }
        startService(new Intent(this, SensorListener.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(config.getPrimaryDarkColor());
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(config.getPrimaryColor()));
        getSupportActionBar().setTitle(config.getName());
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 23 && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        ImageView background = findViewById(R.id.background);
        if (config.isUseBackgroundImage()) {
            background.setImageDrawable(config.getBackgroundImage());
        } else if (config.getBackgroundColor() != null) {
            background.setImageDrawable(new ColorDrawable(config.getBackgroundColor()));
        }
        adView = (AdView) findViewById(R.id.adView);
    }

    private Fragment getFragmentOverview(){
        if(config.getLayout().equals(Config.LAYOUT_BASIC)){
            return new Fragment_Overview();
        }else if(config.getLayout().equals(Config.LAYOUT_PROGRESS)){
            return  new FragmentOverviewProgress();
        }else if(config.getLayout().equals(Config.LAYOUT_APPBAR)){
            return new FragmentOverviewWideAppbar();
        }
        return new Fragment_Overview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppsgeyserSDK.onPause(this);
        if (adView != null) {
            adView.onPause();//into onPause()
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppsgeyserSDK.onResume(this);
        if (adView != null) {
            adView.onResume();//into onResume()
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    public boolean optionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (config.getNavigation().equals(Config.Navigation.DRAWER.getName())) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
                } else {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                break;
            case R.id.action_settings:
                item.setChecked(true);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new Fragment_Settings()).addToBackStack(null)
                        .commit();
                break;
            case R.id.action_achievements:
                item.setChecked(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AchievementsFragment(), "achievement").addToBackStack(null)
                        .commit();
                break;
            case R.id.action_about:
                AppsgeyserSDK.showAboutDialog(this);
                break;
            case R.id.overview:
                item.setChecked(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, getFragmentOverview(), "overview").addToBackStack(null)
                        .commit();
                break;
            case R.id.tips:
                item.setChecked(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new TipsListFragment(), "tips").addToBackStack(null)
                        .commit();
                break;
            case R.id.action_split_count:
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("overview");
                if (fragment != null) {
                    fragment.onOptionsItemSelected(item);
                }
                return true;
            case R.id.action_share:
                /*Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = config.getTipsUrl();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));*/
                break;
        }
        return true;
    }

    private class MainPagerAdapter extends FragmentStatePagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position){
                case 0:
                    return getFragmentOverview();
                case 1:
                    return new Fragment_Settings();
                case 2:
                    if(config.getAchievementList().size() > 0) {
                        return new AchievementsFragment();
                    }
                    return new TipsListFragment();
                case 3:
                    return new TipsListFragment();
                default:
                    return new Fragment_Settings();
            }
        }

        @Override
        public int getCount() {
            int count =2;
            if(config.getAchievementList().size() > 0) {
                count++;
            }
            if(config.getTipsList().size() > 0) {
                count++;
            }
            return count;
        }
    }


    long lastShownTime = 0;
    public void showFullscreen(){
        if(System.currentTimeMillis() - lastShownTime > 2 * 60 * 1000) {
            AppsgeyserSDK
                    .getFullScreenBanner(this)
                    .load(Constants.BannerLoadTags.ON_START);
            lastShownTime = System.currentTimeMillis();
        }
    }
}