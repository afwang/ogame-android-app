package com.wikaba.ogapp.agent.models;

/**
 * Created by kevinleperf on 25/07/15.
 */
public class Resource {
    public long current_quantity;
    public long current_production;
    public long storage_capacity;
    public long stash_quantity;

    public Resource() {
        current_production = current_quantity = storage_capacity = stash_quantity = -1;
    }
}
