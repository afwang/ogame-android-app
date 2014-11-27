package com.wikaba.ogapp.agent;

public class IntegerMissionMap {
	public static final int ATTACK = 1;
	public static final int TRANSPORT = 3;
	public static final int DEPLOYMENT = 4;
	public static final int ESPIONAGE = 6;
	public static final int HARVEST = 8;
	
	public static final String ATTACK_NAME = "Attack";
	public static final String TRANSPORT_NAME = "Transport";
	public static final String DEPLOYMENT_NAME = "Deployment";
	public static final String ESPIONAGE_NAME = "Espionage";
	public static final String HARVEST_NAME = "Harvest";
	
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
		default:
			missionName = "INVALID NAME: " + missionCode;
			break;
		}
		
		return missionName;
	}
}
