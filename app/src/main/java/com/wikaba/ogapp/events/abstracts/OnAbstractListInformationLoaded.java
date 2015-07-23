package com.wikaba.ogapp.events.abstracts;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;

import java.util.List;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class OnAbstractListInformationLoaded {
    private OgameAgent _agent;
    private List<AbstractItemInformation> _item_information;
    private boolean _success;

    public OnAbstractListInformationLoaded(OgameAgent agent,
                                           List<AbstractItemInformation> list_information,
                                           boolean success) {
        _agent = agent;
        _item_information = list_information;
        _success = success;
    }

    public OgameAgent getOgameAgentSender() {
        return _agent;
    }

    public List<AbstractItemInformation> getListOfItemRetrieved() {
        return _item_information;
    }

    public boolean isSuccess() {
        return _success;
    }
}
