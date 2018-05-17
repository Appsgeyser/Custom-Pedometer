package de.j4velin.pedometer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsgeyser.sdk.AppsgeyserSDK;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.TipActivity;
import de.j4velin.pedometer.config.Achievement;
import de.j4velin.pedometer.config.Config;
import de.j4velin.pedometer.config.Tips;

/**
 * Created by roma on 03.05.2018.
 */

public class TipsListFragment extends Fragment {

    Config config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tips_list_layout, null);
        config = ((PedometerApp) getActivity().getApplication()).getConfig();

        final RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.useful_tips);


        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // specify an adapter (see also next example)
        MyAdapter mAdapter = new MyAdapter(getActivity(), config, config.getTipsList(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                Tips item = config.getTipsList().get(itemPosition);
                Intent intent = new Intent(getActivity(), TipActivity.class);
                intent.putExtra("url", item.getUrl());
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

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
    public boolean onOptionsItemSelected(final MenuItem item) {
        return ((Activity_Main) getActivity()).optionsItemSelected(item);
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Config config;
        private SharedPreferences sharedPreferences;

        private SimpleDateFormat simpleDateFormat;

        private List<Tips> tipsList;

        View.OnClickListener onClickListener;
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView name;

            public ViewHolder(View v, TextView name) {
                super(v);
                this.name = name;
            }
        }

        public MyAdapter(final Context context, Config config, List<Tips> tipsList, View.OnClickListener onClickListener) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            this.tipsList = tipsList;
            this.config = config;
            simpleDateFormat = new SimpleDateFormat("MMM dd", context.getResources().getConfiguration().locale);
            this.onClickListener = onClickListener;
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_tip_layout, parent, false);
            ViewHolder vh = new ViewHolder(v, (TextView) v.findViewById(R.id.name));
            v.setOnClickListener(onClickListener);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.name.setText(tipsList.get(position).getName());
            holder.name.setTextColor(config.getTextColor());
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return tipsList.size();
        }
    }
}
