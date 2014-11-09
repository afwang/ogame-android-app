package com.wikaba.ogapp;

import com.wikaba.ogapp.utils.DatabaseManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

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
		if(accountRowId < 0) {
			if(savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new NoAccountFragment()).commit();
			}
			mAccountSelected = false;
		}
		else {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new OverviewFragment(), null).commit();
			mAccountSelected = true;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void addAccount(String universe, String username, String password) {
		DatabaseManager dbman = new DatabaseManager(this);
		accountRowId = dbman.addAccount(universe, username, password);
		dbman.close();
		
		if(accountRowId < 0) {
			Toast.makeText(this, "There was a problem adding an account", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mAccountSelected) {
			mAccountSelected = true;
			
			OverviewFragment confrag = new OverviewFragment();
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, confrag).commit();
		}
	}
	
	public void setListener(ServiceConnection fragment) {
		listeningFragment = fragment;
	}
	
	public void unsetListener() {
		listeningFragment = null;
	}
}
