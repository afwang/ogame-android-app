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

import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.OgameResources;
import com.wikaga.ogapp.agent.FleetEventListComparator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventParserTest {
	InputStream htmlResponse;

	@Before
	public void readInTestFile() {
		//We will use the Class class's methods for retrieving
		//resource files
		final String filepath = "";
		InputStream input = this.getClass().getResourceAsStream(filepath);
		htmlResponse = new BufferedInputStream(input);
	}

	@Test
	public void testParseEvents() {
		EventParser myParser = new DefaultEventParser();
		OgameResources parsedRes = new OgameResources();
		OgameResources expectedRes = new OgameResources();
		List<FleetEvent> expectedEvents = new ArrayList<FleetEvent>();

		List<FleetEvent> parsedEvents = myParser.parseEvents(htmlResponse, parsedRes);

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
			System.out.println("Caught an IOException:\n" + e);
		}
	}
}
