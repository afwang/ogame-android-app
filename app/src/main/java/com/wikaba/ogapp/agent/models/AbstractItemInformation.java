package com.wikaba.ogapp.agent.models;

import com.wikaba.ogapp.utils.Constants;

/**
 * Created by kevinleperf on 22/07/15.
 */
public abstract class AbstractItemInformation {
    private ItemRepresentation _current_representation;
    private long _duration;
    private long _current_cardinal;
    private long _metal_cost;
    private long _crystal_cost;
    private long _deuterium_cost;
    private Constants.Buildable _buildable;
    private long _possible_in_seconds;

    public void setDuration(long duration) {
        _duration = duration;
    }

    public long getDuration() {
        return _duration;
    }

    public void setItemRepresentation(ItemRepresentation representation) {
        _current_representation = representation;
    }

    public ItemRepresentation getItemRepresentation() {
        return _current_representation;
    }

    public void setMetalCost(long metal_cost) {
        _metal_cost = metal_cost;
    }

    public void setCrystalCost(long crystal_cost) {
        _crystal_cost = crystal_cost;
    }

    public void setDeuteriumCost(long deuteriumCost) {
        _deuterium_cost = deuteriumCost;
    }

    public long getMetalCost() {
        return _metal_cost;
    }

    public long getCrystalCost() {
        return _crystal_cost;
    }

    public long getDeuteriumCost() {
        return _deuterium_cost;
    }

    public void setBuildable(Constants.Buildable buildable) {
        _buildable = buildable;
    }

    public Constants.Buildable getBuildableStatus() {
        return _buildable;
    }

    public void setCurrentCardinal(long currentCardinal) {
        _current_cardinal = currentCardinal;
    }

    public long getLevelOrCount() {
        return _current_cardinal;
    }

    public void setPossibleInSeconds(long possible_in_seconds) {
        _possible_in_seconds = possible_in_seconds;
    }

    public long getPossibleInSeconds() {
        return _possible_in_seconds;
    }

    public abstract boolean canBeUpgraded();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " :: " + getLevelOrCount() + " // " + getDuration()
                + " " + getMetalCost() + "m/" + getCrystalCost() + "c/" + getDeuteriumCost() + "d";
    }
}
