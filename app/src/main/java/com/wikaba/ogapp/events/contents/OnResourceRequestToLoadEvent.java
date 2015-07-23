package com.wikaba.ogapp.events.contents;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.constants.ItemRepresentationConstant;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class OnResourceRequestToLoadEvent {
    private OgameAgent _agent;
    private int _requested;
    private ItemRepresentationConstant _content;

    public OnResourceRequestToLoadEvent(OgameAgent agent, int requested,
                                        ItemRepresentationConstant content) {
        _agent = agent;
        _requested = requested;
        _content = content;
    }

    public OgameAgent getOgameAgent() {
        return _agent;
    }

    public int getRequested() {
        return _requested;
    }

    public ItemRepresentationConstant getContent() {
        return _content;
    }
}
