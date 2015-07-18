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


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 *
 * @author afwang
 */
public class OgameAgent {
    public static final String LOGIN_URL = "http://en.ogame.gameforge.com/main/login";
    public static final String OVERVIEW_ENDPOINT = "/game/index.php?page=overview";
    public static final String EVENTLIST_ENDPOINT = "/game/index.php?page=eventList&ajax=1";

    private String serverUri;

    public OgameAgent(String universe, String lang) {
        if (universe == null) {
            throw new IllegalArgumentException("OgameAgent constructor argument is null");
        }
        serverUri = "http://" + String.format(NameToURI.getDomain(universe), lang);
    }

    /**
     * Submits user credentials to Ogame to obtain set of cookies (handled
     * by system's CookieHandler.
     * <p/>
     * This method's purpose is to add the cookies into the current
     * session's CookieStore
     *
     * @param universe - Universe of the account
     * @param username - Username of the account
     * @param password - Password of the account
     * @return true on successful login, false on failure
     */
    public boolean login(String universe, String username, String password, String lang) {
        final int timeoutMillis = 30 * 1000;
        HttpURLConnection connection = null;
        int response = 0;

        boolean successfulResponse;

        universe = String.format(NameToURI.getDomain(universe), lang);
        serverUri = "http://" + universe;

        //Unfortunately, we have to be a bit more direct about
        //following these redirects because Ogame returns cookies that
        //causes Java to throw an error when parsed (might want to
        //look into a different HTTP library for this, like OkHttp).

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
            for (boolean redo = true; redo; ) {
                try {
                    connection = (HttpURLConnection) (new URL(uri)).openConnection();
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
                } catch (java.io.EOFException e) {
                    //Catch annoying server-side issue sending faulty HTTP responses.
                    //Just force a retry.
                    redo = true;
                }
            }

            if (response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
                successfulResponse = true;
                System.out.println("Everything went okay! Response " + response);

                Map<String, List<String>> responseHeaders = connection.getHeaderFields();
                List<String> locationHeader = responseHeaders.get("Location");
                if (locationHeader != null && locationHeader.size() > 0) {
                    uri = locationHeader.get(0);
                }
            } else {
                successfulResponse = false;
                System.err.println("Something went wrong!");
                BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while ((line = errReader.readLine()) != null) {
                    System.err.println(line);
                }
                errReader.close();
            }
            connection.disconnect();
            System.out.println("END FIRST REQUEST (login)");

            if (!successfulResponse) {
                return false;
            }
			
			/*
			 * SECOND REQUEST
			 */
            System.out.println("START SECOND REQUEST");
            connection = (HttpURLConnection) (new URL(uri)).openConnection();
            connection.setConnectTimeout(timeoutMillis);
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);

            connection.connect();

            response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
                successfulResponse = true;

                Map<String, List<String>> responseHeaders = connection.getHeaderFields();

