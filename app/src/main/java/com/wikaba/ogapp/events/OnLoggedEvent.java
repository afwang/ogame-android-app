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
