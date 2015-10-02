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

	You should have received a copy of the GNU General Public License along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp;

/**
 * <p>Class of constants needed to determine which action to run for {@link AgentService}. All
 * codes added to this class should be positive. Negative integer codes are reserved for
 * errors.</p>
 * Created by afwang on 9/5/15.
 */
public class AgentActions {
	public static final String AGENT_ACTION_KEY = "com.wikaba.ogapp.AGENT_ACTION";
	public static final String OGAME_AGENT_KEY = "com.wikaga.ogapp.OGAME_AGENT";
	public static final String ACCOUNT_CREDENTIAL_KEY = "com.wikaba.ogapp.ACCOUNT_CREDENTIALS";

	public static final int LOGIN = 0;
	public static final int OVERVIEW = 1;
	public static final int RESOURCES = 2;
}
