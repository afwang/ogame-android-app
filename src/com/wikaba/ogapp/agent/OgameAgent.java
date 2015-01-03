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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 * @author afwang
 *
 */
public class OgameAgent {
	public static final String LOGIN_URL = "http://en.ogame.gameforge.com/main/login";
	public static final String OVERVIEW_ENDPOINT = "/game/index.php?page=overview";
	public static final String EVENTLIST_ENDPOINT = "/game/index.php?page=eventList&ajax=1";
	
	private String serverUri;
	
	public OgameAgent(String universe) {
		serverUri = "http://" + NameToURI.getDomain(universe);
	}
	
	/**
	 * Submits user credentials to Ogame to obtain set of cookies (handled
	 * by system's CookieHandler.
	 * 
	 * This method's purpose is to add the cookies into the current
	 * session's CookieStore
	 * @param universe - Universe of the account
	 * @param username - Username of the account
	 * @param password - Password of the account
	 * @return true on successful login, false on failure
	 */
	public boolean login(String universe, String username, String password) {
		final int timeoutMillis = 30 * 1000;
		HttpURLConnection connection = null;
		int response = 0;
		universe = NameToURI.getDomain(universe);
		boolean successfulResponse;
		
		/*
		 * FIRST REQUEST
		 */
		System.out.println("START FIRST REQUEST (login)");
		String uri = LOGIN_URL;
		String parameters;
		try {
			parameters = "kid=&uni=" + URLEncoder.encode(universe, "UTF-8") + "&login=" + URLEncoder.encode(username, "UTF-8") + "&pass=" + URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("Error: " + e1 + '\n' + e1.getMessage());
			e1.printStackTrace();
			return false;
		}
		String length = Integer.toString(parameters.length());
		try {
			for(boolean redo = true; redo;) {
				try {
					connection = (HttpURLConnection)(new URL(uri)).openConnection();
					connection.setConnectTimeout(timeoutMillis);
					connection.setRequestProperty("Accept-Language", "en-US");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setRequestProperty("Content-Length", length);
					connection.setDoOutput(true);
					connection.setRequestMethod("POST");
					Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
					writer.write(parameters);
					writer.flush();
					writer.close();
					connection.setInstanceFollowRedirects(false);
					connection.connect();
					response = connection.getResponseCode();
					redo = false;
				}
				catch(java.io.EOFException e) {
					//Catch annoying server-side issue sending faulty HTTP responses.
					//Just force a retry.
					redo = true;
				}
			}
				
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				successfulResponse = true;
				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
				}
			}
			else {
				successfulResponse = false;
				System.err.println("Something went wrong!");
				BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while((line = errReader.readLine()) != null) {
					System.err.println(line);
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END FIRST REQUEST (login)");
			
			if(!successfulResponse) {
				return false;
			}
			
			/*
			 * SECOND REQUEST
			 */
			System.out.println("START SECOND REQUEST");
			connection = (HttpURLConnection)(new URL(uri)).openConnection();
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setDoOutput(false);
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			
			connection.connect();
			
			response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				successfulResponse = true;
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
				}
			}
			else {
				successfulResponse = false;
				System.err.println("Something went wrong!");
				BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while((line = errReader.readLine()) != null) {
					System.err.println(line);
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END SECOND REQUEST");
			if(!successfulResponse) {
				return false;
			}
			
			/*
			 * THIRD REQUEST (final request)
			 */
			for(boolean redo = true; redo; ) {
				try {
					System.out.println("START THIRD REQUEST");
					connection = (HttpURLConnection)(new URL(uri)).openConnection();
					connection.setConnectTimeout(timeoutMillis);
					connection.setRequestProperty("Accept-Language", "en-US");
					connection.setRequestProperty("Accept", "text/html");					
					connection.setDoOutput(false);
					connection.setDoInput(true);
					connection.setRequestMethod("GET");
					connection.setInstanceFollowRedirects(false);
					
					connection.connect();
					
					response = connection.getResponseCode();
					redo = false;
				}
				catch(java.io.EOFException e) {
					//Again, to avoid the weird EOFException bug. Goddamn Ogame's programming!
					redo = true;
				}
			}
			//The last response must receive a status 200. If it isn't, we did something wrong.
			if(response == HttpURLConnection.HTTP_OK) {
				successfulResponse = true;
				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((reader.readLine()) != null);
				reader.close();
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
				}
			}
			else {
				successfulResponse = false;
				System.err.println("Something went wrong!");
				BufferedReader errReader;
				try {
					errReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				}
				catch(IOException e) {
					errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				}
				String line;
				while((line = errReader.readLine()) != null) {
					System.err.println(line);
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END THIRD REQUEST");
			if(!successfulResponse) {
				return false;
			}
		}
		catch(MalformedURLException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
			return false;
		}
		catch(IOException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			connection.disconnect();
		}
		
		return true;
	}
	
	/**
	 * Emulate a user clicking the "Overview" link in the navigation bar/column. Also parse fleet
	 * movement data from the returned response (if available).
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * 		is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * 		will have non-null instance variables.
	 */
	public List<FleetEvent> getOverviewData() throws LoggedOutException {
		List<FleetEvent> overviewData = null;
		
		HttpURLConnection conn = null;
		String connectionUriStr = serverUri + OVERVIEW_ENDPOINT;
		
		try {
			URL connectionUrl = new URL(connectionUriStr);
			conn = (HttpURLConnection)connectionUrl.openConnection();
		}
		catch(IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		InputStream responseStream = null;
		try {
			conn.setInstanceFollowRedirects(false);
			
			int responseCode = makeGETReq(conn);

			if(responseCode < 0) {
				return null;
			}
			
			boolean isError;
			if(responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
				responseStream = conn.getErrorStream();
				isError = true;
			}
			else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				throw new LoggedOutException("Agent's cookies are no longer valid");
			}
			else {
				if(responseCode == HttpURLConnection.HTTP_OK) {
					isError = false;
				}
				else {
					isError = true;
				}
				responseStream = conn.getInputStream();
			}
			
			if(isError) {
				BufferedReader isr = new BufferedReader(new InputStreamReader(responseStream));
				String line;
				while((line = isr.readLine()) != null) {
					System.err.println(line);
				}
				isr.close();
			}
			else {
				List<FleetEvent> events = parseEvents(responseStream);
				overviewData = events;
			}
		}
		catch(IOException e) {
			System.err.println("Could not read response stream");
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(conn != null) {
				conn.disconnect();
			}
			if(responseStream != null) {
				try {
					responseStream.close();
				}
				catch(IOException e) {
				}
			}
		}
		
		if(overviewData != null && overviewData.size() == 0) {
			overviewData = getFleetEvents();
		}
		
		return overviewData;
	}
	
	/**
	 * Parse fleet movement data from the returned response from EVENTLIST_ENDPOINT.
	 * 
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * 		is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * 		will have non-null instance variables.
	 */
	public List<FleetEvent> getFleetEvents() throws LoggedOutException {
		HttpURLConnection conn = null;
		InputStream responseStream = null;
		List<FleetEvent> events = null;
		
		try {
			URL connection = new URL(serverUri + EVENTLIST_ENDPOINT);
			conn = (HttpURLConnection)connection.openConnection();
			
			conn.setInstanceFollowRedirects(false);
			
			int responseCode = makeGETReq(conn);

			if(responseCode < 0) {
				return null;
			}
			
			boolean isError;
			if(responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
				responseStream = conn.getErrorStream();
				isError = true;
			}
			else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				throw new LoggedOutException("Agent's cookies are no longer valid");
			}
			else {
				if(responseCode == HttpURLConnection.HTTP_OK) {
					isError = false;
				}
				else {
					isError = true;
				}
				responseStream = conn.getInputStream();
			}
			
			if(isError) {
				BufferedReader isr = new BufferedReader(new InputStreamReader(responseStream));
				String line;
				while((line = isr.readLine()) != null) {
					System.err.println(line);
				}
				isr.close();
			}
			else {
				events = parseEvents(responseStream);
			}
		}
		catch (IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(conn != null) {
				conn.disconnect();
			}
			if(responseStream != null) {
				try {
					responseStream.close();
				}
				catch(IOException e) {
				}
			}
		}
		return events;
	}
	
