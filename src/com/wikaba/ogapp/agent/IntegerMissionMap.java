package com.wikaba.ogapp.agent;

public class IntegerMissionMap {
	public static final int TRANSPORT = 3;
	public static final int DEPLOYMENT = 4;
	
	public static final String TRANSPORT_NAME = "Transport";
	public static final String DEPLOYMENT_NAME = "Deployment";
	
	/**
	 * Returns the mission name associated with the integer code. This is needed since
	 * Ogame represents the mission type as an integer rather than a string.
	 * @param missionCode
	 * @return
	 */
	public static String getMission(int missionCode) {
		String missionName = "INVALID NAME: " + missionCode;
		
		switch(missionCode) {
		case TRANSPORT:
			missionName = TRANSPORT_NAME;
			break;
		case DEPLOYMENT:
			missionName = DEPLOYMENT_NAME;
		}
		
		return missionName;
	}
}
