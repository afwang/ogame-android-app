/*
	Copyright 2014 Alexander Wang
    
    This file is part of Ogame on Android.

    Ogame on Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Ogame on Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp.ui.overview;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.ui.main.HomeActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OverviewFragment extends Fragment
        implements
        AbsListView.RecyclerListener,
        Runnable {

    private static final String HEADER_KEY = "com.wikaba.ogapp.OverviewFragment.HEADER";
    public static final String USERNAME_KEY = "com.wikaba.ogapp.OverviewFragment.USERNAME";
    public static final String UNIVERSE_KEY = "com.wikaba.ogapp.OverviewFragment.UNIVERSE";
    static final String ACC_ROWID = "account_row_id";

    private static final int LOADER_ID = 0;

    @Bind(R.id.name_universe)
    protected TextView header;

    @Bind(R.id.eventFleetView)
    protected ListView eventFleetView;

    @Bind(R.id.no_events)
    protected TextView noEventsText;

    @Bind(R.id.refresh_button)
    protected Button reload;

    private HomeActivity act;
    private Map<TextView, Long> etaTextViews;
    private Handler handler;

    public OverviewFragment() {
    }

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        this.act = (HomeActivity) act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_overview, container, false);

        ButterKnife.bind(this, root);

        etaTextViews = new HashMap<TextView, Long>();
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            Bundle args = this.getArguments();
            String username;
            String universe;
            if (args != null) {
                username = args.getString(USERNAME_KEY);
                universe = args.getString(UNIVERSE_KEY);
            } else {
                username = "ERROR";
                universe = "ERROR";
            }

            header.setText(universe + ", " + username);
        } else {
            String headerText = savedInstanceState.getString(HEADER_KEY);
            if (headerText == null) {
                headerText = "";
            }
            header.setText(headerText);
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        final long timeInMillis = 1000;

        handler = new Handler();
        retrieveFleetFromActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String uniText = header.getText().toString();
        outState.putString(HEADER_KEY, uniText);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(this);
        handler = null;
    }

    //TODO REGISTER FOR EVENTBUS EVENTS

    @Override
    public void onDetach() {
        super.onDetach();
        act = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.switch_accounts) {
            act.goToAccountSelector();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void retrieveFleetFromActivity() {
        reload.setVisibility(View.VISIBLE);

        List<FleetEvent> events = act.getCurrentOverviewData()._fleet_event;

        Resources res = getResources();
        if (events == null) {
            noEventsText.setText(res.getString(R.string.error_msg));
            events = new ArrayList<FleetEvent>();
            Toast.makeText(act, R.string.login_error, Toast.LENGTH_LONG).show();
        } else {
            noEventsText.setText(res.getString(R.string.no_events));
        }

        if (events.size() > 0) {
            eventFleetView.setVisibility(View.VISIBLE);
            noEventsText.setVisibility(View.GONE);
        } else {
            eventFleetView.setVisibility(View.GONE);
            noEventsText.setVisibility(View.VISIBLE);
        }

        eventFleetView.setAdapter(new EventAdapter(act, events, etaTextViews));
        eventFleetView.setRecyclerListener(this);
        final long timeInMillis = 1000;
        if (handler != null) {
            handler.postDelayed(this, timeInMillis);
        }
    }

    @Override
    public void onMovedToScrapHeap(View v) {
        EventViewHolder holder = (EventViewHolder) v.getTag();
        etaTextViews.remove(holder.eta);
    }

    @Override
    public void run() {
        Handler handler = this.handler;
        Iterator<Entry<TextView, Long>> entryiter = etaTextViews.entrySet().iterator();
        while (entryiter.hasNext()) {
            Entry<TextView, Long> anEntry = entryiter.next();
            TextView textview = anEntry.getKey();
            long arrivalInEpoch = anEntry.getValue().longValue();
            long currentTime = Calendar.getInstance().getTimeInMillis() / 1000;

            long timeLeft = arrivalInEpoch - currentTime;
            if (timeLeft <= 0) {
                textview.setText(getResources().getString(R.string.overview_arrived));
            } else {
                textview.setText(DateUtils.formatElapsedTime(timeLeft));
            }
        }
        final long timeInMillis = 1000;
        if (handler != null) {
            handler.postDelayed(this, timeInMillis);
        }
    }

    @OnClick(R.id.refresh_button)
    public void onRefreshButtonClicked() {
        eventFleetView.setVisibility(View.GONE);
        noEventsText.setVisibility(View.GONE);
        reload.setVisibility(View.INVISIBLE);
        if (act.isBound()) {
            getLoaderManager().getLoader(LOADER_ID).onContentChanged();
        }
        Toast.makeText(act, R.string.reload_in_progress, Toast.LENGTH_SHORT).show();
    }
}
