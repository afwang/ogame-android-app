package com.wikaba.ogapp.agent.parsers.pages;

import com.wikaba.ogapp.agent.models.ResearchInformation;
import com.wikaba.ogapp.agent.parsers.DefaultItemParser;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ResearchParser extends DefaultItemParser<ResearchInformation> {
    @Override
    protected ResearchInformation createDefaultItemInformationInstance() {
        return new ResearchInformation();
    }
}
