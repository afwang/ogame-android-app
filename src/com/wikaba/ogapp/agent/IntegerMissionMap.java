package com.wikaba.ogapp.agent;

public class IntegerMissionMap {
	public static final int TRANSPORT = 3;
	
	public static final String TRANSPORT_NAME = "Transport";
	
	/**
	 * Returns the mission name associated with the integer code. This is needed since
	 * Ogame represents the mission type as an integer rather than a string.
	 * @param missionCode
	 * @return
	 */
	public static String getMission(int missionCode) {
		String missionName = null;
		
		switch(missionCode) {
		case TRANSPORT:
			missionName = TRANSPORT_NAME;
			break;
		}
		
		return missionName;
	}
}
