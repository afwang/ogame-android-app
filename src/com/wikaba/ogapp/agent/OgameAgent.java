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
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
	
	private Map<String, HttpCookie> cookieStore;
	private String serverUri;
	
	public OgameAgent() {
		cookieStore = new HashMap<String, HttpCookie>();
	}
	
	/**
	 * Submits user credentials to Ogame. Parses and returns data from HTTP response
	 * @param universe - The name of the universe to log in to.
	 * @param username - Username of the account on universe to log in to.
	 * @return list of cookies set for this session. Null if login failed.
	 */
	public List<HttpCookie> login(String universe, String username, String password) {
		final int timeoutMillis = 30 * 1000;
		HttpURLConnection connection = null;
		URI theUri = null;
		String cookieHeaderStr;
		int response = 0;
		
		boolean successfulResponse;
		
		universe = NameToURI.getDomain(universe);
		serverUri = "http://" + universe;
		
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
			return null;
		}
		String length = Integer.toString(parameters.length());
		try {
			for(boolean redo = true; redo;) {
				try {
					connection = (HttpURLConnection)(new URL(uri)).openConnection();
					theUri = new URI(uri);
					connection.setConnectTimeout(timeoutMillis);
					connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					//No cookies to set on the first HTTP request
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
				List<String> cookieHeaders = responseHeaders.get("Set-Cookie");
				for(String cookieHeader : cookieHeaders) {
					List<HttpCookie> cookiesList = parseCookies(cookieHeader, theUri.getAuthority(), theUri.getPath());
					for(HttpCookie cookie : cookiesList) {
						cookieStore.put(cookie.getName(), cookie);
					}
				}
				
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
				return null;
			}
			
			/*
			 * SECOND REQUEST
			 */
			System.out.println("START SECOND REQUEST");
			connection = (HttpURLConnection)(new URL(uri)).openConnection();
			theUri = new URI(uri);
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setDoOutput(false);
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);

			cookieHeaderStr = getCookieRequestHeader(theUri);			
			if(cookieHeaderStr.length() > 0)
				connection.setRequestProperty("Cookie", cookieHeaderStr);
			
			connection.connect();
			
			response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				successfulResponse = true;
//				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				List<String> cookieHeaders = responseHeaders.get("Set-Cookie");
				if(cookieHeaders == null) {
					cookieStore.clear();
					return null;
				}
				for(String cookieHeader : cookieHeaders) {
//					System.out.println(cookieHeader);
					List<HttpCookie> cookiesList = parseCookies(cookieHeader, theUri.getAuthority(), theUri.getPath());
					for(HttpCookie cookie : cookiesList) {
						cookieStore.put(cookie.getName(), cookie);
					}
				}
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
//					System.out.println("Redirected to: " + uri);
				}
				
