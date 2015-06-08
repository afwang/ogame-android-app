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

/**
 * Simple class that provides a simple mapping from mission codes
 * to mission names. For example, the mission code for attack
 * missions is 1. This class also provides constants for the
 * known mission codes.
 * 
 * This class also contains constants for the various missions.
 * Feel free to uses these constants, but if you are implementing
 * a custom mapper between the mission codes and the names,
 * it would be smart to use this class's nested interface.
 * This is recommended since IDEs will tend out point out when
 * the custom implementation might be missing a mission.
 * Moreover, this custom implementation can be registered with
 * an instance of IntegerMissionMap, and IntegerMissionMap will
 * handle all the mapping between the mission codes and calling
 * the proper mission name method. A default implementation is
 * provided internally.
 * @author Alexander Wang
 *
 */
public class IntegerMissionMap {
	public static final int ATTACK = 1;
	public static final int TRANSPORT = 3;
	public static final int DEPLOYMENT = 4;
	public static final int ESPIONAGE = 6;
	public static final int COLONIZE = 7;
	public static final int HARVEST = 8;
	public static final int MOON_DESTRUCTION = 9;
	public static final int EXPEDITION = 15;
	
	private Mapper map;
	
	/**
	 * Initializes a IntegerMissionMap object with
	 * the default internal implementation of the
	 * Mapper interface.
	 */
	public IntegerMissionMap() {
		map = new DefaultMapper();
	}
	
	/**
	 * Initializes this IntegerMissionMap object
	 * with a reference to a custom Mapper.
	 * @param customMap
	 */
	public IntegerMissionMap(Mapper customMap) {
		map = customMap;
	}

	/**
	 * Returns the mission name associated with the integer code. This is needed since
	 * Ogame represents the mission type as an integer rather than a string.
	 * @param missionCode - the integer ID given to the mission by Ogame.
	 * @return the name of the mission corresponding to missionCode
	 */
	public String getMission(int missionCode) {
		String missionName;
		
		switch(missionCode) {
		case ATTACK:
			missionName = map.getAttackName();
			break;
		case TRANSPORT:
			missionName = map.getTransportName();
			break;
		case DEPLOYMENT:
			missionName = map.getDeploymentName();
			break;
		case ESPIONAGE:
			missionName = map.getEspionageName();
			break;
		case COLONIZE:
			missionName = map.getColonizationName();
			break;
		case HARVEST:
			missionName = map.getHarvestName();
			break;
		case MOON_DESTRUCTION:
			missionName = map.getMoonDestructionName();
			break;
		default:
			missionName = "INVALID NAME: " + missionCode;
			break;
		}
		
		return missionName;
	}
	
	public static interface Mapper {
		String getAttackName();
		String getTransportName();
		String getDeploymentName();
		String getEspionageName();
		String getColonizationName();
		String getHarvestName();
		String getMoonDestructionName();
		String getExpeditionName();
	}
	
	private static class DefaultMapper implements Mapper {
		public static final String ATTACK_NAME = "Attack";
		public static final String TRANSPORT_NAME = "Transport";
		public static final String DEPLOYMENT_NAME = "Deployment";
		public static final String ESPIONAGE_NAME = "Espionage";
		public static final String COLONIZE_NAME = "Colonize";
		public static final String HARVEST_NAME = "Harvest";
		public static final String MOON_DESTRUCTION_NAME = "Moon Destruction";
		public static final String EXPEDITION_NAME = "Expedition";
		
		@Override
		public String getAttackName() {
			return ATTACK_NAME;
		}

		@Override
		public String getTransportName() {
			return TRANSPORT_NAME;
		}

		@Override
		public String getDeploymentName() {
			return DEPLOYMENT_NAME;
		}

		@Override
		public String getEspionageName() {
			return ESPIONAGE_NAME;
		}

		@Override
		public String getColonizationName() {
			return COLONIZE_NAME;
		}

		@Override
		public String getHarvestName() {
			return HARVEST_NAME;
		}

		@Override
		public String getMoonDestructionName() {
			return MOON_DESTRUCTION_NAME;
		}

		@Override
		public String getExpeditionName() {
			return EXPEDITION_NAME;
		}
	}
}
