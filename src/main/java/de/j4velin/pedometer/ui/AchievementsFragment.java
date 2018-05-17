package de.j4velin.pedometer.ui;

import android.content.Context;
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
import android.view.Gravity;
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
import com.appsgeyser.sdk.configuration.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.j4velin.pedometer.PedometerApp;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.config.Achievement;
import de.j4velin.pedometer.config.Config;

/**
 * Created by roma on 03.05.2018.
 */

public class AchievementsFragment extends Fragment {

    Config config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.achievements_layout, null);
        config = ((PedometerApp) getActivity().getApplication()).getConfig();

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.list);


        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Collections.sort(config.getAchievementList(), new Comparator<Achievement>() {
            @Override
            public int compare(Achievement o1, Achievement o2) {
                Boolean b1 = (sharedPreferences.getLong("achievement_" + o1.getName().hashCode(), 0) != 0);
                Boolean b2 = (sharedPreferences.getLong("achievement_" + o2.getName().hashCode(), 0) != 0);

                return b2.compareTo(b1);
            }
        });

        // specify an adapter (see also next example)
        MyAdapter mAdapter = new MyAdapter(getActivity(), config, config.getAchievementList());
        mRecyclerView.setAdapter(mAdapter);

        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.achievements);

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
        if (config.getNavigation().equals(Config.Navigation.DRAWER.getName())) {
            inflater.inflate(R.menu.main_menu_drawer, menu);
        } else if (config.getNavigation().equals(Config.Navigation.TABS.getName())) {
            inflater.inflate(R.menu.tabs_menu, menu);
        } else {
            inflater.inflate(R.menu.main, menu);
        }

    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(config.getNavigation().equals(Config.Navigation.MENU.getName())){
            menu.findItem(R.id.action_split_count).setVisible(false);
            menu.findItem(R.id.action_achievements).setVisible(false);
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

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Config config;
        private SharedPreferences sharedPreferences;

        private SimpleDateFormat simpleDateFormat;

        private List<Achievement> achievements;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public TextView description;
            public TextView date;
            public ImageView imageView;
            public CardView cardView;

            public ViewHolder(View v, CardView cardView, TextView name, TextView description, TextView date, ImageView imageView) {
                super(v);
                this.cardView = cardView;
                this.name = name;
                this.description = description;
                this.date = date;
                this.imageView = imageView;
            }
        }

        public MyAdapter(Context context, Config config, List<Achievement> achievements) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            this.achievements = achievements;
            this.config = config;
            simpleDateFormat = new SimpleDateFormat("MMM dd", context.getResources().getConfiguration().locale);
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.achievement_card_view, parent, false);
            ViewHolder vh = new ViewHolder(v, (CardView) v.findViewById(R.id.card), (TextView) v.findViewById(R.id.name),
                    (TextView) v.findViewById(R.id.description), (TextView) v.findViewById(R.id.date), (ImageView) v.findViewById(R.id.image));
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.name.setText(achievements.get(position).getName());
            holder.description.setText(achievements.get(position).getDescription());
            if (sharedPreferences.getLong("achievement_" + achievements.get(position).getName().hashCode(), 0) == 0) {
                holder.imageView.setImageDrawable(config.getAchievementLockedImage());
                holder.date.setVisibility(View.INVISIBLE);
            } else {
                holder.imageView.setImageDrawable(config.createDrawable(holder.imageView.getContext(), achievements.get(position).getImageUrl()));
                holder.date.setText(simpleDateFormat.format(new Date(sharedPreferences.getLong("achievement_" + achievements.get(position).getName().hashCode(), 0))));
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return achievements.size();
        }
    }
}
