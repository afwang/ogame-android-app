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

package com.wikaba.ogapp.agent.models;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Simple container class to hold all the details of a particular fleet event.
 * For consistency with the HTML response from the Ogame servers, the instance
 * variables have the same names as the attribute names for the event in the HTML.
 * Some variables can not be translated nicely into Java (for example, the attribute
 * "data-mission-type"); for these variables, this document should explicitly state which
 * attribute the variable corresponds to (for example, data_mission_type will correspond
 * with "data-mission-type").</p>
 * <p/>
 * <p>Explanation of instance variables:</p>
 * <ul>
 * <li>originFleet - name of the originating planet this fleet is from.</li>
 * <li>coordsOrigin - coordinates of the originating planet</li>
 * <li>destFleet - name of the destination planet this fleet is headed towards</li>
 * <li>destCoords - coordinates of the destination planet</li>
 * <li>data_mission_type - An int indicating the type of mission. Corresponds to the
 * "data-mission-type" attribute. See IntegerMissionMap for relation between
 * integers and missions.</li>
 * <li>data_arrival_time - A long indicating the arrival time of the mission, in Unix
 * epoch time. Corresponds to the "data-arrival-time" attribute.</li>
 * <li>data_return_flight - Boolean indicating whether this flight is a returning flight.
 * True if the flight is returning. False if the flight is outgoing.</li>
 * <li>fleetResources - A map from Strings to Longs. Key strings are names of ships or
 * name of resource. The values are the numbers of the ship or resource named
 * as the key. Key Strings are used from the contract class
 * {@link com.wikaba.ogapp.agent.FleetAndResources}.</li>
 * </ul>
 *
 * @author afwang
 */
public class FleetEvent implements Comparable<FleetEvent> {
    public int data_mission_type;
    public boolean data_return_flight;
    public long data_arrival_time;
    public String originFleet;
    public LinkHTML coordsOrigin;
    public String destFleet;
    public LinkHTML destCoords;
    public Map<String, Long> fleet;
    public FleetResources resources;

    public FleetEvent() {
        fleet = new HashMap<String, Long>();
        resources = new FleetResources();

        originFleet = "";
        //coordsOrigin = new LinkHTML();
        destFleet = "";
        //destCoords = new LinkHTML();
    }

    @Override
    public String toString() {
        return data_mission_type + " " + data_return_flight + " " + data_arrival_time + " "
                + originFleet + " " + coordsOrigin + " " + destFleet + " " +
                destCoords + " " + fleet + " " + resources;
    }

    @Override
    public int compareTo(FleetEvent another) {
        int difference = 0;
        try {
            difference = coordsOrigin.compareTo(another.coordsOrigin);
            if (difference != 0) {
                return difference;
            }

            difference = (int) (data_arrival_time - another.data_arrival_time);
            if (difference != 0) {
                return difference;
            }

            difference = data_mission_type - another.data_mission_type;
            if (difference != 0) {
                return difference;
            }

            if (data_return_flight != another.data_return_flight) {
                if (data_return_flight) {
                    return 1;
                }
            }

            difference = destCoords.compareTo(another.destCoords);
            if (difference != 0) {
                return difference;
            }

            difference = destFleet.compareTo(another.destFleet);
            if (difference != 0) {
                return difference;
            }

            difference = originFleet.compareTo(another.originFleet);
            if (difference != 0) {
                return difference;
            }

            //TODO compare resources
        } catch (Exception exception) {
            exception.printStackTrace();
            difference = 0;
        }
        return difference;

    }
}
