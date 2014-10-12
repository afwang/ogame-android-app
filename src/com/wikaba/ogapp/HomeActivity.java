package com.wikaba.ogapp;

import com.wikaba.ogapp.utils.DatabaseManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		accountRowId = prefs.getLong(DEFAULT_ACC, -1);
		if(accountRowId < 0) {
			//TODO: Check if there is an account in database. If there is, load the
			//first one returned by cursor. If no account in database, load the no-acc
			//fragment
			if(savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new NoAccountFragment()).commit();
			}
			mAccountSelected = false;
		}
		else {
			mAccountSelected = true;
		}
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
			
			ContentFragment confrag = new ContentFragment();
			Bundle fragargs = new Bundle();
			fragargs.putLong(ContentFragment.ACC_ROWID, accountRowId);
			confrag.setArguments(fragargs);
			
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, confrag).commit();
		}
	}
}
