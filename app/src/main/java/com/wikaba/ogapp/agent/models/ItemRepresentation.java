package com.wikaba.ogapp.agent.models;

import com.wikaba.ogapp.agent.parsers.AbstractParser;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ItemRepresentation {
    private String _page;
    private int _index;
    private int _resource_string;
    private AbstractParser<? extends AbstractItemInformation> _parser;

    private ItemRepresentation() {

    }

    public ItemRepresentation(String page, int index, int resource_string,
                              AbstractParser<? extends AbstractItemInformation> parser) {
        this();
        
        _page = page;
        _index = index;
        _parser = parser;
        _resource_string = resource_string;
    }

    public String getPage() {
        return _page;
    }

    public int getIndex() {
        return _index;
    }

    public int getResourceString() {
        return _resource_string;
    }

    public AbstractParser<? extends AbstractItemInformation> getParser() {
        return _parser;
    }
}
