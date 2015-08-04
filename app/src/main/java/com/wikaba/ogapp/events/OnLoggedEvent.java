/*
	Copyright 2015 Kevin Le Perf

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

package com.wikaba.ogapp.events;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.agent.models.PlanetResources;
import com.wikaba.ogapp.utils.AccountCredentials;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class OnLoggedEvent {
    private boolean _state;
    private OgameAgent _agent;
    private OverviewData _events;
    private PlanetResources _resources_information;
    private AccountCredentials _credentials;

    public OnLoggedEvent(boolean state, AccountCredentials credentials, OgameAgent agent,
                         OverviewData events,
                         PlanetResources resources_information) {
        _credentials = credentials;
        _state = state;
        _agent = agent;
        _events = events;
        _resources_information = resources_information;
    }

    public boolean isConnected() {
        return _state;
    }

    public OgameAgent getOgameAgent() {
        return _agent;
    }

    public OverviewData getOverviewData() {
        return _events;
    }

    public PlanetResources getPlanetResources() {
        return _resources_information;
    }

    public AccountCredentials getCredentials() {
        return _credentials;
    }
}
