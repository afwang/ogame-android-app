package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.models.ResearchInformation;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ResearchParser extends DefaultItemParser<ResearchInformation> {
    @Override
    protected ResearchInformation createDefaultItemInformationInstance() {
        return new ResearchInformation();
    }
}
