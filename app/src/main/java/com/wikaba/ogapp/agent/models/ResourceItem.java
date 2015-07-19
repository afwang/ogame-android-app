package com.wikaba.ogapp.agent.models;

import com.wikaba.ogapp.agent.FleetAndResources;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class ResourceItem {
    /**
     * The type of the resource
     * <p/>
     * corresponding to the
     */
    public FleetAndResources.Resources resource_type;
    public long resource_production;
    public String resource_consumption;

    public ResourceItem(FleetAndResources.Resources resource) {
        resource_type = resource;
    }

    @Override
    public String toString() {
        return resource_type + " " + resource_production + " " + resource_consumption;
    }
}
