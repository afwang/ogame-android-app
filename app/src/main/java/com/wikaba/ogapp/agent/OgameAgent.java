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
import com.wikaba.ogapp.agent.parsers.FleetEventParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private FleetEventParser _fleet_event_parser = new FleetEventParser();
    private ReceivedCookiesInterceptor interceptor = new ReceivedCookiesInterceptor();
    private static CookieManager manager = new CookieManager();
    private final static OkHttpClient redirector = new OkHttpClient();
    public static final String LOGIN_URL_ROOT = "http://%s.ogame.gameforge.com/";
    public static final String LOGIN_URL = "http://fr.ogame.gameforge.com/main/login";
    public static final String EVENTLIST_ENDPOINT = "/game/index.php?page=eventList&ajax=1";

    private boolean _is_login;
    private String serverUri;

    public OgameAgent(String universe, String lang) {
        if (universe == null) {
            throw new IllegalArgumentException("OgameAgent constructor argument is null");
        }

        _is_login = false;

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
        _is_login = true;
        interceptor.cleanCookie();

        interceptor.addCookie("deviceId", "deviceId=" + UUID.randomUUID().toString());

        Log.d("LANG", "lang " + lang);

        final int timeoutMillis = 30 * 1000;

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
            } catch (RetrofitError e) {
                if (e != null && e.getResponse() != null) {
                    answer = e.getResponse();
                }
            } catch (Exception error) {
            }

            if (answer != null
                    && (answer.getStatus() == HttpURLConnection.HTTP_OK
                    || ((answer.getStatus() & 300) == 300))) {
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
                _is_login = false;
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
                }
            } catch (Exception e) {
                answer = null;
            }

            if (answer != null
                    && (answer.getStatus() == HttpURLConnection.HTTP_OK
                    || ((answer.getStatus() & 300) == 300))) {
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
                _is_login = false;
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
                error.printStackTrace();
                if (error != null && error.getResponse() != null) {
                    answer = error.getResponse();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (answer != null && answer.getStatus() == HttpURLConnection.HTTP_OK) {
                successfulResponse = true;
                System.out.println("Everything went okay! Response " + answer.getStatus());

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
                _is_login = false;
                return false;
            }
        } catch (Exception e) {
            System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
            e.printStackTrace();
            _is_login = false;
            return false;
        } finally {
        }

        _is_login = false;
        return true;
    }

    /**
     * Tell if the current Agent is currently performing a request of login
     *
     * @return
     */
    public boolean isLogin() {
        return _is_login;
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
        return parseEvents(out);
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
            return parseEvents(strb);
        } catch (IOException e) {
            System.err.println("Error reading the response: " + e + '\n' + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private List<FleetEvent> parseEvents(StringBuilder response) {
        String sub_result = response.toString().replaceAll("&(?![A-Za-z]+;)", "&amp;");
        response = new StringBuilder(sub_result);
        //Removing javascript:
        removeSection(response, "<!-- JAVASCRIPT -->", "<!-- END JAVASCRIPT -->");
        removeSection(response, "<!-- #MMO:NETBAR# -->", "</script>");
        removeSection(response, "<!-- Start Alexa Certify Javascript -->", "</script>");
        removeSection(response, "The relocation allows you to move your planets", "deactivated for 24 hours.");
        removeSection(response, "<div id=\"mmonetbar\" class=\"mmoogame\">", "</script>");


        return _fleet_event_parser.parse(response.toString(), null);
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
}
