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


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ResponseBody;
import com.wikaba.ogapp.agent.constants.ItemRepresentationConstant;
import com.wikaba.ogapp.agent.constants.OgameAgentState;
import com.wikaba.ogapp.agent.interfaces.IWebservice;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.agent.models.ItemRepresentation;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.agent.models.PlanetResources;
import com.wikaba.ogapp.agent.parsers.PlanetResourceParser;
import com.wikaba.ogapp.agent.parsers.pages.FleetEventParser;
import com.wikaba.ogapp.agent.parsers.pages.ResourcesParser;
import com.wikaba.ogapp.utils.AccountCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 *
 * @author afwang
 */
public class OgameAgent {
	public static final Logger logger = LoggerFactory.getLogger(OgameAgent.class);

	public static final String LOGIN_URL_ROOT = "http://%s.ogame.gameforge.com/";

	private OkHttpClient httpClient;
	private IWebservice callCreator;
	private FleetEventParser _fleet_event_parser = new FleetEventParser();
	private ResourcesParser _resources_parser = new ResourcesParser();

	private boolean _is_login;
	private String serverUri;

	private String _temporary_universe;
	private String universe;
	private String username;
	private String password;
	private String lang;
	private int agentState;
	private Object lastRetrievedData;

	/**
	 * <p>Create an OgameAgent object with the given credentials and HTTP client.
	 * The client should be configured with a CookieHandler that can handle Ogame's cookies.</p>
	 *
	 * <p>There is a bug where the CookieHandler's CookieStore will drop some essential cookies needed for
	 * Ogame. This is the case in older versions of Java 6, where some non-standard cookie attributes
	 * cause the CookieStore to drop the cookie (by throwing an Exception) rather than silently ignoring
	 * the non-standard cookie attribute. If you just need a CookieHandler without wanting to write
	 * one of your own that can work with Ogame's cookies, consider using {@link CustomCookieManager}</p>
	 * @param username Username of the account to associate this OgameAgent with
	 * @param passwd Password of the account to associate this OgameAgent with
	 * @param universe Universe name of account
	 * @param lang Language of the Universe
	 * @param client HTTP client used to make requests for playing the game
	 * @throws IllegalArgumentException if any of the parameters are null.
	 */
	public OgameAgent(String username, String passwd, String universe, String lang, OkHttpClient client) {
		if(username == null
			|| passwd == null
			|| universe == null
			|| lang == null
			|| client == null) {
			throw new IllegalArgumentException("None of the parameters passed to OgameAgent constructor can be null");
		}

		this.username = username;
		this.password = passwd;
		this.lang = lang;
		this.universe = universe;
		agentState = OgameAgentState.LOGGED_OUT;
		lastRetrievedData = null;
		this.universe = String.format(NameToURI.getDomain(this.universe), lang);
		serverUri = "http://" + universe;
		httpClient = client;
		Retrofit adapterBuilder = createAdapter(serverUri);
		callCreator = adapterBuilder.create(IWebservice.class);
	}

	@Deprecated
	public OgameAgent(String universe, String lang, OkHttpClient client) {
		if (universe == null) {
			throw new IllegalArgumentException("OgameAgent constructor argument is null");
		}

		_is_login = false;

		//TODO getDOmain(), fr to String universe, String language

		agentState = OgameAgentState.LOGGED_OUT;
		lastRetrievedData = null;
		this.universe = String.format(NameToURI.getDomain(universe), lang);
		serverUri = "http://" + universe;
		httpClient = client;
		Retrofit adapterBuilder = createAdapter(serverUri);
		callCreator = adapterBuilder.create(IWebservice.class);
	}

	private Retrofit createAdapter(String base) {
		Retrofit.Builder rfb = new Retrofit.Builder();
		rfb.baseUrl(base);
		rfb.client(httpClient);
		return rfb.build();
	}

	/**
	 * <p>Submits user credentials to Ogame to obtain set of cookies (handled
	 * by system's CookieHandler.
	 * <p/>
	 *
	 * This method's purpose is to add the cookies into the cookie store managed by
	 * this Agent.
	 *
	 * @return true on successful login, false on failure
	 */
	public boolean login() {
		boolean successfulResponse;

		String uri = String.format(LOGIN_URL_ROOT, lang);
		Retrofit adapter = createAdapter(uri);
		IWebservice loginInstance = adapter.create(IWebservice.class);

		logger.debug("START FIRST REQUEST (login)");
		Call<ResponseBody> loginCaller = loginInstance.loginStep1("", universe, username, password);
		Response<ResponseBody> answer;
		try {
			answer = loginCaller.execute();
		} catch(IOException e) {
			logger.error("Could not perform login", e);
			answer = null;
		}

		if (answer != null) {
			int code = answer.code();
			successfulResponse = code == HttpURLConnection.HTTP_OK;
			agentState = OgameAgentState.LOGGED_IN;
		} else {
			successfulResponse = false;
			logger.error("Could not perform login");
			agentState = OgameAgentState.LOGGED_OUT;
		}
		logger.debug("END FIRST REQUEST (login)");

		return successfulResponse;
	}

	/**
	 * Returns the state of this OgameAgent. A description of states is listed in {@link OgameAgentState}.
	 *
	 * @return the current state of this OgameAgent
	 */
	public int getState() {
		return agentState;
	}

	public PlanetResources getResourcePagesContent() throws LoggedOutException {
		return getResourcePagesContent(true);
	}

