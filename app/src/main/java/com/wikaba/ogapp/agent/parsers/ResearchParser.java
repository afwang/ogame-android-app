package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.BuildingInformation;

import java.io.InputStream;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ResearchParser extends AbstractParser<BuildingInformation> {
    @Override
    public BuildingInformation parse(InputStream stream, OgameResources resources) {
        return parse(consumeStream(stream), resources);
    }

    @Override
    public BuildingInformation parse(String raw, OgameResources resources) {
        return null;
    }
}
