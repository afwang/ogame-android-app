/*
    Copyright 2014 Alexander Wang
    
    This file is part of Ogame on Android.

    Ogame on Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Ogame on Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.wikaba.ogapp.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple container class to hold all the details of a particular fleet event.
 * For consistency with the HTML response from the Ogame servers, the instance
 * variables have the same names as the attribute names for the event in the HTML.
 * Some variables can not be translated nicely into Java (for example, the attribute
 * "data-mission-type"); for these variables, this document should explicitly state which
 * attribute the variable corresponds to (for example, data_mission_type will correspond
 * with "data-mission-type").
 * 
 * Explanation of instance variables:
 * 	originFleet - name of the originating planet this fleet is from.
 * 	coordsOrigin - coordinates of the originating planet
 * 	destFleet - name of the destination planet this fleet is headed towards
 * 	destCoords - coordinates of the destination planet
 * 	data_mission_type - An int indicating the type of mission. Corresponds to the
 * 		"data-mission-type" attribute. See IntegerMissionMap for relation between
 * 		integers and missions.
 * 	data_arrival_time - A long indicating the arrival time of the mission, in Unix
 * 		epoch time. Corresponds to the "data-arrival-time" attribute.
 * 	data_return_flight - Boolean indicating whether this flight is a returning flight.
 * 		True if the flight is returning. False if the flight is outgoing.
 * 	fleetResources - A map from Strings to Longs. Key strings are names of ships or
 * 		name of resource. The values are the numbers of the ship or resource named
 * 		as the key. Key Strings are used from the contract class FleetAndResources.
 * 
 * @author afwang
 *
 */
public class FleetEvent {
	public int data_mission_type;
	public boolean data_return_flight;
	public long data_arrival_time;
	public String originFleet;
	public String coordsOrigin;
	public String destFleet;
	public String destCoords;
	public Map<String, Long> fleetResources;
	
	public FleetEvent() {
		fleetResources = new HashMap<String, Long>();
	}
}
