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


import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.wikaba.ogapp.agent.interfaces.IWebservice;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 *
 * @author afwang
 */
public class OgameAgent {
    private ReceivedCookiesInterceptor interceptor = new ReceivedCookiesInterceptor();
    private static CookieManager manager = new CookieManager();
    private final static OkHttpClient redirector = new OkHttpClient();
    public static final String LOGIN_URL_ROOT = "http://%s.ogame.gameforge.com/";
    public static final String LOGIN_URL = "http://fr.ogame.gameforge.com/main/login";
    public static final String EVENTLIST_ENDPOINT = "/game/index.php?page=eventList&ajax=1";

    private String serverUri;

    public OgameAgent(String universe, String lang) {
        if (universe == null) {
            throw new IllegalArgumentException("OgameAgent constructor argument is null");
        }

        //TODO getDOmain(), fr to String universe, String language
        serverUri = "http://" + String.format(NameToURI.getDomain(universe), lang);
    }

    private RestAdapter createLoginAdapter(String base) {
        CookieHandler.setDefault(manager);
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        redirector.setFollowRedirects(false);
        redirector.interceptors().add(interceptor);

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(base)
                .setLogLevel(RestAdapter.LogLevel.HEADERS)
                .setRequestInterceptor(interceptor)
                .setClient(new OkClient(redirector))
                .build();
        return adapter;
    }

    private class UriCut {
        public String root;
        public String path;
        public HashMap<String, String> parameters;
    }