                List<String> locationHeader = responseHeaders.get("Location");
                if (locationHeader != null && locationHeader.size() > 0) {
                    uri = locationHeader.get(0);
                }
            } else {
                successfulResponse = false;
                System.err.println("Something went wrong!");
                BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while ((line = errReader.readLine()) != null) {
                    System.err.println(line);
                }
                errReader.close();
            }
            connection.disconnect();
            System.out.println("END SECOND REQUEST");
            if (!successfulResponse) {
                return false;
            }
			
			/*
			 * THIRD REQUEST (final request)
			 */
            for (boolean redo = true; redo; ) {
                try {
                    System.out.println("START THIRD REQUEST");
                    connection = (HttpURLConnection) (new URL(uri)).openConnection();
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
                } catch (java.io.EOFException e) {
                    //Again, to avoid the weird EOFException bug. Goddamn Ogame's programming!
                    redo = true;
                }
            }
            //The last response must receive a status 200. If it isn't, we did something wrong.
            if (response == HttpURLConnection.HTTP_OK) {
                successfulResponse = true;
                System.out.println("Everything went okay! Response " + response);

                Map<String, List<String>> responseHeaders = connection.getHeaderFields();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((reader.readLine()) != null) ;
                reader.close();

                List<String> locationHeader = responseHeaders.get("Location");
                if (locationHeader != null && locationHeader.size() > 0) {
                    uri = locationHeader.get(0);
                }
            } else {
                successfulResponse = false;
                System.err.println("Something went wrong!");
                BufferedReader errReader;
                try {
                    errReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } catch (IOException e) {
                    errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String line;
                while ((line = errReader.readLine()) != null) {
                    System.err.println(line);
                }
                errReader.close();
            }
            connection.disconnect();
            System.out.println("END THIRD REQUEST");
            if (!successfulResponse) {
                return false;
            }
        } catch (MalformedURLException e) {
            System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            connection.disconnect();
        }

        return true;
    }

    /**
     * Emulate a user clicking the "Overview" link in the navigation bar/column. Also parse fleet
     * movement data from the returned response (if available).
     *
     * @return If no errors occurred while extracting the data, a list of fleet movements with details
     * is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
     * will have non-null instance variables.
     */
    public List<FleetEvent> getOverviewData() throws LoggedOutException {
        List<FleetEvent> overviewData = null;

        HttpURLConnection conn = null;
        String connectionUriStr = serverUri + OVERVIEW_ENDPOINT;

        try {
            URL connectionUrl = new URL(connectionUriStr);
            conn = (HttpURLConnection) connectionUrl.openConnection();
        } catch (IOException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
            return null;
        }

        InputStream responseStream = null;
        try {
            conn.setInstanceFollowRedirects(false);
            int responseCode = makeGETReq(conn);

            if (responseCode < 0) {
                return null;
            }

            boolean isError;
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                responseStream = conn.getErrorStream();
                isError = true;
            } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new LoggedOutException("Agent's cookies are no longer valid");
            } else {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    isError = false;
                } else {
                    isError = true;
                }
                responseStream = conn.getInputStream();
            }

            if (isError) {
                BufferedReader isr = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while ((line = isr.readLine()) != null) {
                    System.err.println(line);
                }
                isr.close();
            } else {
                List<FleetEvent> events = parseEvents(responseStream);
                overviewData = events;
            }
        } catch (IOException e) {
            System.err.println("Could not read response stream");
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                }
            }
        }

        if (overviewData != null && overviewData.size() == 0) {
            overviewData = getFleetEvents();
        }

        return overviewData;
    }

    /**
     * Parse fleet movement data from the returned response from EVENTLIST_ENDPOINT.
     *
     * @return If no errors occurred while extracting the data, a list of fleet movements with details
     * is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
     * will have non-null instance variables.
     */
    public List<FleetEvent> getFleetEvents() throws LoggedOutException {
        HttpURLConnection conn = null;
        InputStream responseStream = null;
        List<FleetEvent> events = null;

        try {
            URL connection = new URL(serverUri + EVENTLIST_ENDPOINT);
            conn = (HttpURLConnection) connection.openConnection();

            conn.setInstanceFollowRedirects(false);

            int responseCode = makeGETReq(conn);

            if (responseCode < 0) {
                return null;
            }

            boolean isError;
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                responseStream = conn.getErrorStream();
                isError = true;
            } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new LoggedOutException("Agent's cookies are no longer valid");
            } else {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    isError = false;
                } else {
                    isError = true;
                }
                responseStream = conn.getInputStream();
            }

            if (isError) {
                BufferedReader isr = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while ((line = isr.readLine()) != null) {
                    System.err.println(line);
                }
                isr.close();
            } else {
                events = parseEvents(responseStream);
            }
        } catch (IOException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                }
            }
        }
        return events;
    }

    /**
     * Sets the required headers and cookies for the request specified by connection.
     * Connects to the URL specified by connection.
     * <p/>
     * Pre-condition: Parameter connection must be a valid connection request, and it
     * should not have had connect() called on it.
     * <p/>
     * Post-condition: Parameter connection now has either an input or error stream to read from.
     * Use the return value of this method to determine which stream to read from. Method
     * disconnect() has also not been called on connection, so the caller of this method should
     * handle calling disconnect on the connection.
     *
     * @param connection - an HttpURLConnection to connect to.
     * @return the HTTP response's status code if a connection was made. -1 if setting up the connection failed
     * -2 if the connection could not be made.
     */
    private int makeGETReq(HttpURLConnection connection) {
        int responseCode = 0;

        try {
            connection.setRequestProperty("Accept-Language", "en-US");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            responseCode = connection.getResponseCode();
        } catch (ProtocolException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
            responseCode = -1;
        } catch (IOException e) {
            System.err.println("Could not open connection");
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
            responseCode = -2;
        }

        return responseCode;
    }

    /**
     * Uses an XMLPullParser to parse an HTML file presented as an InputStream for fleet events.
     * <p/>
     * Pre-condition: inputStream must not be closed.
     * <p/>
     * Post-condition: inputStream remains unclosed. The caller must close it themselves.
     *
     * @param inputStream - Input stream from HttpURLConnection after a successful connection.
     *                    inputStream contains the HTML response from the Ogame server.
     * @return list of mission events parsed from HTML InputStream. Returns empty list if no events.
     * Null on error.
     */
    private List<FleetEvent> parseEvents(InputStream inputStream) {
        List<FleetEvent> eventList = new LinkedList<FleetEvent>();

        String response = "";
        try {
            StringBuilder strb = new StringBuilder();
            char[] buffer = new char[1024];
            InputStreamReader isr = new InputStreamReader(inputStream);
            while ((isr.read(buffer)) > 0) {
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
        } catch (IOException e) {
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
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                if (tagName != null && tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
                    lastScannedEvent = new FleetEvent();
                    int attrCount = xpp.getAttributeCount();
                    for (int index = 0; index < attrCount; index++) {
                        String attrName = xpp.getAttributeName(index);
                        if (attrName.equals("data-mission-type")) {
                            String value = xpp.getAttributeValue(index);
                            lastScannedEvent.data_mission_type = Integer.valueOf(value);
                        } else if (attrName.equals("data-return-flight")) {
                            String value = xpp.getAttributeValue(index);
                            lastScannedEvent.data_return_flight = Boolean.valueOf(value);
                        } else if (attrName.equals("data-arrival-time")) {
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
                } catch (XmlPullParserException e) {
                    if (e != null) {
						/* For some strange reason, the emulator can reach this catch block with
						 * e set to null. (Why and how?) Might be a debugger bug
						 */
                        System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //This exception occurs near the end of the document, but it is not something that
                    //should stop the app over.
                    System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
                    e.printStackTrace();
                    eventType = XmlPullParser.END_DOCUMENT;
                }
            }

            //No events scanned. Just return.
            if (lastScannedEvent == null)
                return eventList;

            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Begin parsing for fleet events.
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    tagName = (tagName == null) ? "" : tagName;

                    if (tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
                        eventList.add(lastScannedEvent);
                        lastScannedEvent = new FleetEvent();

                        int attrCount = xpp.getAttributeCount();
                        for (int index = 0; index < attrCount; index++) {
                            String attrName = xpp.getAttributeName(index);
                            if (attrName.equals("data-mission-type")) {
                                String value = xpp.getAttributeValue(index);
                                lastScannedEvent.data_mission_type = Integer.valueOf(value);
                            } else if (attrName.equals("data-return-flight")) {
                                String value = xpp.getAttributeValue(index);
                                lastScannedEvent.data_return_flight = Boolean.valueOf(value);
                            } else if (attrName.equals("data-arrival-time")) {
                                String value = xpp.getAttributeValue(index);
                                lastScannedEvent.data_arrival_time = Long.valueOf(value);
                            }
                        }
                    } else if (tagName.equals("td")) {
                        if (hasAttrValue(xpp, "class", "originFleet")) {
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
                            while ((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
                                htmlevent = xpp.next();
                                tagName = xpp.getName();
                                counter++;
                            }

                            xpp.next();
                            if (xpp.getEventType() == XmlPullParser.TEXT) {
                                lastScannedEvent.originFleet = xpp.getText();
                                if (lastScannedEvent.originFleet != null)
                                    lastScannedEvent.originFleet = lastScannedEvent.originFleet.trim();
                            }
                        } else if (hasAttrValue(xpp, "class", "coordsOrigin")) {
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
                            while ((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
                                htmlevent = xpp.next();
                                tagName = xpp.getName();
                                counter++;
                            }

                            xpp.next();
                            if (xpp.getEventType() == XmlPullParser.TEXT) {
                                lastScannedEvent.coordsOrigin = xpp.getText();
                                if (lastScannedEvent.coordsOrigin != null)
                                    lastScannedEvent.coordsOrigin = lastScannedEvent.coordsOrigin.trim();
                            }
                        } else if (hasAttrValue(xpp, "class", "icon_movement") || hasAttrValue(xpp, "class", "icon_movement_reserve")) {
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
                            while (htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("span")) {
                                htmlevent = xpp.next();
                                tagName = xpp.getName();
                            }

                            Map<String, Long> fleetData = null;
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                int attrSize = xpp.getAttributeCount();
                                String titleValue = null;
                                for (int index = 0; index < attrSize; index++) {
                                    String attrName = xpp.getAttributeName(index);
                                    if (attrName.equals("title")) {
                                        titleValue = xpp.getAttributeValue(index);
                                    }
                                }

                                if (titleValue != null) {
                                    fleetData = parseFleetResComposition(titleValue);
                                }
                            }

                            lastScannedEvent.fleetResources.putAll(fleetData);
                        } else if (hasAttrValue(xpp, "class", "destFleet")) {
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
                            while ((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
                                htmlevent = xpp.next();
                                tagName = xpp.getName();
                                counter++;
                            }
                            xpp.next();
                            if (xpp.getEventType() == XmlPullParser.TEXT) {
                                lastScannedEvent.destFleet = xpp.getText();
                                if (lastScannedEvent.destFleet != null)
                                    lastScannedEvent.destFleet = lastScannedEvent.destFleet.trim();
                            }
                        } else if (hasAttrValue(xpp, "class", "destCoords")) {
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
                            while ((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
                                htmlevent = xpp.next();
                                tagName = xpp.getName();
                                counter++;
                            }

                            xpp.next();
                            if (xpp.getEventType() == XmlPullParser.TEXT) {
                                lastScannedEvent.destCoords = xpp.getText();
                                if (lastScannedEvent.destCoords != null)
                                    lastScannedEvent.destCoords = lastScannedEvent.destCoords.trim();
                            }
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (XmlPullParserException e) {
                    if (e != null) {
                        System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
                        e.printStackTrace();
                    }
                    eventType = XmlPullParser.END_DOCUMENT;
                } catch (ArrayIndexOutOfBoundsException e) {
                    //This exception occurs near the end of the document, but it is not something that
                    //should stop the app over.
                    System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
                    e.printStackTrace();
                    eventType = XmlPullParser.END_DOCUMENT;
                }
            }
            if (lastScannedEvent != null) {
                eventList.add(lastScannedEvent);
            }
        } catch (XmlPullParserException e) {
            if (e != null) {
                System.err.println(e.toString() + '\n' + e.getMessage());
                e.printStackTrace();
            }
            return null;
        } catch (IOException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
        }
        return eventList;
    }

    /**
     * Convenience method for removing javascript from the response. Mainly used by parseOverviewResponse()
     *
     * @param input
     * @param searchKey1
     * @param searchKey2
     * @return
     */
    private StringBuilder removeSection(StringBuilder input, String searchKey1, String searchKey2) {
        String searchKey = searchKey1;
        int javascript = input.indexOf(searchKey);
        int endjavascript;
        if (javascript >= 0) {
            searchKey = searchKey2;
            endjavascript = input.indexOf(searchKey, javascript);
            if (endjavascript >= javascript) {
                endjavascript += searchKey.length();
                input.delete(javascript, endjavascript);
            } else {
                input.delete(javascript, input.length());
            }
        }
        return input;
    }

    /**
     * Pre-condition: The current state of the XmlPullParser xpp is at a START_TAG
     *
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
        for (index = 0; index < attrsize; index++) {
            String currentAttrName = xpp.getAttributeName(index);
            if (currentAttrName.equals(attrName)) {
                attrFound = true;
                break;
            }
        }

        if (!attrFound)
            return false;
        else {
            String currentAttrValue = xpp.getAttributeValue(index);
            if (currentAttrValue.equals(attrValue)) {
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
     * class FleetAndResources
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
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (subxpp.getEventType() == XmlPullParser.TEXT) {
                    String textData = subxpp.getText();
                    textData = textData.replaceAll(":", "");
                    if (textData.equals("Ships")) {
                        parsingShips = true;
                        break;
                    }
                }
                try {
                    subxpp.next();
                    eventType = subxpp.getEventType();
                } catch (XmlPullParserException e) {
                    System.out.println("Caught an exception. Not stopping: " + e + '\n' + e.getMessage());
                    e.printStackTrace();
                }
            }

            while (parsingShips && eventType != XmlPullParser.END_DOCUMENT) {
                subxpp.next();
                eventType = subxpp.getEventType();
                if (eventType == XmlPullParser.TEXT) {
                    String textData = subxpp.getText();
                    if (textData != null) {
                        textData = textData.trim();
                    }
                    if (textData != null && textData.length() > 0) {
                        if (textData.equals("Shipment:")) {
                            currentShip = null;
                            parsingShips = false;
                            parsingRes = true;
                            break;
                        } else {
                            textData = textData.substring(0, textData.length() - 1);
                            currentShip = FleetAndResources.getName(textData);
                        }

                        textData = "";
                        while (textData.length() == 0) {
                            subxpp.next();
                            eventType = subxpp.getEventType();
                            if (eventType == XmlPullParser.TEXT) {
                                textData = subxpp.getText();
                                textData = textData.trim();
                            }
                        }

                        String numshipstr = textData;
                        numshipstr = numshipstr.replaceAll("\\.", "");
                        if (currentShip != null && currentShip.length() > 0) {
                            Long numships = Long.valueOf(numshipstr);
                            fleetResData.put(currentShip, numships);
                        }
                    }
                }
            }

            eventType = subxpp.getEventType();
            while (parsingRes && eventType != XmlPullParser.END_DOCUMENT) {
                subxpp.next();
                eventType = subxpp.getEventType();
                if (eventType == XmlPullParser.TEXT) {
                    String textData = subxpp.getText();
                    if (textData != null) {
                        textData = textData.trim();
                    }
                    if (textData != null && textData.length() > 0) {
                        String resType = subxpp.getText();
                        if (resType.equals("Metal:")) {
                            currentRes = FleetAndResources.METAL;
                        } else if (resType.equals("Crystal:")) {
                            currentRes = FleetAndResources.CRYSTAL;
                        } else if (resType.equals("Deuterium:")) {
                            currentRes = FleetAndResources.DEUT;
                        } else {
                            continue;
                        }

                        textData = "";
                        while (textData.length() == 0) {
                            subxpp.next();
                            eventType = subxpp.getEventType();
                            if (eventType == XmlPullParser.TEXT) {
                                textData = subxpp.getText();
                                textData = textData.trim();
                            }
                        }

                        String amount = textData;
                        amount = amount.replaceAll("\\.", "");
                        if (amount.length() > 0) {
                            Long resAmount = Long.valueOf(amount);
                            fleetResData.put(currentRes, resAmount);
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e.toString() + '\n' + e.getMessage());
            e.printStackTrace();
        } finally {
            if (subxpp != null) {
                try {
                    subxpp.setInput(null);
                } catch (XmlPullParserException e) {
                    System.err.println(e.toString() + '\n' + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (strReader != null)
                strReader.close();
        }

        return fleetResData;
    }
}
