package com.wikaba.ogapp.events;

import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.OgameAgent;

import java.util.List;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class OnLoggedEvent {
    private boolean _state;
    private OgameAgent _agent;
    private List<FleetEvent> _fleet_events;

    public OnLoggedEvent(boolean state, OgameAgent agent, List<FleetEvent> fleet_events) {
        _state = state;
        _agent = agent;
        _fleet_events = fleet_events;
    }

    public boolean isConnected() {
        return _state;
    }

    public OgameAgent getOgameAgent() {
        return _agent;
    }

    public List<FleetEvent> getFleetEvents(){
        return _fleet_events;
    }
}
