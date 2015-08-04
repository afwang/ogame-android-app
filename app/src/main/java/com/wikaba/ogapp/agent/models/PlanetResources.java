package com.wikaba.ogapp.agent.models;

/**
 * Created by kevinleperf on 25/07/15.
 */
public class PlanetResources {
    public Resource metal;
    public Resource crystal;
    public Resource deuterium;

    public PlanetResources() {
        metal = crystal = deuterium = new Resource();
    }
}
