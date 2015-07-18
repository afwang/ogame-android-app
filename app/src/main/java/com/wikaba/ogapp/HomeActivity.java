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

package com.wikaba.ogapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.utils.AccountCredentials;

public class HomeActivity extends AppCompatActivity {
    private static final String ACCOUNT_KEY = "com.wikaba.ogapp.HomeActivity.activeAccount";
    private AccountCredentials activeAccount;

    private ServiceConnection agentServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AgentService.AgentServiceBinder binder = (AgentService.AgentServiceBinder) service;
            mAgent = binder.getService();
            mBound = true;
            if (listeningFragment != null) {
                listeningFragment.serviceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mAgent = null;
            if (listeningFragment != null) {
                listeningFragment.serviceDisconnected();
            }
        }
    };
    private AgentService mAgent;
    private boolean mBound;
    private AgentServiceConsumer listeningFragment;

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
    protected void onStart() {
        super.onStart();
        Intent bindingIntent = new Intent(this, AgentService.class);
        bindService(bindingIntent, agentServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
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
        long accountRowId = manager.addAccount(creds.universe, creds.username, creds.passwd);

        if (accountRowId < 0) {
            Toast.makeText(this, "There was a problem adding an account", Toast.LENGTH_LONG).show();
            return;
        }

        activeAccount = new AccountCredentials(creds);
        activeAccount.id = accountRowId;
        goToOverview();
    }

    public void setListener(AgentServiceConsumer fragment) {
        listeningFragment = fragment;
        if (mBound) {
            listeningFragment.serviceConnected();
        }
    }

    public void unsetListener() {
        if (listeningFragment != null) {
            listeningFragment.serviceDisconnected();
        }
        listeningFragment = null;
    }

    public void goToAccountSelector() {
        NoAccountFragment f = new NoAccountFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f).commit();
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

    public void setActiveAccount(AccountCredentials acc) {
        this.activeAccount = acc;
    }

    public AccountCredentials getAccountCredentials() {
        AccountCredentials creds = new AccountCredentials();
        creds.universe = activeAccount.universe;
        creds.username = activeAccount.username;
        creds.passwd = activeAccount.passwd;
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
}
