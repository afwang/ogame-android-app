/**
 * 
 */
package com.wikaba.ogapp.utils;

import android.content.res.Resources;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.IntegerMissionMap;

/**
 * This class is a replacement for IntegerMissionMap's
 * default implementation in the Ogame agent library,
 * since it's preferable to use Android's own resource
 * resolver instead of relying on hard-coded strings.
 * @author Alexander Wang
 *
 */
public class AndroidMissionMap implements IntegerMissionMap.Mapper {
	
	private Resources res;
	
	/**
	 * Initialize this object with a reference to
	 * an Android Resources object. This object is needed
	 * to retrieve the strings resolved by Android.
	 * @param resource Resources object to maintain a reference to
	 * @throws IllegalArgumentException if null is passed in
	 */
	public AndroidMissionMap(Resources resource) {
		if(resource == null) {
			throw new IllegalArgumentException("Resources parameter is null");
		}
		res = resource;
	}

	@Override
	public String getAttackName() {
		return res.getString(R.string.mission_attack);
	}

	@Override
	public String getTransportName() {
		return res.getString(R.string.mission_transport);
	}

	@Override
	public String getDeploymentName() {
		return res.getString(R.string.mission_deploy);
	}

	@Override
	public String getEspionageName() {
		return res.getString(R.string.mission_espionage);
	}

	@Override
	public String getColonizationName() {
		return res.getString(R.string.mission_colonize);
	}

	@Override
	public String getHarvestName() {
		return res.getString(R.string.mission_harvest);
	}

	@Override
	public String getMoonDestructionName() {
		return res.getString(R.string.mission_moon_destruction);
	}

	@Override
	public String getExpeditionName() {
		return res.getString(R.string.mission_expedition);
	}
}