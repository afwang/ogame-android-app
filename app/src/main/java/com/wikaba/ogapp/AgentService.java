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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.LongSparseArray;

import com.wikaba.ogapp.agent.CustomCookieManager;
import com.wikaba.ogapp.agent.LoggedOutException;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.models.ResourceItem;
import com.wikaba.ogapp.database.CookiesManager;
import com.wikaba.ogapp.events.OnLoggedEvent;
import com.wikaba.ogapp.events.OnLoginEvent;
import com.wikaba.ogapp.events.OnLoginRequested;
import com.wikaba.ogapp.utils.AccountCredentials;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class AgentService extends Service {
    static final String LOGTAG = "AgentService";

    private IBinder mBinder;
    private LongSparseArray<OgameAgent> ogameSessions;
    private volatile CookiesManager cookiesManager;

    public AgentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

        if (ogameSessions == null) {
            ogameSessions = new LongSparseArray<OgameAgent>();
        }

        if (cookiesManager == null) {
            cookiesManager = ApplicationController.getInstance().getCookiesManager();
        }

        CustomCookieManager cookieman = new CustomCookieManager();
        CookieStore cookiestore = cookieman.getCookieStore();

        //Retrieve all cookies from database.
        //Warning: This is currently done on the main thread.
        ArrayList<HttpCookie> cookieList = cookiesManager.getAllHttpCookies();
        for (Iterator<HttpCookie> cookieIter = cookieList.iterator(); cookieIter.hasNext(); ) {
            HttpCookie cookie = cookieIter.next();
            cookiestore.add(null, cookie);
        }

        CookieHandler.setDefault(cookieman);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new AgentServiceBinder();
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        CustomCookieManager cookieman = (CustomCookieManager) CookieHandler.getDefault();
        CookieStore cookiestore = cookieman.getCookieStore();
        List<HttpCookie> cookies = cookiestore.getCookies();
        cookiesManager.saveCookies(cookies);
        //TODO IN THE CONTROLLER MANAGE COOKIES WITH ONLY PROPER VALUES WITH THE LRUCACHE
        //AND STORE, MORE EFFICIENT
        super.onDestroy();
    }

    /**
     * <p>Logs in to the specified account (from database) using a software agent
     * for Ogame.</p>
     *
     * @param account AccountCredentials to use to log in
     * @return True if acquiring session cookies (logging in) for the account
     * completed successfully. False otherwise.
     */
    public boolean loginToAccount(AccountCredentials account) {
        OgameAgent agent = ogameSessions.get(account.id);
        if (agent == null) {
            agent = new OgameAgent(account.universe, account.lang);
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
        if (agent == null) {
            loginToAccount(account);
            agent = ogameSessions.get(account.id);
        }

        if (agent == null) {
            return null;
        }

        List<FleetEvent> events = null;
        try {
            events = agent.getOverviewData();
        } catch (LoggedOutException e) {
            e.printStackTrace();
            //Log in and try again!
            agent.login(account.universe, account.username, account.passwd, account.lang);
            try {
                events = agent.getOverviewData();
            } catch (LoggedOutException e1) {
                e1.printStackTrace();
            }
        }
        return events;
    }

    public class AgentServiceBinder extends Binder {
        public AgentService getService() {
            return AgentService.this;
        }
    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void onRequestLogin(OnLoginRequested login_request) {
        EventBus.getDefault().postSticky(new OnLoginEvent(true));
        synchronized (this) {
            AccountCredentials credentials = login_request.getAccountCredentials();
            loginToAccount(credentials);
            OgameAgent agent = ogameSessions.get(credentials.id);
            if (!agent.isLogin()) {
                boolean logged = agent.login(credentials.universe, credentials.username,
                        credentials.passwd, credentials.lang);
                List<FleetEvent> events = null;
                List<ResourceItem> current_resources = null;
                List<AbstractItemInformation> resources = null;
                List<AbstractItemInformation> building = null;
                List<AbstractItemInformation> research = null;
                List<AbstractItemInformation> shipyard = null;
                List<AbstractItemInformation> defense = null;
                if (logged) {
                    try {
                        events = agent.getFleetEvents();
                    } catch (LoggedOutException exception) {
                        //impossible since we are here when it is all ok
                    }

                    try {
                        String raw_res = agent.getResourcePagesContent();
                        current_resources = agent.getResourcesFromResourcePageContent(raw_res);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        resources = agent.getItemFromPage(ItemRepresentationFactory.getResourceConstants());
                        building = agent.getItemFromPage(ItemRepresentationFactory.getBuildingConstants());
                        research = agent.getItemFromPage(ItemRepresentationFactory.getResearchConstants());
                        shipyard = agent.getItemFromPage(ItemRepresentationFactory.getShipConstants());
                        defense = agent.getItemFromPage(ItemRepresentationFactory.getDefenseConstants());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                EventBus.getDefault().postSticky(new OnLoginEvent(false));
                EventBus.getDefault().postSticky(new OnLoggedEvent(logged, credentials,
                        agent, events, current_resources,
                        resources, building, research, shipyard, defense));
            }
            //no post event here
        }
        // or here
        //since the only way to be ok is via the current poststicky/post
    }
}
