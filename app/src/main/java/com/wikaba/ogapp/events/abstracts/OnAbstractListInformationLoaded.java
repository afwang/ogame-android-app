package com.wikaba.ogapp.events.abstracts;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.utils.Constants;

import java.util.List;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class OnAbstractListInformationLoaded {
    private OgameAgent _agent;
    private int _type;
    private List<AbstractItemInformation> _item_information;
    private Constants.Status _success;

    public OnAbstractListInformationLoaded(OgameAgent agent,
                                           int type,
                                           List<AbstractItemInformation> list_information,
                                           Constants.Status success) {
        _agent = agent;
        _type = type;
        _item_information = list_information;
        _success = success;
    }

    public int getType() {
        return _type;
    }

    public OgameAgent getOgameAgentSender() {
        return _agent;
    }

    public List<AbstractItemInformation> getListOfItemRetrieved() {
        return _item_information;
    }

    public Constants.Status isSuccess() {
        return _success;
    }

    public void setRetrieved(List<AbstractItemInformation> retrieved) {
        _item_information = retrieved;
    }

    public void setStatus(Constants.Status status) {
        _success = status;
    }

    public Constants.Status getStatus() {
        return _success;
    }
}
