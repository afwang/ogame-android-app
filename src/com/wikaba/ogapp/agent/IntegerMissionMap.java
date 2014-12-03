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

public class IntegerMissionMap {
	public static final int ATTACK = 1;
	public static final int TRANSPORT = 3;
	public static final int DEPLOYMENT = 4;
	public static final int ESPIONAGE = 6;
	public static final int HARVEST = 8;
	public static final int MOON_DESTRUCTION = 9;
	
	public static final String ATTACK_NAME = "Attack";
	public static final String TRANSPORT_NAME = "Transport";
	public static final String DEPLOYMENT_NAME = "Deployment";
	public static final String ESPIONAGE_NAME = "Espionage";
	public static final String HARVEST_NAME = "Harvest";
	public static final String MOON_DESTRUCTION_NAME = "Moon Destruction";
	
	/**
	 * Returns the mission name associated with the integer code. This is needed since
	 * Ogame represents the mission type as an integer rather than a string.
	 * @param missionCode - the integer ID given to the mission by Ogame.
	 * @return the name of the mission corresponding to missionCode
	 */
	public static String getMission(int missionCode) {
		String missionName;
		
		switch(missionCode) {
		case ATTACK:
			missionName = ATTACK_NAME;
			break;
		case TRANSPORT:
			missionName = TRANSPORT_NAME;
			break;
		case DEPLOYMENT:
			missionName = DEPLOYMENT_NAME;
			break;
		case ESPIONAGE:
			missionName = ESPIONAGE_NAME;
			break;
		case HARVEST:
			missionName = HARVEST_NAME;
			break;
		case MOON_DESTRUCTION:
			missionName = MOON_DESTRUCTION_NAME;
			break;
		default:
			missionName = "INVALID NAME: " + missionCode;
			break;
		}
		
		return missionName;
	}
}
