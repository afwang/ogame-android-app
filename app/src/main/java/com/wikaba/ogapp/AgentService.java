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

import android.app.IntentService;
import android.content.Intent;

import com.squareup.okhttp.OkHttpClient;
import com.wikaba.ogapp.agent.CustomCookieManager;
import com.wikaba.ogapp.agent.LoggedOutException;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.constants.OgameAgentState;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.database.CookiesManager;
import com.wikaba.ogapp.events.OnAgentUpdateEvent;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.OgameAgentManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;

public class AgentService extends IntentService {
	public static final Logger logger = LoggerFactory.getLogger(AgentService.class);

	public static final Lock clientLock = new ReentrantLock();
	private volatile static OkHttpClient httpClient;

	public AgentService() {
		super(AgentService.class.getName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(httpClient == null) {
			initHttpClient();
		}
		//Begin branching the codepaths depending on what we're asked to do.
		int actionCode = intent.getIntExtra(AgentActions.AGENT_ACTION_KEY, -1);
		long ogameAgentKey = intent.getLongExtra(AgentActions.OGAME_AGENT_KEY, -1);
		OgameAgent agent = OgameAgentManager.getInstance().get(ogameAgentKey);
		if(agent == null) {
			AccountCredentials credentials = intent.getParcelableExtra(AgentActions.ACCOUNT_CREDENTIAL_KEY);
			if(credentials == null) {
				logger.info("The OgameAgent object we've been asked to use is no longer available.\n{}",
						"Perhaps the object was removed from the manager while we were working on something else.");
				return;
			}
			addAccount(credentials);
			ogameAgentKey = buildOgameAgent(credentials);
			agent = OgameAgentManager.getInstance().get(ogameAgentKey);
			if(agent == null)  {
				//TODO: Send a Toast saying there was trouble creating the OgameAgent object
				return;
			}
		}

		boolean agentUpdated = true;
		switch(actionCode) {
			case AgentActions.LOGIN:
				agentUpdated = loginToAccount(agent);
				if(agentUpdated) {
					List<HttpCookie> cookieList = getCookiesFromStore();
					CookiesManager.getInstance().saveCookies(cookieList);
				}
				break;
			case AgentActions.OVERVIEW:
				//TODO: load overview screen
				//TODO: Update OgameAgent with new data.
				break;
			case AgentActions.RESOURCES:
				//TODO: load resources screen
				//TODO: Update OgameAgent with new data.
				break;
			default:
				logger.error("Invalid AgentAction code sent to AgentService: {}", actionCode);
				agentUpdated = false;
		}
		EventBus.getDefault().postSticky(new OnAgentUpdateEvent(ogameAgentKey, agentUpdated));
	}

	private void initHttpClient() {
		try {
			clientLock.lock();
			if(httpClient == null) {
				httpClient = new OkHttpClient();
				CustomCookieManager cm = retrieveCookies();
				httpClient.setCookieHandler(cm);
			}
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * Retrieve all cookies from database.
	 */
	private CustomCookieManager retrieveCookies() {
		CookiesManager cookieDbMan = CookiesManager.getInstance();
		CustomCookieManager cm = new CustomCookieManager();
		CookieStore cs = cm.getCookieStore();
		ArrayList<HttpCookie> cookieList = cookieDbMan.getAllHttpCookies();
		for (Iterator<HttpCookie> cookieIter = cookieList.iterator(); cookieIter.hasNext(); ) {
			HttpCookie cookie = cookieIter.next();
			cs.add(null, cookie);
		}
		return cm;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * Login on the given agent.
	 *
	 * @param agent Agent to login on.
	 * @return True if login completed successfully. False otherwise.
	 */
	private boolean loginToAccount(OgameAgent agent) {
		if (agent == null) {
			return false;
		}
		return agent.login();
	}

	/**
	 * Retrieve all cookies from the CookieStore of the HTTP client.
	 * @return List of cookies from the CookieStore of the HTTP client.
	 */
	private List<HttpCookie> getCookiesFromStore() {
		if(httpClient == null) {
			return new ArrayList<>();
		}
		CustomCookieManager cm = (CustomCookieManager)httpClient.getCookieHandler();
		CookieStore cs = cm.getCookieStore();
		return cs.getCookies();
	}

	/**
	 * <p>Returns the fleet events parsed from the overview event screen.</p>
	 *
	 * @param agent OgameAgent account to use to log in
	 * @return list of fleet events from overview screen. Returns null on error.
	 */
	private OverviewData loadOverview(OgameAgent agent) {
		if(agent == null || agent.getState() == OgameAgentState.LOGGED_OUT) {
			return null;
		}

		OverviewData events = null;
		try {
			events = agent.loadOverviewData();
		} catch (LoggedOutException e) {
			logger.info("We got logged out by the server. Logging in and trying to load overview");
			//Log in and try again!
			agent.login();
			try {
				events = agent.loadOverviewData();
			} catch (LoggedOutException e1) {
				logger.error(
						"We couldn't load overview data even after relogging in. Something is wrong.",
						e1
				);
			}
		}
		return events;
	}

	private long buildOgameAgent(AccountCredentials credentials) {
		OgameAgentManager ogm = OgameAgentManager.getInstance();
		if(httpClient == null) {
			initHttpClient();
		}
		return ogm.getOrBuild(credentials, httpClient);
	}

	private long addAccount(AccountCredentials credentials) {
		long id = credentials.getId();
		if(id >= 0) {
			return id;
		}
		AccountsManager manager = AccountsManager.getInstance();
		long accountRowId = manager.addAccount(
				credentials.getUniverse(),
				credentials.getUsername(),
				credentials.getPasswd(),
				credentials.getLang()
		);
		credentials.setId(accountRowId);
		//TODO: If accountRowId < 0, make toast informing user that their account could not be
		//added.
		return accountRowId;
	}
}
