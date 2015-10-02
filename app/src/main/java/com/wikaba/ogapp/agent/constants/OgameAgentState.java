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
package com.wikaba.ogapp.agent.constants;

/**
 * <p>This class contains integer constants used to represent the state of an OgameAgent.
 * Descriptions for each individual state are given below.</p>
 *
 * Created by afwang on 9/7/15.
 */
public class OgameAgentState {
	/**
	 * The agent object has only either been created, or the Ogame server forced kick-out has
	 * occurred.
	 */
	public static final int LOGGED_OUT = -1;

	/**
	 * The login method was last called (and was successful). Unfortunately, there is no guarantee
	 * that the next call to the OgameAgent will be successful, since the login might have occurred
	 * at 01:59:59 (Ogame server time), and the next call occurs at 02:00:01; a LoggedOutException
	 * would be thrown for the next call since the server kickoff has occurred. However, this
	 * status can be used to note when the client should try logging in again on the OgameAgent,
	 * assuming this process is not done while handling the thrown LoggedOutException.
	 */
	public static final int LOGGED_IN = 0;

	/**
	 * The overview screen method was last called and was successful. When the OgameAgent is at
	 * this state, the next call to retrieve the overview screen's data is guaranteed to be
	 * successful.
	 */
	public static final int AT_OVERVIEW = 1;

}
