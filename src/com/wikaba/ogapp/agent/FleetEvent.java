/**
 * 
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
 * 		"data-mission-type" attribute.
 * 	data_arrival_time - A long indicating the arrival time of the mission, in Unix
 * 		epoch time. Corresponds to the "data-arrival-time" attribute.
 * 	data_return_flight - Boolean indicating whether this flight is a returning flight.
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
