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

package com.wikaba.ogapp.loaders;

import android.support.test.runner.AndroidJUnit4;

import com.wikaba.ogapp.AgentService;
import com.wikaba.ogapp.HomeActivity;
import com.wikaba.ogapp.agent.FleetAndResources;
import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.IntegerMissionMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class FleetEventLoaderTest {
	HomeActivity act;
	List<FleetEvent> startingList;

	@Before
	public void setUpActivity() {
		act = Mockito.mock(HomeActivity.class);
		AgentService service = Mockito.mock(AgentService.class);
		Mockito.when(act.getAgentService()).thenReturn(service);
		Mockito.when(act.getAccountRowId()).thenReturn(Long.valueOf(1));
		List<FleetEvent> myEventList = new ArrayList<FleetEvent>();
		FleetEvent myEvent = new FleetEvent();
		myEvent.coordsOrigin = "1:1:1";
		myEvent.data_arrival_time = 1000;
		myEvent.data_mission_type = IntegerMissionMap.ATTACK;
		myEvent.data_return_flight = true;
		myEvent.destCoords = "1:1:2";
		myEvent.destFleet = "Destination planet";
		myEvent.originFleet = "Origin planet";
		Map<String, Long> fleetResources = myEvent.fleetResources;
		fleetResources.put(FleetAndResources.LF, 1000L);
		fleetResources.put(FleetAndResources.METAL, 500L);
		myEventList.add(myEvent);
		Mockito.when(service.getFleetEvents(1)).thenReturn(myEventList);
		startingList = myEventList;
	}

	@Test
	public void testLoadInBackground() {
		FleetEventLoader loader = new FleetEventLoader(act);
		List<FleetEvent> returnedList = loader.loadInBackground();
	}

	private boolean areListsEqual(List<FleetEvent> l1, List<FleetEvent> l2) {
		if(l1 == null || l2 == null) {
			return l1 == l2;
		}

		if(l1.size() != l2.size()) {
			return false;
		}

		FleetEvent cursor1;
		FleetEvent cursor2;
		Iterator<FleetEvent> iter1 = l1.iterator();
		Iterator<FleetEvent> iter2 = l2.iterator();
		while(iter1.hasNext()) {
			cursor1 = iter1.next();
			cursor2 = iter2.next();
			boolean areEventsEqual =
				cursor1.coordsOrigin.equals(cursor2.coordsOrigin)
				&& cursor1.data_arrival_time == cursor2.data_arrival_time
				&& cursor1.data_mission_type == cursor2.data_mission_type
				&& cursor1.data_return_flight == cursor2.data_return_flight
				&& cursor1.destCoords.equals(cursor2.destCoords)
				&& cursor1.destFleet.equals(cursor2.destFleet)
				&& cursor1.originFleet.equals(cursor2.originFleet);
			if(areEventsEqual) {
				areEventsEqual = areMapsEqual(cursor1.fleetResources, cursor2.fleetResources);
			}

			if(!areEventsEqual) {
				return false;
			}
		}
		return true;
	}

	private boolean areMapsEqual(Map<String, Long> m1, Map<String, Long> m2) {
		if(m1 == null || m2 == null) {
			return m1 == m2;
		}

		if(m1.size() != m2.size()) {
			return false;
		}

		Iterator<String> keyIter = m1.keySet().iterator();
		//To check the maps, we first check to ensure that
		//every relationship in m1 is in m2. During this
		//process we will be removing verified relationships
		//from m2. At the end, m2 should be an empty set.
		//If not, that means m2 contains a relationship that
		//m1 did not contain (and we will return false
		while(keyIter.hasNext()) {
			String key1 = keyIter.next();
			long v1 = m1.get(key1);
			Long val2 = m2.get(key1);
			if(val2 == null) {
				return false;
			}
			long v2 = val2;
			if(v1 != v2) {
				return false;
			}
			m2.remove(key1);
		}
		return m2.size() != 0;
	}
}