//				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				String line;
//				while((reader.readLine()) != null) {
//					System.out.println(line);
//				}
//				reader.close();
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
				return null;
			}
			
			/*
			 * THIRD REQUEST (final request)
			 */
			for(boolean redo = true; redo; ) {
				try {
					System.out.println("START THIRD REQUEST");
					connection = (HttpURLConnection)(new URL(uri)).openConnection();
					theUri = new URI(uri);
					connection.setConnectTimeout(timeoutMillis);
					connection.setRequestProperty("Accept-Language", "en-US");
					connection.setRequestProperty("Accept", "text/html");
					
					cookieHeaderStr = getCookieRequestHeader(theUri);
					if(cookieHeaderStr.length() > 0)
						connection.setRequestProperty("Cookie", cookieHeaderStr);
					
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
//				System.out.println("Response headers:");
//				Set<Map.Entry<String, List<String>>> entrySet = responseHeaders.entrySet();
//				for(Map.Entry<String, List<String>> mapping : entrySet) {
//					List<String> values = mapping.getValue();
//					for(String val : values) {
//						System.out.println(mapping.getKey() + ": " + val);
//					}
//				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				String line;
				while((reader.readLine()) != null) {
//					System.out.println(line);
				}
				reader.close();
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
//					System.out.println("Redirected to: " + uri);
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
				return null;
			}
		}
		catch(MalformedURLException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch(IOException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (URISyntaxException e) {
			System.err.println("URI error: " + e + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			connection.disconnect();
		}
		
		Iterator<Map.Entry<String, HttpCookie>> cookieIter = cookieStore.entrySet().iterator();
		List<HttpCookie> cookieList = new LinkedList<HttpCookie>();
		while(cookieIter.hasNext()) {
			Map.Entry<String, HttpCookie> entry = cookieIter.next();
			HttpCookie theCookie = entry.getValue();
			cookieList.add(theCookie);
		}
		return cookieList;
	}
	
	/**
	 * We have to implement our own cookie parser because Java's HttpCookie.parse() method throws an Exception when
	 * it should not throw that Exception.
	 * 
	 * Pre-condition: defaultDomain and defaultPath must not be empty strings. defaultDomain must be a valid domain.
	 * defaultPath must be a valid path.
	 * 
	 * Post-condition: The list of cookies returned will properly contain the data encoded in the Set-Cookie header, cookieStr.
	 * The cookies will have a valid domain and path set, defaulting to defaultDomain and defaultPath if the domain and path are
	 * not set in the header.
	 * 
	 * @param cookieStr - The header value from one Set-Cookie header field without the "Set-Cookie" portion. May contain multiple multiple cookies.
	 * @param defaultDomain - default domain to set for cookies that do not have the "domain" attribute set in the header
	 * @param defaultPath - default path to set for cookies that do not have the "path" attribute set in the header
	 * @return A list of HttpCookie objects representing the cookies in cookieStr
	 */
	private List<HttpCookie> parseCookies(String cookieStr, String defaultDomain, String defaultPath) {
		HttpCookie cookie = null;
		String[] cookieTokens = cookieStr.split(";");
		
		String name = null;
		String value = null;
		String expiration = null;
		String path = null;
		String domain = null;
		
		for(String tok : cookieTokens) {
			String[] tokpieces = tok.split("=");
			if(tokpieces.length == 2) {
				tokpieces[0] = tokpieces[0].trim();
				tokpieces[1] = tokpieces[1].trim();
				
				if(tokpieces[0].equalsIgnoreCase("expires")) {
					expiration = tokpieces[1];
				}
				else if(tokpieces[0].equalsIgnoreCase("path")) {
					path = tokpieces[1];
				}
				else if(tokpieces[0].equalsIgnoreCase("domain")) {
					domain = tokpieces[1];
				}
				else { //name of the cookie
					name = tokpieces[0];
					value = tokpieces[1];
				}
			}
		}
		
		if(name == null)
			return null;
		
		cookie = new HttpCookie(name, value);
		cookie.setVersion(0);
		if(expiration != null) {
			//sample format: Tue, 15-Jan-2013 21:47:38 GMT
			//another sample from Ogame itself: Sat, 01-Nov-2014 21:22:40 GMT
			//All non-numerical strings are 3 letters long.
			String[] weekdaySplit = expiration.split(",");
			//weekday does not need to be parsed. We don't use that to calculate the time.
//			String weekday = weekdaySplit[0];
			expiration = weekdaySplit[1].trim();
			
			String[] dateSplit = expiration.split("-");
			String dom = dateSplit[0];
			String month = dateSplit[1];
			String[] yearSplit = dateSplit[2].split(" ");
			String year = yearSplit[0];
			
			String[] timeSplit = yearSplit[1].split(":");
			String hour = timeSplit[0];
			String minute = timeSplit[1];
			String seconds = timeSplit[2];
			
			//should just be GMT. Do not need to parse this.
//			String zone = yearSplit[2];
			
			TimeZone gmtZone = TimeZone.getTimeZone("GMT");
			Calendar cal = Calendar.getInstance(gmtZone);
			int iyear = Integer.valueOf(year);
			int imonth = 0;
			if(month.equals("Jan")) {
				imonth = Calendar.JANUARY;
			}
			else if(month.equals("Feb")) {
				imonth = Calendar.FEBRUARY;
			}
			else if(month.equals("Mar")) {
				imonth = Calendar.MARCH;
			}
			else if(month.equals("Apr")) {
				imonth = Calendar.APRIL;
			}
			else if(month.equals("May")) {
				imonth = Calendar.MAY;
			}
			else if(month.equals("Jun")) {
				imonth = Calendar.JUNE;
			}
			else if(month.equals("Jul")) {
				imonth = Calendar.JULY;
			}
			else if(month.equals("Aug")) {
				imonth = Calendar.AUGUST;
			}
			else if(month.equals("Sep")) {
				imonth = Calendar.SEPTEMBER;
			}
			else if(month.equals("Oct")) {
				imonth = Calendar.OCTOBER;
			}
			else if(month.equals("Nov")) {
				imonth = Calendar.NOVEMBER;
			}
			else if(month.equals("Dec")) {
				imonth = Calendar.DECEMBER;
			}
			int iday = Integer.valueOf(dom);
			int ihour = Integer.valueOf(hour);
			int iminute = Integer.valueOf(minute);
			int iseconds = Integer.valueOf(seconds);
			cal.set(iyear, imonth, iday, ihour, iminute, iseconds);
			long timeInMillis = cal.getTimeInMillis();
			long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
			cookie.setMaxAge((timeInMillis - currentTimeInMillis) / 1000);
		}
		if(path != null) {
			cookie.setPath(path);
		}
		else {
			cookie.setPath(defaultPath);
		}
		if(domain != null) {
			cookie.setDomain(domain);
		}
		else {
			cookie.setDomain(defaultDomain);
		}
		ArrayList<HttpCookie> cookieList = new ArrayList<HttpCookie>();
		cookieList.add(cookie);
		return cookieList;
	}

	/**
	 * Emulate a user clicking the "Overview" link in the navigation bar/column. Also parse fleet
	 * movement data from the returned response.
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * 		is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * 		will have non-null instance variables.
	 */
	public List<FleetEvent> getOverviewData() throws LoggedOutException {
		List<FleetEvent> overviewData = null;
		
		HttpURLConnection conn = null;
		String connectionUriStr = serverUri + OVERVIEW_ENDPOINT;
		URI connUri;
		try {
			connUri = new URI(connectionUriStr);
		}
		catch(URISyntaxException e) {
			System.err.println("URI syntax is wrong: " + connectionUriStr);
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		try {
			URL connectionUrl = new URL(connectionUriStr);
			conn = (HttpURLConnection)connectionUrl.openConnection();
		}
		catch(MalformedURLException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch(IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		InputStream responseStream = null;
		try {
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			int responseCode = goToOverview(conn, connUri);
			
			if(responseCode < 0) {
				return null;
			}
			
			boolean isError;
			if(responseCode >= 400) {
				responseStream = conn.getErrorStream();
				isError = true;
			}
			else if(responseCode == 302) {
				throw new LoggedOutException("Agent's cookies are no longer valid");
			}
			else {
				responseStream = conn.getInputStream();
				isError = false;
			}
			
			if(isError) {
				BufferedReader isr = new BufferedReader(new InputStreamReader(responseStream));
				String line;
				while((line = isr.readLine()) != null) {
					System.err.println(line);
				}
				isr.close();
				return null;
			}
			else {
				List<FleetEvent> events = parseOverviewResponse(responseStream);
				overviewData = events;
			}
			responseStream.close();
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
		return overviewData;
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
	 * @param connectionUri - used to determine which cookies to include with the HTTP request.
	 * @return the HTTP response's status code if a connection was made. -1 if setting up the connection failed
	 * 	-2 if the connection could not be made.
	 */
	private int goToOverview(HttpURLConnection connection, URI connectionUri) {
		int responseCode = 0;
		
		try {
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			String cookieHeader = getCookieRequestHeader(connectionUri);
			if(cookieHeader.length() > 0)
				connection.setRequestProperty("Cookie", cookieHeader);
			
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
	 * Uses an XMLPullParser to parse an HTML file presented as an InputStream for fleet events.
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
	private List<FleetEvent> parseOverviewResponse(InputStream inputStream) {
		List<FleetEvent> eventList = new LinkedList<FleetEvent>();
		
		String response = "";
		try {
			StringBuilder strb = new StringBuilder();
			char[] buffer = new char[1024];
			InputStreamReader isr = new InputStreamReader(inputStream);
			while((isr.read(buffer)) > 0) {
				strb.append(buffer);
			}
			response = strb.toString().replaceAll("&(?![A-Za-z]+;)", "&amp;");
			strb = new StringBuilder(response);
			//Removing javascript:
			removeSection(strb, "<!-- JAVASCRIPT -->", "<!-- END JAVASCRIPT -->");
			removeSection(strb, "<!-- #MMO:NETBAR# -->", "</script>");
			removeSection(strb, "<!-- Start Alexa Certify Javascript -->", "</script>");
			removeSection(strb, "The relocation allows you to move your planets", "deactivated for 24 hours.");
			removeSection(strb, "<div id=\"mmonetbar\" class=\"mmoogame\">", "</script>");
			response = strb.toString();
//			System.out.println(response);
		}
		catch(IOException e) {
			System.err.println("Error reading the response: " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			XmlPullParserFactory xppfactory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = xppfactory.newPullParser();
			xpp.setInput(new StringReader(response));
			xpp.defineEntityReplacementText("ndash", "-");
			xpp.defineEntityReplacementText("nbsp", " ");
			
			FleetEvent lastScannedEvent = null;
			//To make the next for loop look easier to read and understand, we get to the first instance of
			//<tr class="eventFleet">
			int eventType = xpp.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = xpp.getName();

				if(tagName != null && tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
					lastScannedEvent = new FleetEvent();
					int attrCount = xpp.getAttributeCount();
					for(int index = 0; index < attrCount; index++) {
						String attrName = xpp.getAttributeName(index);
						if(attrName.equals("data-mission-type")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_mission_type = Integer.valueOf(value);
						}
						else if(attrName.equals("data-return-flight")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_return_flight = Boolean.valueOf(value);
						}
						else if(attrName.equals("data-arrival-time")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_arrival_time = Long.valueOf(value);
						}
					}
					//We must call next() here before breaking. Otherwise, the next loop
					//will add the incomplete FleetEvent object since it will detect
					//the same <tr...> element and think it's a new event when it's
					//still the same first event.
					xpp.next();
					break;
				}
				try {
					eventType = xpp.next();
				}
				catch(XmlPullParserException e) {
					if(e != null) {
						/* For some strange reason, the emulator can reach this catch block with
						 * e set to null. (Why and how?) Might be a debugger bug
						 */
						System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
						e.printStackTrace();
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					//This exception occurs near the end of the document, but it is not something that
					//should stop the app over.
					System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
					e.printStackTrace();
					eventType = XmlPullParser.END_DOCUMENT;
				}
			}
			
			//No events scanned. Just return.
			if(lastScannedEvent == null)
				return eventList;
			
			eventType = xpp.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				//Begin parsing for fleet events.
				if(eventType == XmlPullParser.START_TAG) {
					String tagName = xpp.getName();
					tagName = (tagName == null) ? "" : tagName;
					
					if(tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
						eventList.add(lastScannedEvent);
						lastScannedEvent = new FleetEvent();
						
						int attrCount = xpp.getAttributeCount();
						for(int index = 0; index < attrCount; index++) {
							String attrName = xpp.getAttributeName(index);
							if(attrName.equals("data-mission-type")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_mission_type = Integer.valueOf(value);
							}
							else if(attrName.equals("data-return-flight")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_return_flight = Boolean.valueOf(value);
							}
							else if(attrName.equals("data-arrival-time")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_arrival_time = Long.valueOf(value);
							}
						}
					}
					else if(tagName.equals("td")) {
						if(hasAttrValue(xpp, "class", "originFleet")) {
							/* Example from the extracted response sample:
							 * 	<td class="originFleet"> <--XPP pointer is here currently
									<span class="tooltip" title="A Whole New World">
										<figure class="planetIcon planet"></figure>
										A Whole New World
									</span>
								</td>
							 */
							tagName = xpp.getName();
							int htmlevent = 0;
							int counter = 0;
							//From the response extract, we need 5 next()'s to get to the text we need.
							//Set a hard limit just in case.
							while((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}
							
							xpp.next();
							if(xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.originFleet = xpp.getText();
								if(lastScannedEvent.originFleet != null)
									lastScannedEvent.originFleet = lastScannedEvent.originFleet.trim();
							}
						}
						else if(hasAttrValue(xpp, "class", "coordsOrigin")) {
							/* Example:
							 * <td class="coordsOrigin"> <-- XPP pointer here
									<a href="http://s125-en.ogame.gameforge.com/game/index.php?page=galaxy&galaxy=1&system=373" target="_top">
										[1:373:8]
									</a>
								</td>
							 */
							tagName = xpp.getName();
							
							//We need 2 next()'s to get to the <a> element. Use a hard limit just in case.
							int htmlevent = 0;
							int counter = 0;
							while((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}
							
							xpp.next();
							if(xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.coordsOrigin = xpp.getText();
								if(lastScannedEvent.coordsOrigin != null)
									lastScannedEvent.coordsOrigin = lastScannedEvent.coordsOrigin.trim();
							}
						}
						else if(hasAttrValue(xpp, "class", "icon_movement") || hasAttrValue(xpp, "class", "icon_movement_reserve")) {
							//Have to parse another HTML snippet. This HTML is both encoded to not confuse
							//the parser, so it must be decoded first. Then it must be put through another
							//XmlPullParser to gather the data.
							/* Example:
							 * <td class="icon_movement"> <-- xpp point here
							 * 	<span class="blah blah blah"
							 * 		title="bunch of escaped HTML we have to unescape"
							 * 		data-federation-user-id="">
							 * 			&nbsp;
							 * 	</span>
							 * </td>
							 */
							tagName = xpp.getName();
							int htmlevent = 0;
							tagName = (tagName == null) ? "" : tagName;
							while(htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("span")) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
							}
							
							Map<String, Long> fleetData = null;
							if(xpp.getEventType() == XmlPullParser.START_TAG) {
								int attrSize = xpp.getAttributeCount();
								String titleValue = null;
								for(int index = 0; index < attrSize; index++) {
									String attrName = xpp.getAttributeName(index);
									if(attrName.equals("title")) {
										titleValue = xpp.getAttributeValue(index);
									}
								}
								
								if(titleValue != null) {
									fleetData = parseFleetResComposition(titleValue);
								}
							}
							
							lastScannedEvent.fleetResources.putAll(fleetData);
						}
						else if(hasAttrValue(xpp, "class", "destFleet")) {
							/* Example:
							 * <td class="destFleet"> <-- XPP pointer here
									<span class="tooltip" title="Slot 8 unavailable">
										<figure class="planetIcon planet"></figure>
										Slot 8 unavailable
									</span>
								</td>
							 */
							int counter = 0;
							int htmlevent = 0;
							tagName = xpp.getName();
							tagName = (tagName == null) ? "" : tagName;
							while((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}
							xpp.next();
							if(xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.destFleet = xpp.getText();
								if(lastScannedEvent.destFleet != null)
									lastScannedEvent.destFleet = lastScannedEvent.destFleet.trim();
							}
						}
						else if(hasAttrValue(xpp, "class", "destCoords")) {
							/* Example:
							 * <td class="destCoords"> <--XPP pointer here
									<a href="http://s125-en.ogame.gameforge.com/game/index.php?page=galaxy&galaxy=1&system=204" target="_top">
										[1:204:8]
									</a>
								</td>
							 */
							
							int counter = 0;
							int htmlevent = 0;
							tagName = xpp.getName();
							tagName = (tagName == null) ? "" : tagName;
							while((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}
							
							xpp.next();
							if(xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.destCoords = xpp.getText();
								if(lastScannedEvent.destCoords != null)
									lastScannedEvent.destCoords = lastScannedEvent.destCoords.trim();
							}
						}
					}
				}
				try {
					eventType = xpp.next();
				}
				catch(XmlPullParserException e) {
					if(e != null) {
						/* For some strange reason, the emulator can reach this catch block with
						 * e set to null. (Why and how?) Might be a debugger bug
						 */
						System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
						e.printStackTrace();
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					//This exception occurs near the end of the document, but it is not something that
					//should stop the app over.
					System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
					e.printStackTrace();
					eventType = XmlPullParser.END_DOCUMENT;
				}
			}
			if(lastScannedEvent != null) {
				eventList.add(lastScannedEvent);
			}
		}
		catch (XmlPullParserException e) {
			if(e != null) {
				System.err.println(e.toString() + '\n' + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
		catch (IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
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
	
	/**
	 * Checks the cookie store and builds the Cookie HTTP request header, without the "Cookie: "
	 * portion.
	 * @param connection - This URI is used to determine which cookies from the cookie store are
	 * added to the header.
	 * @return cookie request header
	 */
	private String getCookieRequestHeader(URI connection) {
		StringBuilder cookieStrBuilder = new StringBuilder();
		Iterator<Map.Entry<String, HttpCookie>> cookieMapIter = cookieStore.entrySet().iterator();
		
		String connDomain = connection.getAuthority();
		String connPath = connection.getPath();
		
		boolean isFirstCookie = true;
		
		while (cookieMapIter.hasNext()) {
			Map.Entry<String, HttpCookie> cookieEntry = cookieMapIter.next();
			HttpCookie cookie = cookieEntry.getValue();
			String cookieDomain = cookie.getDomain();
			String cookiePath = cookie.getPath();
			if(cookieDomain.charAt(0) != '.' && !connDomain.equals(cookieDomain))
				cookieDomain = '.' + cookieDomain;
			
			if(connDomain.endsWith(cookieDomain) && connPath.startsWith(cookiePath) && !cookie.hasExpired()) {
				//isFirstCookie and the following if block adds the delimiting comma (,) between cookies
				//if there are multiple cookies.
				if(!isFirstCookie) {
					cookieStrBuilder.append(';');
				}
				cookieStrBuilder.append(cookie.toString());
				isFirstCookie = false;
			}
		}
		
		return cookieStrBuilder.toString();
	}
}