	private PlanetResources getResourcePagesContent(boolean retry) throws LoggedOutException {
		try {
			Call<ResponseBody> resourceCall = callCreator.getSinglePage("resources");
			Response<ResponseBody> response = resourceCall.execute();
			String res = consumeResponseToString(response).toString();
			PlanetResourceParser parser = new PlanetResourceParser();
			return parser.parse(res, null);
		} catch(IOException e) {
			logger.error("Could not retrieve resources page. Did we log out?", e);
			throw new LoggedOutException(e.getMessage());
		}
	}

	public List<AbstractItemInformation> getItemFromPage(ItemRepresentationConstant item_to_fetch) {
		return getItemFromPage(item_to_fetch, true);
	}

	private List<AbstractItemInformation> getItemFromPage(
			ItemRepresentationConstant item_to_fetch,
			boolean retry) {
		List<AbstractItemInformation> items = new ArrayList<>();
		try {
			List<ItemRepresentation> list = item_to_fetch.toList();

			AbstractItemInformation tmp_information;

			for (ItemRepresentation item : list) {
				Call<ResponseBody> call = callCreator.getSinglePageFromCategory(item.getPage(), 1, item.getIndex());
				Response<ResponseBody> response = call.execute();
				String raw = consumeResponseToString(response).toString();
				tmp_information = item.getParser().parse(raw, null);

				if (tmp_information != null) {
					tmp_information.setItemRepresentation(item);
					items.add(tmp_information);
				}
			}
		} catch(IOException e) {
			logger.error("Unable to retrieve item", e);
			items = null;
		}
		return items;
	}

	/**
	 * Emulate a user clicking the "Overview" link in the navigation bar/column. Also parse fleet
	 * movement data from the returned response (if available). This method will make a network
	 * request to retrieve the data. The data will be
	 *
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * will have non-null instance variables.
	 */
	public OverviewData loadOverviewData() throws LoggedOutException {
		OverviewData overviewData = null;

		try {
			Call<ResponseBody> overviewCall = callCreator.getSinglePage("overview");
			Response<ResponseBody> overviewResponse = overviewCall.execute();
			overviewData = consumeFleetEventFrom(overviewResponse);
		} catch(IOException e) {
			logger.error("Could not load overview data", e);
			throw new LoggedOutException("Agent's cookies are no longer valid");
		}

		if (overviewData == null || overviewData._fleet_event == null ||
				overviewData._fleet_event.size() == 0) {
			overviewData = getFleetEvents();
		}

		agentState = OgameAgentState.AT_OVERVIEW;
		lastRetrievedData = overviewData;

		return overviewData;
	}

	/**
	 * Returns the cached result from loadOverviewData. No other load*() methods should be
	 * called between loadOverviewData() and this method. Otherwise, an IllegalStateException
	 * will be thrown
	 * @return cached OverviewData result from the last call to loadOverviewData
	 * @throws IllegalStateException if a different load*() method was called between loadOverviewData
	 * 		and this method.
	 */
	public OverviewData getOverviewData() {
		if(agentState != OgameAgentState.AT_OVERVIEW) {
			throw new IllegalStateException(
					"OgameAgent object has not called loadOverviewData immediately before getOverviewData");
		}
		OverviewData data = (OverviewData)lastRetrievedData;
		return data;
	}

	/**
	 * <p>Parse fleet movement data from the returned response from the event list endpoint. Ogame
	 * provides a convenient javascript call so that the fleet events can be retrieved on any page.</p>
	 *
	 * <p>TODO: Currently, the agent assumes the game behaves the way it does in Ogame legacy. That is,
	 * the only way to view the fleet events is through the overview. This method should be made
	 * public in the future to support returning fleet events on any page. This is not done at the
	 * current moment since fleet event data is bundled with an OverviewData object.</p>
	 *
	 * @return If no errors occurred while extracting the data, a list of fleet movements with details
	 * is returned. Otherwise, null is returned. Every FleetEvent entry in the returned List
	 * will have non-null instance variables.
	 */
	private OverviewData getFleetEvents() throws LoggedOutException {
		OverviewData data = null;
		try {
			Call<ResponseBody> fleetEventsCall = callCreator.getSinglePageWithAjaxParameter("eventList", 1);
			Response<ResponseBody> fleetEventsResp = fleetEventsCall.execute();
			data = consumeFleetEventFrom(fleetEventsResp);
			agentState = OgameAgentState.AT_OVERVIEW;
			lastRetrievedData = data;
		} catch(IOException e) {
			logger.error("Could not retrieve fleet events", e);
			throw new LoggedOutException("Agent's cookies are no longer valid");
		}
		return data;
	}

	/**
	 * Convert a call for fleet event to an actual list of such items
	 *
	 * @return If no errors occurred while extracting the data, a list of @NotNull fleet movements with details
	 * is returned. Otherwvise, null is returned
	 */
	private OverviewData consumeFleetEventFrom(Response<ResponseBody> response) {
		return parseEvents(consumeResponseToString(response));
	}

	public StringBuilder consumeResponseToString(Response<ResponseBody> response) {
		ResponseBody body = response.body();
		StringBuilder out = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream()));
			out = new StringBuilder();
			String newLine = System.getProperty("line.separator");
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(newLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	private OverviewData parseEvents(StringBuilder response) {
		String sub_result = response.toString().replaceAll("&(?![A-Za-z]+;)", "&amp;");
		response = new StringBuilder(sub_result);
		return _fleet_event_parser.parse
				(response.toString(), null);
	}
}
