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

	You should have received a copy of the GNU General Public License along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.wikaba.ogapp.agent.CustomCookieManager;
import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.LoggedOutException;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.DatabaseManager;

public class AgentService extends Service {
	static final String LOGTAG = "AgentService";

	private IBinder mBinder;
	private LongSparseArray<OgameAgent> ogameSessions;
	private volatile DatabaseManager dbman;

	public AgentService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if(ogameSessions == null) {
			ogameSessions = new LongSparseArray<OgameAgent>();
		}

		if(dbman == null) {
			dbman = new DatabaseManager(this);
		}

		CustomCookieManager cookieman = new CustomCookieManager();
		CookieStore cookiestore = cookieman.getCookieStore();

		//Retrieve all cookies from database.
		//Warning: This is currently done on the main thread.
		ArrayList<HttpCookie> cookieList = dbman.getCookies();
		for(Iterator<HttpCookie> cookieIter = cookieList.iterator(); cookieIter.hasNext(); ) {
			HttpCookie cookie = cookieIter.next();
			cookiestore.add(null, cookie);
		}

		CookieHandler.setDefault(cookieman);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(mBinder == null) {
			mBinder = new AgentServiceBinder();
		}
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		CustomCookieManager cookieman = (CustomCookieManager)CookieHandler.getDefault();
		CookieStore cookiestore = cookieman.getCookieStore();
		List<HttpCookie> cookies = cookiestore.getCookies();
		dbman.saveCookies(cookies);

		if(dbman != null) {
			dbman.close();
			dbman = null;
		}
	}

	/**
	 * <p>Logs in to the specified account (from database) using a software agent
	 * for Ogame.</p>
	 *
	 * @param account AccountCredentials to use to log in
	 * @return True if acquiring session cookies (logging in) for the account
	 * 	completed successfully. False otherwise.
	 */
	public boolean loginToAccount(AccountCredentials account) {
		OgameAgent agent = ogameSessions.get(account.id);
		if(agent == null) {
			agent = new OgameAgent(account.universe);
			ogameSessions.put(account.id, agent);
		}
		return true;
	}

	/**
	 * <p>Returns the fleet events parsed from the overview event screen.</p>
	 * 
	 * @param account Account to use to log in
	 * @return list of fleet events from overview screen. Returns null on error.
	 */
	public List<FleetEvent> getFleetEvents(AccountCredentials account) {
		OgameAgent agent = ogameSessions.get(account.id);
		if(agent == null) {
			loginToAccount(account);
			agent = ogameSessions.get(account.id);
		}

		if(agent == null) {
			return null;
		}

		List<FleetEvent> events = null;
		final int retryAttempts = 3;
		for(int attempts = 0; attempts < retryAttempts; attempts++) {
			try {
				events = agent.getOverviewData();
				break;
			}
			catch(LoggedOutException e) {
				//Log in and try again!
				agent.login(account.universe, account.username, account.passwd);
			}
		}
		return events;
	}

	public class AgentServiceBinder extends Binder{
		public AgentService getService() {
			return AgentService.this;
		}
	}
}
