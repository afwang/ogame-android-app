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

package com.wikaba.ogapp.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.wikaba.ogapp.AgentService;
import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.events.OnLoggedEvent;
import com.wikaba.ogapp.ui.listings.ListingFragmentWithEvent;
import com.wikaba.ogapp.ui.login.NoAccountActivity;
import com.wikaba.ogapp.ui.overview.OverviewFragment;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.Constants;
import com.wikaba.ogapp.utils.FragmentStackManager;
import com.wikaba.ogapp.utils.SystemFittableActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class HomeActivity extends SystemFittableActivity {
    private static final String ACCOUNT_KEY = "com.wikaba.ogapp.HomeActivity.activeAccount";

    @Bind(R.id.drawerLayout)
    protected DrawerLayout mDrawerLayout;


    @OnClick(R.id.main)
    public void onOverView() {
        goToOverview();
        closeDrawer();
    }

    @OnClick(R.id.resource)
    public void onResourceClick() {
        push(Constants.RESOURCES_INDEX);
    }

    @OnClick(R.id.building)
    public void onBuildingClick() {
        push(Constants.BUILDING_INDEX);
    }

    @OnClick(R.id.research)
    public void onResearchClick() {
        push(Constants.RESEARCH_INDEX);
    }

    @OnClick(R.id.shipyard)
    public void onShipyardCLick() {
        push(Constants.SHIPYARD_INDEX);
    }

    @OnClick(R.id.defense)
    public void onDefenseClick() {
        push(Constants.DEFENSE_INDEX);
    }

    private AccountCredentials activeAccount;
    private OnLoggedEvent _current_logged_event;

    private ServiceConnection agentServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AgentService.AgentServiceBinder binder = (AgentService.AgentServiceBinder) service;
            mAgent = binder.getService();
            mBound = true;
            //TODO POST EVENT CONNECTED TO SERVICE
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mAgent = null;
            //TODO POST EVENT DISCONNECT FROM SERVICE
        }
    };
    private AgentService mAgent;
    private boolean mBound;
    protected View mMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        _actionbar_toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,         /* DrawerLayout object */
                getToolbar(),
                0,
                0) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                hideKeyboard(mDrawerLayout);
            }
        };

        mDrawerLayout.setDrawerListener(_actionbar_toggle);


        //no creation since the register on eventbus will created for us the event
        mBound = false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        setupToolbar();
        setupDrawer();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        setupToolbar();
        setupDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindingIntent = new Intent(this, AgentService.class);
        startService(bindingIntent); //start it consistently
        bindService(bindingIntent, agentServiceConn, Context.BIND_AUTO_CREATE);
        //and bind to it
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeAccount != null) {
            outState.putParcelable(ACCOUNT_KEY, activeAccount);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(agentServiceConn);
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _actionbar_toggle.onConfigurationChanged(newConfig);
    }

    private boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void setupDrawer() {
        mMenuView = new View(this);
        mMenuView.setBackgroundColor(Color.TRANSPARENT);
        mMenuView.setPadding(0, getPaddingInsetTop(false), 0, 0);


        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    protected void setupToolbar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_menu_white);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public void goToAccountSelector() {
        EventBus.getDefault().postSticky(new OnLoggedEvent(false, null, null, null, null));
    }

    public void goToOverview() {
        if (activeAccount != null) {
            Bundle args = new Bundle();
            args.putString(OverviewFragment.UNIVERSE_KEY, activeAccount.universe);
            args.putString(OverviewFragment.USERNAME_KEY, activeAccount.username);

            push(Constants.OVERVIEW_INDEX, args);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void onEventLogged(OnLoggedEvent event) {
        if (event.isConnected()) {
            _current_logged_event = event;
            activeAccount = event.getCredentials();

            goToOverview();
        } else {
            startLoginActivity();
        }
    }

    public void setActiveAccount(AccountCredentials acc) {
        this.activeAccount = acc;
    }

    public AccountCredentials getAccountCredentials() {
        AccountCredentials creds = new AccountCredentials();
        creds.universe = activeAccount.universe;
        creds.username = activeAccount.username;
        creds.passwd = activeAccount.passwd;
        creds.lang = activeAccount.lang;
        return creds;
    }

    /**
     * Check if this Activity is bound to the AgentService yet
     *
     * @return true if the Activity is bound, false otherwise
     */
    public boolean isBound() {
        return mBound;
    }

    /**
     * Return the AgentService that is bound to this activity
     *
     * @return the AgentService bound to this activity
     */
    public AgentService getAgentService() {
        return mAgent;
    }

    public OgameAgent getCurrentOgameAgent() {

        return _current_logged_event != null ? _current_logged_event.getOgameAgent() : null;
    }

    public OverviewData getCurrentOverviewData() {
        return _current_logged_event != null ? _current_logged_event.getOverviewData() : null;
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, NoAccountActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


    //Manager used to push / pop Fragments
    @Override
    protected FragmentStackManager getFragmentStackManager() {
        return new HomeFragmentStackManager(this, R.id.container);
    }

    private void push(int type) {
        try {
            push(type, ListingFragmentWithEvent.createBundleInstance(type));
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDrawer();
    }
}
