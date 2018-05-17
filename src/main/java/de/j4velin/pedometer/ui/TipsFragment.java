package de.j4velin.pedometer.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.config.Achievement;
import de.j4velin.pedometer.config.Config;

/**
 * Created by roma on 03.05.2018.
 */

public class TipsFragment extends Fragment {

    Config config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tips_layout, null);
        config = ((PedometerApp) getActivity().getApplication()).getConfig();


        WebView webView = v.findViewById(R.id.webView);
        final ProgressBar progress = v.findViewById(R.id.progress);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                progress.setVisibility(View.GONE);
            }
        });
        //webView.loadUrl(config.getTipsUrl());

        setHasOptionsMenu(true);
        ((Activity_Main) getActivity()).showFullscreen();

        return v;
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
        inflater.inflate(R.menu.tips_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Drawable d = getResources().getDrawable(R.drawable.ic_share_black_24dp);
        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.action_share).setIcon(d);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return ((Activity_Main) getActivity()).optionsItemSelected(item);
    }
}
