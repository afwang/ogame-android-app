package com.wikaba.ogapp.events.contents;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.events.abstracts.OnAbstractListInformationLoaded;
import com.wikaba.ogapp.utils.Constants;

import java.util.List;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class OnShipyardsLoaded extends OnAbstractListInformationLoaded {
    public OnShipyardsLoaded(OgameAgent agent, int type, List<AbstractItemInformation> list_information, Constants.Status success) {
        super(agent, type, list_information, success);
    }
}
