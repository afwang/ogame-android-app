package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.models.ShipInformation;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ShipyardParser extends DefaultItemParser<ShipInformation> {
    @Override
    protected ShipInformation createDefaultItemInformationInstance() {
        return new ShipInformation();
    }
}