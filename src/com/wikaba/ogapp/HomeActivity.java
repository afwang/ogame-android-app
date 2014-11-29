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

import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.DatabaseManager;

import android.support.v7.app.ActionBarActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class HomeActivity extends ActionBarActivity {
	static final String DEFAULT_ACC = "default_account";
	boolean mAccountSelected;
	long accountRowId;
	
	private ServiceConnection agentServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			AgentService.AgentServiceBinder binder = (AgentService.AgentServiceBinder)service;
			mAgent = binder.getService();
			mBound = true;
			if(listeningFragment != null) {
				listeningFragment.onServiceConnected(name, service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			mAgent = null;
			if(listeningFragment != null) {
				listeningFragment.onServiceDisconnected(name);
			}
		}
	};
	AgentService mAgent;
	boolean mBound;
	private ServiceConnection listeningFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		accountRowId = prefs.getLong(DEFAULT_ACC, -1);
		DatabaseManager dbman = new DatabaseManager(this);
		AccountCredentials accountExists = dbman.getAccount(accountRowId);
		dbman.close();
		if(savedInstanceState == null) {
			if(accountRowId < 0 || accountExists == null) {
				if(savedInstanceState == null) {
					getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new NoAccountFragment()).commit();
				}
				mAccountSelected = false;
			}
			else {
				Bundle args = new Bundle();
				args.putString(OverviewFragment.UNIVERSE_KEY, accountExists.universe);
				args.putString(OverviewFragment.USERNAME_KEY, accountExists.username);
				
				OverviewFragment frag = new OverviewFragment();
				frag.setArguments(args);
				
				getSupportFragmentManager().beginTransaction()
				.add(R.id.container, frag, null).commit();
				mAccountSelected = true;
			}
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
		if(accountRowId >= 0) {
			SharedPreferences.Editor edit = getPreferences(MODE_PRIVATE).edit();
			edit.putLong(DEFAULT_ACC, accountRowId);
			edit.commit();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(agentServiceConn);
	}
	
	public void addAccount(String universe, String username, String password) {
		DatabaseManager dbman = new DatabaseManager(this);
		accountRowId = dbman.addAccount(universe, username, password);
		dbman.close();
		
		if(accountRowId < 0) {
			Toast.makeText(this, "There was a problem adding an account", Toast.LENGTH_LONG).show();
			return;
		}
		
		goToOverview();
	}
	
	public void setListener(ServiceConnection fragment) {
		listeningFragment = fragment;
	}
	
	public void unsetListener() {
		listeningFragment = null;
	}
	
	public void goToAccountSelector() {
		NoAccountFragment f = new NoAccountFragment();
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.container, f).commit();
	}
	
	public void goToOverview() {
		DatabaseManager dbman = new DatabaseManager(this);
		AccountCredentials accountExists = dbman.getAccount(accountRowId);
		dbman.close();
		
		Bundle args = new Bundle();
		args.putString(OverviewFragment.UNIVERSE_KEY, accountExists.universe);
		args.putString(OverviewFragment.USERNAME_KEY, accountExists.username);
		OverviewFragment confrag = new OverviewFragment();
		confrag.setArguments(args);
		
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.container, confrag).commit();
	}
}
