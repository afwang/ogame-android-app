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

package com.wikaba.ogapp.utils;

import android.content.res.Resources;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.FleetAndResources;

/**
 * This class acts as a bridge between the Ogame agent library and the Android
 * system's resource resolver. For example, the Ogame agent library uses the
 * names of ships and resources in English with their full names. This does not
 * bode well for non-English speakers or on devices with small screen size.
 * 
 * Fortunately, the Android's resource resolver solves the problem of localizing
 * quite nicely. This class will form mapping relations from the Ogame agent's use of
 * full English names to the strings generated from Android.
 * 
 * An example use of this class:
 * 		Resources res = context.getResources();
 * 		NameBridge bridge = new NameBridge(res);
 * 		String localizedLightFighterName = bridge.getName(FleetAndResources.LF);
 *
 */
public class NameBridge {
	private static final int[] resids = {
		R.string.metal,
		R.string.crystal,
		R.string.deuterium,
		R.string.lightfighter,
		R.string.heavyfighter,
		R.string.cruiser,
		R.string.battleship,
		R.string.smallcargo,
		R.string.largecargo,
		R.string.colony,
		R.string.battlecruiser,
		R.string.bomber,
		R.string.destroyer,
		R.string.deathstar,
		R.string.recycler,
		R.string.probe,
		R.string.satellite
	};
	private static final String[] resourceKeys = {
		FleetAndResources.METAL,
		FleetAndResources.CRYSTAL,
		FleetAndResources.DEUT,
		FleetAndResources.LF,
		FleetAndResources.HF,
		FleetAndResources.CR,
		FleetAndResources.BS,
		FleetAndResources.SC,
		FleetAndResources.LC,
		FleetAndResources.COLONY,
		FleetAndResources.BC,
		FleetAndResources.BB,
		FleetAndResources.DS,
		FleetAndResources.RIP,
		FleetAndResources.RC,
		FleetAndResources.EP,
		FleetAndResources.SS
	};
	private static final int size = resourceKeys.length;
	
	private Resources res;
	
	/**
	 * Create a NameBridge object using a Resources object acquired
	 * from the Android framework. 
	 * @param res - resource object used for retrieving strings from resouces
	 */
	public NameBridge(Resources res) {
		this.res = res;
	}

	/**
	 * Retrieve the string resolved by Android's resolver that corresponds to
	 * nameInLibrary. For example, passing in FleetAndResources.LF will return
	 * whatever string Android resolves for light fighters.
	 * 
	 * @param nameInLibrary - string used in Ogame agent library
	 * @return string resolved by Android, or null when nameInLibrary does not
	 * 		match any string from Ogame agent's FleetAndResources class.
	 * @throws NotFoundException when no resource is found by Android
	 */
	public String getName(String nameInLibrary) {
		for(int index = 0; index < size; index++) {
			if(nameInLibrary.equals(resourceKeys[index])) {
				String androidName = res.getString(resids[index]);
				return androidName;
			}
		}
		return null;
	}
}
