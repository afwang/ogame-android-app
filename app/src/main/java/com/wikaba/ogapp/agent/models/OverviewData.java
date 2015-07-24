package com.wikaba.ogapp.agent.models;

import java.util.List;

/**
 * Created by kevinleperf on 25/07/15.
 */
public class OverviewData {
    public List<FleetEvent> _fleet_event;

    //class > content-box-s
    //  > content
    //      table
    //          tr.data ? > active
    //              td
    //B                  div?
    //B                      a?
    //B                          onClick? cancelProduction(? > split "," > FIRST > ID
    //B                  span? class level? > trim().split(" ") > 1 > long >> LEVEL
    //
    //S                 div? class shipSumCount?
    //S                     text > long > CARDINAL

    //ID : Countdown
    //in Javascript : getElementByIdWithCache("Countdown") to " > split, get the 2nd splitted item > countdown
    public long _current_building_id;
    public long _current_building_level;
    public int _current_building_countdown;

    public long _current_research_id;
    public long _current_research_level;
    public int _current_research_countdown;

    public long _current_ship_id;
    public long _current_ship_count;
    public int _current_ship_countdown;

    public OverviewData() {
        _fleet_event = null;

        _current_building_id = -1;
        _current_building_level = -1;
        _current_building_countdown = -1;

        _current_research_id = -1;
        _current_research_level = -1;
        _current_research_countdown = -1;

        _current_ship_id = -1;
        _current_ship_count = -1;
        _current_ship_countdown = -1;
    }
}