    private UriCut getRoot(String uri, String split) {
        UriCut cut = new UriCut();
        String[] splitted = uri.split(split);

        Log.d("TAG", "splitted " + Arrays.toString(splitted));
        if (splitted.length > 1) {
            cut.root = splitted[0] + split;
            splitted = splitted[1].split("\\?");
            cut.path = splitted[0];
            String parameters = null;
            if (splitted.length > 1) parameters = splitted[1];

            if (parameters != null) {
                HashMap<String, String> params = new HashMap<>();
                String[] queries = parameters.split("&");
                String[] sub_query;
                for (String query : queries) {
                    sub_query = query.split("=");
                    String right = sub_query.length > 1 ? sub_query[1] : "";
                    try {
                        params.put(sub_query[0], URLDecoder.decode(right, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                cut.parameters = params;
            }
        }
        return cut;
    }

    private Header getHeader(List<Header> headers, String name) {
        //TODO make this research efficient by storing all header in map instead of the raw list
        for (Header header : headers) {
            if (header != null && header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
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
        interceptor.cleanCookie();

        interceptor.addCookie("deviceId", "deviceId=" + UUID.randomUUID().toString());

        Log.d("LANG", "lang " + lang);

        final int timeoutMillis = 30 * 1000;
        int response = 0;

        boolean successfulResponse;

        universe = String.format(NameToURI.getDomain(universe), lang);
        serverUri = "http://" + universe;

        //Unfortunately, we have to be a bit more direct about
        //following these redirects because Ogame returns cookies that
        //causes Java to throw an error when parsed (might want to
        //look into a different HTTP library for this, like OkHttp).

        RestAdapter adapter = createLoginAdapter(String.format("http://%s.ogame.gameforge.com", lang));
        IWebservice login_instance = adapter.create(IWebservice.class);

        try {
            Response reponse = login_instance.getMain();
        } catch (Exception e) {
            e.printStackTrace();
        }

		/*
         * FIRST REQUEST
		 */
        System.out.println("START FIRST REQUEST (login)");
        String uri = String.format(LOGIN_URL_ROOT, lang);
        try {
            adapter = createLoginAdapter(uri);
            login_instance = adapter.create(IWebservice.class);

            Response answer = null;
            try {
                answer = login_instance.loginStep1("", universe, username, password);
                response = answer.getStatus();
            } catch (RetrofitError e) {
                if (e != null && e.getResponse() != null) {
                    answer = e.getResponse();
                    response = answer.getStatus();
                }
            } catch (Exception error) {
            }

            if (answer != null && (response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400))) {
                successfulResponse = true;
                Header header = getHeader(answer.getHeaders(), "Location");
                if (header != null && header.getValue() != null) {
                    uri = header.getValue();
                }
            } else {
                successfulResponse = false;
                Log.d("TAG", "Something went wrong!");
            }
            Log.d("TAG", "END FIRST REQUEST (login)");

            if (!successfulResponse) {
                return false;
            }

			/*
             * SECOND REQUEST
			 */
            Log.d("TAG", "START SECOND REQUEST " + uri);

            UriCut root = getRoot(uri, "gameforge.com/");
            adapter = createLoginAdapter(root.root);
            Log.d("TAG", "having root = " + root.root);
            login_instance = adapter.create(IWebservice.class);

            try {
                answer = login_instance.loginStep2(root.parameters.get("data"));
            } catch (RetrofitError error) {
                answer = null;
                if (error != null && error.getResponse() != null) {
                    answer = error.getResponse();
                    response = error.getResponse().getStatus();
                }
            } catch (Exception e) {
                answer = null;
            }

            if (answer != null && (response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400))) {
                successfulResponse = true;
                Header locationHeader = getHeader(answer.getHeaders(), "Location");
                if (locationHeader != null) {
                    uri = locationHeader.getValue();
                }
            } else {
                successfulResponse = false;
            }
            System.out.println("END SECOND REQUEST");
            if (!successfulResponse) {
                return false;
            }

            /*
             * THIRD REQUEST (final request)
			 */
            root = getRoot(uri, "gameforge.com/");
            adapter = createLoginAdapter(root.root);
            login_instance = adapter.create(IWebservice.class);


            serverUri = root.root;

            //remove last server cookie
            interceptor.deleteCookie("OG_lastServer");
            try {
                answer = login_instance.loginStep3(root.parameters.get("page"));
            } catch (RetrofitError error) {
                if (error != null && error.getResponse() != null) {
                    answer = error.getResponse();
                }
            } catch (Exception e) {
            }
            if (answer != null && response == HttpURLConnection.HTTP_OK) {
                successfulResponse = true;
                System.out.println("Everything went okay! Response " + response);

                List<Header> headers = answer.getHeaders();

                Header locationHeader = getHeader(headers, "Location");
                if (locationHeader != null) {
                    uri = locationHeader.getValue();
                }
            } else {
                successfulResponse = false;
                System.err.println("Something went wrong!");
            }
            System.out.println("END THIRD REQUEST");
            if (!successfulResponse) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
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
        List<FleetEvent> overviewData;
        RestAdapter adapter = createLoginAdapter(serverUri);
        Log.d("TAG", "having root = " + serverUri);
        IWebservice instance = adapter.create(IWebservice.class);

        try {
            Response overview = instance.getSinglePage("overview");
            overviewData = consumeFleetEventFrom(overview);
        } catch (RetrofitError error) {
            error.printStackTrace();
            throw new LoggedOutException("Agent's cookies are no longer valid");
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
        RestAdapter adapter = createLoginAdapter(serverUri);
        Log.d("TAG", "having root = " + serverUri);
        IWebservice instance = adapter.create(IWebservice.class);

        try {
            Response overview = instance.getSinglePageWithAjaxParameter("eventList", 1);
            return consumeFleetEventFrom(overview);
        } catch (RetrofitError error) {
            throw new LoggedOutException("Agent's cookies are no longer valid");
        }
    }

    /**
     * Convert a call for fleet event to an actual list of such items
     *
     * @return If no errors occurred while extracting the data, a list of @NotNull fleet movements with details
     * is returned. Otherwvise, null is returned
     */
    private List<FleetEvent> consumeFleetEventFrom(Response response) {
        TypedInput body = response.getBody();
        StringBuilder out = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
            out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            System.out.println(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseEvents(out.toString());
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
        } catch (IOException e) {
            System.err.println("Error reading the response: " + e + '\n' + e.getMessage());
            e.printStackTrace();
        }
        return parseEvents(response);
    }

    private List<FleetEvent> parseEvents(String response) {
        List<FleetEvent> eventList = new LinkedList<FleetEvent>();

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
