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
import com.wikaba.ogapp.agent.constants.ItemRepresentationConstant;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.agent.models.ResourceItem;
import com.wikaba.ogapp.database.CookiesManager;
import com.wikaba.ogapp.events.OnLoggedEvent;
import com.wikaba.ogapp.events.OnLoginEvent;
import com.wikaba.ogapp.events.OnLoginRequested;
import com.wikaba.ogapp.events.abstracts.OnAbstractListInformationLoaded;
import com.wikaba.ogapp.events.contents.OnBuildingLoaded;
import com.wikaba.ogapp.events.contents.OnDefensesLoaded;
import com.wikaba.ogapp.events.contents.OnResearchsLoaded;
import com.wikaba.ogapp.events.contents.OnResourceRequestToLoadEvent;
import com.wikaba.ogapp.events.contents.OnResourcesLoaded;
import com.wikaba.ogapp.events.contents.OnShipyardsLoaded;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.Constants;

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
    public OverviewData getFleetEvents(AccountCredentials account) {
        OgameAgent agent = ogameSessions.get(account.id);
        if (agent == null) {
            loginToAccount(account);
            agent = ogameSessions.get(account.id);
        }

        if (agent == null) {
            return null;
        }

        OverviewData events = null;
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
                OverviewData events = null;
                List<ResourceItem> current_resources = null;
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

                    EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent,
                            Constants.RESOURCES_INDEX,
                            ItemRepresentationFactory.getResourceConstants()));

                    EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent,
                            Constants.BUILDING_INDEX,
                            ItemRepresentationFactory.getBuildingConstants()));

                    EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent,
                            Constants.RESEARCH_INDEX,
                            ItemRepresentationFactory.getResearchConstants()));

                    EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent,
                            Constants.SHIPYARD_INDEX,
                            ItemRepresentationFactory.getShipConstants()));

                    EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent,
                            Constants.DEFENSE_INDEX,
                            ItemRepresentationFactory.getDefenseConstants()));
                }
                EventBus.getDefault().postSticky(new OnLoginEvent(false));
                EventBus.getDefault().postSticky(new OnLoggedEvent(logged, credentials,
                        agent, events, current_resources));
            } else {
                //cancel the call ?
                //EventBus.getDefault().postSticky(new OnResourcesLoaded(agent, null, false));
                //EventBus.getDefault().postSticky(new OnBuildingLoaded(agent, null, false));
                //EventBus.getDefault().postSticky(new OnResearchsLoaded(agent, null, false));
                //EventBus.getDefault().postSticky(new OnShipyardsLoaded(agent, null, false));
                //EventBus.getDefault().postSticky(new OnDefensesLoaded(agent, null, false));
                //for now, the previous calls are commented out, so we must in UI
                //check if the saved Sticky events is corresponding to the current logged
            }
            //no post event here
        }
        // or here
        //since the only way to be ok is via the current poststicky/post
    }

    @Subscribe(threadMode = ThreadMode.Async)
    public void onResourceRequestToLoading(OnResourceRequestToLoadEvent event) {
        OnAbstractListInformationLoaded resource_request_to_load = null;

        switch (event.getRequested()) {
            case Constants.RESOURCES_INDEX:
                resource_request_to_load = new OnResourcesLoaded(event.getOgameAgent(),
                        event.getRequested(), null, Constants.Status.LOADING);
                break;
            case Constants.BUILDING_INDEX:
                resource_request_to_load = new OnBuildingLoaded(event.getOgameAgent(),
                        event.getRequested(), null, Constants.Status.LOADING);
                break;
            case Constants.RESEARCH_INDEX:
                resource_request_to_load = new OnResearchsLoaded(event.getOgameAgent(),
                        event.getRequested(), null, Constants.Status.LOADING);
                break;
            case Constants.SHIPYARD_INDEX:
                resource_request_to_load = new OnShipyardsLoaded(event.getOgameAgent(),
                        event.getRequested(), null, Constants.Status.LOADING);
                break;
            case Constants.DEFENSE_INDEX:
                resource_request_to_load = new OnDefensesLoaded(event.getOgameAgent(),
                        event.getRequested(), null, Constants.Status.LOADING);
                break;
        }

        if (resource_request_to_load != null) {
            EventBus.getDefault().postSticky(resource_request_to_load);

            ItemRepresentationConstant content = event.getContent();
            List<AbstractItemInformation> retrieved = event.getOgameAgent().getItemFromPage(content);
            resource_request_to_load.setRetrieved(retrieved);
            resource_request_to_load.setStatus(Constants.Status.LOADED);

            EventBus.getDefault().postSticky(resource_request_to_load);
        }
    }
}
