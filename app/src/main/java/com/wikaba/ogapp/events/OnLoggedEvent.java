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
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.models.ResourceItem;

import java.util.List;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class OnLoggedEvent {
    private boolean _state;
    private OgameAgent _agent;
    private List<FleetEvent> _fleet_events;
    private List<ResourceItem> _resources_information;

    public OnLoggedEvent(boolean state, OgameAgent agent,
                         List<FleetEvent> fleet_events,
                         List<ResourceItem> resources_information) {
        _state = state;
        _agent = agent;
        _fleet_events = fleet_events;
        _resources_information = resources_information;
    }

    public boolean isConnected() {
        return _state;
    }

    public OgameAgent getOgameAgent() {
        return _agent;
    }

    public List<FleetEvent> getFleetEvents() {
        return _fleet_events;
    }

    public List<ResourceItem> getResourceItems() {
        return _resources_information;
    }
}