	/**
	 * Sets the required headers and cookies for the request specified by connection.
	 * Connects to the URL specified by connection.
	 * 
	 * Pre-condition: Parameter connection must be a valid connection request, and it 
	 * should not have had connect() called on it.
	 * 
	 * Post-condition: Parameter connection now has either an input or error stream to read from.
	 * Use the return value of this method to determine which stream to read from. Method
	 * disconnect() has also not been called on connection, so the caller of this method should
	 * handle calling disconnect on the connection.
	 * 
	 * @param connection - an HttpURLConnection to connect to.
	 * @return the HTTP response's status code if a connection was made. -1 if setting up the connection failed
	 * 	-2 if the connection could not be made.
	 */
	private int makeGETReq(HttpURLConnection connection) {
		int responseCode = 0;
		
		try {
			connection.setRequestProperty("Accept-Language", "en-US");
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			
			connection.connect();
			responseCode = connection.getResponseCode();
		}
		catch(ProtocolException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			responseCode = -1;
		}
		catch(IOException e) {
			System.err.println("Could not open connection");
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			responseCode = -2;
		}
		
		return responseCode;
	}

	/**
	 * Uses JSoup to parse an HTML file presented as an InputStream for fleet events.
	 * 
	 * Pre-condition: inputStream must not be closed.
	 * 
	 * Post-condition: inputStream remains unclosed. The caller must close it themselves.
	 * 
	 * @param inputStream - Input stream from HttpURLConnection after a successful connection.
	 * 	inputStream contains the HTML response from the Ogame server.
	 * @return list of mission events parsed from HTML InputStream. Returns empty list if no events.
	 * 	Null on error.
	 */
	private List<FleetEvent> parseEvents(InputStream inputStream) {
		
		String response = "";
		try {
			StringBuilder strb = new StringBuilder();
			char[] buffer = new char[1024];
			InputStreamReader isr = new InputStreamReader(inputStream);
			while((isr.read(buffer)) > 0) {
				strb.append(buffer);
			}
			response = strb.toString().replaceAll("&(?![A-Za-z]+;)", "&amp;");
//			strb = new StringBuilder(response);
			//Removing javascript:
//			removeSection(strb, "<!-- JAVASCRIPT -->", "<!-- END JAVASCRIPT -->");
//			removeSection(strb, "<!-- #MMO:NETBAR# -->", "</script>");
//			removeSection(strb, "<!-- Start Alexa Certify Javascript -->", "</script>");
//			removeSection(strb, "The relocation allows you to move your planets", "deactivated for 24 hours.");
//			removeSection(strb, "<div id=\"mmonetbar\" class=\"mmoogame\">", "</script>");
//			response = strb.toString();
//			System.out.println(response);
		}
		catch(IOException e) {
			System.err.println("Error reading the response: " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}

		List<FleetEvent> eventList = new LinkedList<FleetEvent>();
		
		//TODO: Need to submit URI for base URI
		Document doc = Jsoup.parse(response);
		
		Elements fleetEventsRaw = doc.getElementsByClass("eventFleet");
		Iterator<Element> eleIter = fleetEventsRaw.iterator();
		while(eleIter.hasNext()) {
			Element thisEventRow = eleIter.next();
			FleetEvent thisEvent = new FleetEvent();
			
			// Get tr attributes like the mission type, return flight, or arrival time
			Attributes attrs = thisEventRow.attributes();
			for(Attribute thisAttribute : attrs) {
				String key = thisAttribute.getKey();
				String value = thisAttribute.getValue();
				
				if(key.equals("data-mission-type")) {
					thisEvent.data_mission_type = Integer.parseInt(value);
				}
				else if(key.equals("data-return-flight")) {
					thisEvent.data_return_flight = Boolean.parseBoolean(value);
				}
				else if(key.equals("data-arrival-time")) {
					thisEvent.data_arrival_time = Long.parseLong(value);
				}
			}
			
			// Get rest of information from the td's
			Elements tds = thisEventRow.getElementsByTag("td");
			for(Element thisElement : tds) {
				String tdClass = thisElement.className();
				
				if(tdClass.equals("coordsOrigin")) {
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
					for(Element shipOrResource : fleetData) {
						String fleetInfoText = shipOrResource.getElementsByTag("td").text().trim();
						
						if(fleetInfoText.isEmpty()) {
							continue;
						}

						String[] fleetInfo = fleetInfoText.split(":");
						if(fleetInfo == null || fleetInfo.length < 2) {
							continue;
						}
						fleetInfo[0] = fleetInfo[0].trim();
						fleetInfo[1] = fleetInfo[1].trim().replaceAll("\\.", "");

						// Loop through FleetAndResources.class fields and if we find a match for a tr's text add an entry to the fleetRresources
						try {
							FleetAndResources instance = new FleetAndResources();
							for(Field field : FleetAndResources.class.getFields()) {
								String fleetOrResourceValue = FleetAndResources.class.getField(field.getName()).get(instance).toString();
								if(fleetOrResourceValue.equalsIgnoreCase(fleetInfo[0])) {
									// TODO: Is it possible we might have multiple fleets?
									thisEvent.fleetResources.put(fleetOrResourceValue, Long.valueOf(fleetInfo[1]));
								}
							}
						}
						catch (NoSuchFieldException nsfe) {
							System.out.println("Caught NoSuchFieldException: " + nsfe);
						}
						catch (IllegalAccessException iae) {
							System.out.println("Caught IllegalAccessException: " + iae);
						}
					}
				}
			}
			
			eventList.add(thisEvent);
		}
		
		return eventList;
	}
	
	/**
	 * Convenience method for removing javascript from the response. Mainly used by parseOverviewResponse()
	 * @param input
	 * @param searchKey1
	 * @param searchKey2
	 * @return
	 */
	private StringBuilder removeSection(StringBuilder input, String searchKey1, String searchKey2) {
		String searchKey = searchKey1;
		int javascript = input.indexOf(searchKey);
		int endjavascript;
		if(javascript >= 0) {
			searchKey = searchKey2;
			endjavascript = input.indexOf(searchKey, javascript);
			if(endjavascript >= javascript) {
				endjavascript += searchKey.length();
				input.delete(javascript, endjavascript);
			}
			else {
				input.delete(javascript, input.length());
			}
		}
		return input;
	}
	
	/**
	 * Pre-condition: The current state of the XmlPullParser xpp is at a START_TAG
	 * @param xpp
	 * @param attrName
	 * @param attrValue
	 * @return
	 */
	private boolean hasAttrValue(XmlPullParser xpp, String attrName, String attrValue) {
		//Scan attributes for "class" attribute.
		int attrsize = xpp.getAttributeCount();
		int index;
		boolean attrFound = false;
		for(index = 0; index < attrsize; index++) {
			String currentAttrName = xpp.getAttributeName(index);
			if(currentAttrName.equals(attrName)) {
				attrFound = true;
				break;
			}
		}
		
		if(!attrFound)
			return false;
		else {
			String currentAttrValue = xpp.getAttributeValue(index);
			if(currentAttrValue.equals(attrValue)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Unescapes the HTML-escaped data in the parameter string htmlEncodedData.
	 * The string is then parsed with an XmlPullParser to extract fleet and resource
	 * data. The data is inserted into a Map<String, Long> object. This Map is then
	 * returned.
	 * 
	 * @param htmlEncodedData - HTML-escaped string containing the details of the fleet breakdown and composition
	 * @return a Map<String, Long> object containing fleet and resource composition. Keys are listed in
	 * 	class FleetAndResources
	 */
	private Map<String, Long> parseFleetResComposition(String htmlEncodedData) {
		Map<String, Long> fleetResData = new HashMap<String, Long>();
		
		StringReader strReader = null;
		XmlPullParser subxpp = null;
		try {
			strReader = new StringReader(htmlEncodedData);
			subxpp = XmlPullParserFactory.newInstance().newPullParser();
			subxpp.setInput(strReader);
			subxpp.defineEntityReplacementText("nbsp", " ");
			
			boolean parsingShips = false;
			boolean parsingRes = false;
			String currentShip = null;
			String currentRes = null;
			
			int eventType = subxpp.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(subxpp.getEventType() == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					textData = textData.replaceAll(":", "");
					if(textData.equals("Ships")) {
						parsingShips = true;
						break;
					}
				}
				try {
					subxpp.next();
					eventType = subxpp.getEventType();
				}
				catch(XmlPullParserException e) {
					System.out.println("Caught an exception. Not stopping: " + e + '\n' + e.getMessage());
					e.printStackTrace();
				}
			}
			
			while(parsingShips && eventType != XmlPullParser.END_DOCUMENT) {
				subxpp.next();
				eventType = subxpp.getEventType();
				if(eventType == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					if(textData != null) {
						textData = textData.trim();
					}
					if(textData != null && textData.length() > 0) {
						if(textData.equals("Small Cargo:")) {
							currentShip = FleetAndResources.SC;
						}
						else if(textData.equals("Large Cargo:")) {
							currentShip = FleetAndResources.LC;
						}
						else if(textData.equals("Colony Ship:")) {
							currentShip = FleetAndResources.COLONY;
						}
						else if(textData.equals("Light Fighter:")) {
							currentShip = FleetAndResources.LF;
						}
						else if(textData.equals("Heavy Fighter:")) {
							currentShip = FleetAndResources.HF;
						}
						else if(textData.equals("Cruiser:")) {
							currentShip = FleetAndResources.CR;
						}
						else if(textData.equals("Battleship:")) {
							currentShip = FleetAndResources.BS;
						}
						else if(textData.equals("Battlecruiser:")) {
							currentShip = FleetAndResources.BC;
						}
						else if(textData.equals("Bomber:")) {
							currentShip = FleetAndResources.BB;
						}
						else if(textData.equals("Destroyer:")) {
							currentShip = FleetAndResources.DS;
						}
						else if(textData.equals("Deathstar:")) {
							currentShip = FleetAndResources.RIP;
						}
						else if(textData.equals("Recycler:")) {
							currentShip = FleetAndResources.RC;
						}
						else if(textData.equals("Espionage Probe:")) {
							currentShip = FleetAndResources.EP;
						}
						else if(textData.equals("Shipment:")) {
							currentShip = null;
							parsingShips = false;
							parsingRes = true;
							break;
						}
						
						textData = "";
						while(textData.length() == 0) {
							subxpp.next();
							eventType = subxpp.getEventType();
							if(eventType == XmlPullParser.TEXT) {
								textData = subxpp.getText();
								textData = textData.trim();
							}
						}

						String numshipstr = textData;
						numshipstr = numshipstr.replaceAll("\\.", "");
						if(currentShip != null && currentShip.length() > 0) {
							Long numships = Long.valueOf(numshipstr);
							fleetResData.put(currentShip, numships);
						}
					}
				}
			}
			
			eventType = subxpp.getEventType();
			while(parsingRes && eventType != XmlPullParser.END_DOCUMENT) {
				subxpp.next();
				eventType = subxpp.getEventType();
				if(eventType == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					if(textData != null) {
						textData = textData.trim();
					}
					if(textData != null && textData.length() > 0) {
						String resType = subxpp.getText();
						if(resType.equals("Metal:")) {
							currentRes = FleetAndResources.METAL;
						}
						else if(resType.equals("Crystal:")) {
							currentRes = FleetAndResources.CRYSTAL;
						}
						else if(resType.equals("Deuterium:")) {
							currentRes = FleetAndResources.DEUT;
						}
						else {
							continue;
						}
						
						textData = "";
						while(textData.length() == 0) {
							subxpp.next();
							eventType = subxpp.getEventType();
							if(eventType == XmlPullParser.TEXT) {
								textData = subxpp.getText();
								textData = textData.trim();
							}
						}
						
						String amount = textData;
						amount = amount.replaceAll("\\.", "");
						if(amount.length() > 0) {
							Long resAmount = Long.valueOf(amount);
							fleetResData.put(currentRes, resAmount);
						}
					}
				}
			}
		}
		catch (XmlPullParserException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(subxpp != null) {
				try {
					subxpp.setInput(null);
				}
				catch(XmlPullParserException e) {
					System.err.println(e.toString() + '\n' + e.getMessage());
					e.printStackTrace();
				}
			}
			
			if(strReader != null)
				strReader.close();
		}
		
		return fleetResData;
	}
}
