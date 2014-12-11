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


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 * @author afwang
 *
 */
public class OgameAgent {
	public static final String ogameBaseUrl = "http://en.ogame.gameforge.com/";
	public static final String LOGIN_URL = ogameBaseUrl + "main/login";
	public static final String OVERVIEW_ENDPOINT = "/game/index.php?page=overview";
	public static final String ogameEventDataUrlEnd = "/game/index.php?page=eventList";
	
	private Map<String, String> cookieStore;
	private String serverUri;
	
	public OgameAgent() {
		cookieStore = new HashMap<String, String>();
	}
	
	/**
	 * Submits user credentials to Ogame. Parses and returns data from HTTP response
	 * @param universe - The name of the universe to log in to.
	 * @param username - Username of the account on universe to log in to.
	 * @return list of cookies set for this session.
	 */
	public Map<String, String> login(String universe, String username, String password)
	{
		final int timeoutMillis = 30 * 1000;
		
		Connection.Response ogameOverviewResponse;
		try
		{
			serverUri = NameToURI.getDomain(universe);
			
			// Get overview Page
			ogameOverviewResponse = Jsoup.connect(LOGIN_URL)
					.data("kid", "")
					.data("uni", serverUri)
					.data("login", username)
					.data("pass", password)
					.timeout(timeoutMillis)
					.method(Connection.Method.POST)
					.execute();

			if(connectionSuccessful(ogameOverviewResponse.statusCode()))
			{
				// return account cookies
				cookieStore = ogameOverviewResponse.cookies();
				System.out.println("Logged in successfully.");

				return cookieStore;
			}
			
			throw new Exception("Could not login.");
		}
		catch(java.io.EOFException e)
		{
			// attempt to retry method
			System.out.println("Caught EOFException, retrying login ");
			return this.login(universe, username, password);
		}
		catch (Exception e)
		{
			System.out.print("Couldn't login and see overview page: " + e);
//			throw e;
		}

		return null;
	}
	
	/**
	 * Emulate a user clicking the "Overview" link in the navigation bar/column. Also parse fleet
	 * movement data from the returned response.
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * 		is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * 		will have non-null instance variables.
	 */
	public List<FleetEvent> getOverviewData() throws LoggedOutException
	{
		String ogameEventDataUrl = "http://" + serverUri + ogameEventDataUrlEnd;
		Connection.Response eventDataResponse;
		try
		{
			eventDataResponse = Jsoup.connect(ogameEventDataUrl)
					.cookies(cookieStore)
					.method(Connection.Method.GET)
					.execute();
			
			if(connectionSuccessful(eventDataResponse.statusCode()))
			{
				System.out.println(eventDataResponse.parse().getElementsByClass("eventFleet").size());
				return parseFleetAndResData(eventDataResponse.parse().getElementsByClass("eventFleet"));
			}
			
			// if the connection didn't work
			throw new Exception("Couldn't connect to eventDataUrl.");
		}
		catch (Exception e)
		{
			System.out.print(e);
//			throw e;
		}
		return null;
	}
	
	private static List<FleetEvent> parseFleetAndResData(Elements eventFleets)
	{
		System.out.println("parseFleetAndResData called.");
		List<FleetEvent> eventList = new LinkedList<FleetEvent>();
		
		for(int i=0;i<eventFleets.size();i++)
		{
			FleetEvent thisEvent = new FleetEvent();
			Element thisEventRow = eventFleets.get(i);
			
			// Get tr attributes like the mission type, return flight, or arrival time
			for(org.jsoup.nodes.Attribute thisAttribute : thisEventRow.attributes())
			{
				String key = thisAttribute.getKey();
				String value = thisAttribute.getValue();
				
				if(key.equals("data-mission-type")) {
					thisEvent.data_mission_type = Integer.valueOf(value);
				}
				else if(key.equals("data-return-flight")) {
					thisEvent.data_return_flight = Boolean.valueOf(value);
				}
				else if(key.equals("data-arrival-time")) {
					thisEvent.data_arrival_time = Long.valueOf(value);
				}
			}

			// Get rest of information from the td's
			for(Element thisElement : thisEventRow.getElementsByTag("td"))
			{
				String tdClass = thisElement.className();
				
				if(tdClass.equals("coordsOrigin"))
				{
					thisEvent.coordsOrigin = thisElement.text();
				}
				else if(tdClass.equals("destFleet")) {
					thisEvent.destFleet = thisElement.text();
				}
				else if(tdClass.equals("destCoords")) {
					thisEvent.destCoords = thisElement.text();
				}
				else if(tdClass.equals("originFleet")) {
					thisEvent.originFleet = thisElement.text();
				}
				else if(tdClass.equals("icon_movement") || tdClass.equals("icon_movement_reserve")) {

					// This will get all tr elements in the fleetDataDiv, if a tr is not part of the fleet data we'll need to use the more specific longer way above
					Elements fleetData = Jsoup.parseBodyFragment(thisElement.getElementsByTag("span").attr("title")).body().select("tr");

					// Loop through tr's found
					for (Element shipOrResource : fleetData)
					{
						String fleetInfoText = shipOrResource.getElementsByTag("td").text().trim();
						
						if(fleetInfoText.isEmpty() || fleetInfoText.length() <= 7)
						{
							continue;
						}

						String[] fleetInfo = fleetInfoText.split(":");
						fleetInfo[0] = fleetInfo[0].trim();
						fleetInfo[1] = fleetInfo[1].trim();

						// Loop through FleetAndResources.class fields and if we find a match for a tr's text add an entry to the fleetRresources
						try
						{
							FleetAndResources instance = new FleetAndResources();
							for(Field field : FleetAndResources.class.getFields())
							{
								String fleetOrResourceValue = FleetAndResources.class.getField(field.getName()).get(instance).toString();
								if(fleetOrResourceValue.equals(fleetInfo[0]))
								{
									// TODO: Is it possible we might have multiple fleets?
									thisEvent.fleetResources.put(fleetOrResourceValue, Long.valueOf(fleetInfo[1]));
								}
							}
						}
						catch (NoSuchFieldException nsfe)
						{
							System.out.println("Caught NoSuchFieldException: " + nsfe);
						}
						catch (IllegalAccessException iae)
						{
							System.out.println("Caught IllegalAccessException: " + iae);
						}
					}
				}
				else if(tdClass.equals("detailsFleet")) {
					System.out.println("Found a detailsFleet, not sure if I need to look at it or not.");
				}
			}
			
			eventList.add(thisEvent);
		}
		System.out.println("Done Parsing events: " + eventList.size());
		return eventList;
	}

	private static boolean connectionSuccessful(int statusCode)
	{
		if(statusCode == 200 || (statusCode >= 300 && statusCode < 400))
		{
			return true;
		}
		
		System.out.println("connection not successful");
		return false;
	}
}
