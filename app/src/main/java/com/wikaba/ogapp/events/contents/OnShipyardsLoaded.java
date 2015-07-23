package com.wikaba.ogapp.events.contents;

import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.events.abstracts.OnAbstractListInformationLoaded;

import java.util.List;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class OnShipyardsLoaded extends OnAbstractListInformationLoaded {
    public OnShipyardsLoaded(OgameAgent agent, List<AbstractItemInformation> list_information, boolean success) {
        super(agent, list_information, success);
    }
}
