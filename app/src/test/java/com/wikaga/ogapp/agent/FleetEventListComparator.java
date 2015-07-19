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

package com.wikaga.ogapp.agent;

import com.wikaba.ogapp.agent.models.FleetEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FleetEventListComparator implements Comparator<List<FleetEvent>> {
	@Override
	public int compare(List<FleetEvent> l1, List<FleetEvent> l2) {
		if(l1 == null || l2 == null) {
			if(l1 != l2) {
				//One, and only one, of the two list references are not null
				//Treat null as the "lesser" value
				return l1 == null ? -1 : 1;
			}
			else {
				return 0;
			}
		}

		int size1 = l1.size();
		int size2 = l2.size();
		if(size1 != size2) {
			return size2 - size1;
		}

		long difference = 0;
		Iterator<FleetEvent> iter1 = l1.iterator();
		Iterator<FleetEvent> iter2 = l2.iterator();
		while(iter1.hasNext()) {
			FleetEvent f1 = iter1.next();
			FleetEvent f2 = iter2.next();
			difference = f1.coordsOrigin.compareTo(f2.coordsOrigin);
			if(difference != 0) {
				break;
			}

			difference = f1.data_arrival_time - f2.data_arrival_time;
			if(difference != 0) {
				break;
			}

			difference = f1.data_mission_type - f2.data_mission_type;
			if(difference != 0) {
				break;
			}

			if(f1.data_return_flight != f2.data_return_flight) {
				difference = f1.data_return_flight ? 1 : 0;
			}

			if(difference != 0) {
				break;
			}

			difference = f1.destCoords.compareTo(f2.destCoords);
			if(difference != 0) {
				break;
			}

			difference = f1.destFleet.compareTo(f2.destFleet);
			if(difference != 0) {
				break;
			}

			difference = f1.originFleet.compareTo(f2.destFleet);
			if(difference != 0) {
				break;
			}

			difference = compareFleetResources(f1.fleetResources, f2.fleetResources);
			if(difference != 0) {
				break;
			}
		}

		return (int)difference;
	}

	private int compareFleetResources(Map<String, Long> m1, Map<String, Long> m2) {
		if(m1 == null || m2 == null) {
			if(m1 != m2) {
				return m1 == null ? -1 : 1;
			}
			else {
				return 0;
			}
		}

		int size1 = m1.size();
		int size2 = m2.size();
		if(size1 != size2) {
			return size2 - size1;
		}

		long difference = 0;
		Iterator<String> keyIter = m1.keySet().iterator();
		while(keyIter.hasNext()) {
			String key1 = keyIter.next();
			long v1 = m1.get(key1);
			Long val2 = m2.get(key1);
			if(val2 == null) {
				return 1;
			}
			long v2 = val2;
			if(v1 != v2) {
				difference = v2 - v1;
				break;
			}
			m2.remove(key1);
		}
		return (int)difference;
	}
}
