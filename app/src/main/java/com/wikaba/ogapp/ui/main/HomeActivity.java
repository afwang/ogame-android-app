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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.wikaba.ogapp.AgentService;
import com.wikaba.ogapp.ApplicationController;
import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.events.OnLoggedEvent;
import com.wikaba.ogapp.events.OnLoginRequested;
import com.wikaba.ogapp.ui.login.LoginFragment;
import com.wikaba.ogapp.ui.overview.OverviewFragment;
import com.wikaba.ogapp.utils.AccountCredentials;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class HomeActivity extends AppCompatActivity {
    private static final String ACCOUNT_KEY = "com.wikaba.ogapp.HomeActivity.activeAccount";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new NoAccountFragment()).commit();
        } else {
            activeAccount = savedInstanceState.getParcelable(ACCOUNT_KEY);
        }
        mBound = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
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

    public void addAccount(AccountCredentials creds) {
        AccountsManager manager = ApplicationController.getInstance().getAccountsManager();
        long accountRowId = manager.addAccount(creds.universe, creds.username, creds.passwd, creds.lang);

        if (accountRowId < 0) {
            Toast.makeText(this, "There was a problem adding an account", Toast.LENGTH_LONG).show();
            return;
        }

        activeAccount = new AccountCredentials(creds);
        activeAccount.id = accountRowId;

        goToLogin();
        EventBus.getDefault().post(new OnLoginRequested(activeAccount));
    }

    public void goToAccountSelector() {
        NoAccountFragment f = new NoAccountFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f).commit();
    }

    public void goToLogin() {
        LoginFragment confrag = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, confrag).commit();
    }

    public void goToOverview() {
        if (activeAccount != null) {
            Bundle args = new Bundle();
            args.putString(OverviewFragment.UNIVERSE_KEY, activeAccount.universe);
            args.putString(OverviewFragment.USERNAME_KEY, activeAccount.username);
            OverviewFragment confrag = new OverviewFragment();
            confrag.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, confrag).commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEventLogged(OnLoggedEvent event) {
        if (event.isConnected()) {
            _current_logged_event = event;

            goToOverview();
        } else {
            goToAccountSelector();
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

    public List<FleetEvent> getCurrentFleetEvents() {
        return _current_logged_event != null ? _current_logged_event.getFleetEvents() : null;
    }
}
