package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;

import java.io.InputStream;

/**
 * Created by kevinleperf on 19/07/15.
 */
public abstract class AbstractParser<T> {
    public abstract T parse(InputStream strea, OgameResources ressources);

    public abstract T parse(String raw, OgameResources ressources);
}
