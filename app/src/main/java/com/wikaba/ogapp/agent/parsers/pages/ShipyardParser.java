package com.wikaba.ogapp.agent.parsers.pages;

import com.wikaba.ogapp.agent.models.ShipInformation;
import com.wikaba.ogapp.agent.parsers.DefaultItemParser;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ShipyardParser extends DefaultItemParser<ShipInformation> {
    @Override
    protected ShipInformation createDefaultItemInformationInstance() {
        return new ShipInformation();
    }
}