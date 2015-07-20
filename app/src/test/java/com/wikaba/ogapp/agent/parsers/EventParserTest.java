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

package com.wikaba.ogapp.agent.parsers;

import com.google.gson.Gson;
import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.FleetEvent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class EventParserTest {
	private static final Logger logger = LoggerFactory.getLogger(EventParserTest.class);

	InputStream htmlResponse;
	List<FleetEvent> expectedEvents;

	@Before
	public void readInTestFile() {
		//We will use the Class class's methods for retrieving
		//resource files
		final String filepath = "lots_of_events.html";
		InputStream input = this.getClass().getResourceAsStream(filepath);
		htmlResponse = new BufferedInputStream(input);

		expectedEvents = buildExpectedEventsList();
	}

	@Test
	public void testParseEvents() {
		FleetEventParser myParser = new FleetEventParser();
		OgameResources parsedRes = new OgameResources();
		OgameResources expectedRes = new OgameResources();
		List<FleetEvent> expectedEvents = new ArrayList<FleetEvent>();

		List<FleetEvent> parsedEvents = myParser.parse(htmlResponse, parsedRes);

		Comparator<List<FleetEvent>> listComparator = new FleetEventListComparator();
		Assert.assertEquals(listComparator.compare(expectedEvents, parsedEvents), 0);
		Assert.assertEquals(expectedRes.getMetal(), parsedRes.getMetal());
		Assert.assertEquals(expectedRes.getCrystal(), parsedRes.getCrystal());
		Assert.assertEquals(expectedRes.getDeut(), parsedRes.getDeut());
		Assert.assertEquals(expectedRes.getAvailEnergy(), parsedRes.getAvailEnergy());
		Assert.assertEquals(expectedRes.getMaxEnergy(), parsedRes.getMaxEnergy());
	}

	@After
	public void closeResources() {
		try {
			if(htmlResponse != null) {
				htmlResponse.close();
			}
		}
		catch(IOException e) {
			logger.error("Caught an IOException", e);
		}
	}

	private List<FleetEvent> buildExpectedEventsList() {
		Gson gson = new Gson();
		final String expectedResultsPath = "expectedEventsList.json";
		InputStream expectedResultsIn = this.getClass().getResourceAsStream(expectedResultsPath);
		BufferedReader jsonInput = new BufferedReader(
			new InputStreamReader(expectedResultsIn));
		EventsList expectedEvents = gson.fromJson(jsonInput, EventsList.class);
		return Arrays.asList(expectedEvents.events);
	}
}
