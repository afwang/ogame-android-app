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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;

public class FleetAndResources {
	public static final String METAL = "metal";
	public static final String CRYSTAL = "crystal";
	public static final String DEUT = "deuterium";
	
	public static final String LF = "Light Fighter";
	public static final String HF = "Heavy Fighter";
	public static final String CR = "Cruiser";
	public static final String BS = "Battleship";
	public static final String SC = "Small Cargo";
	public static final String LC = "Large Cargo";
	public static final String COLONY = "Colony Ship";
	public static final String BC = "Battlecruiser";
	public static final String BB = "Bomber";
	public static final String DS = "Destroyer";
	public static final String RIP = "Deathstar";
	public static final String RC = "Recycler";
	public static final String EP = "Espionage Probe";
	public static final String SS = "Solar Satellite";

	private static HashMap<String, String> names;
	private static final int NUM_NAMES = 17;

	/**
	 * Check if the given @{code input} is contained represented
	 * by one of the public static fields of this class. The
	 * internal implementation of this class makes the check
	 * for whether a name is contained an O(1) operation rather
	 * than a search through all the public static fields, an
	 * O(n) operation
	 * @param input - string to check if there is a static field
	 * 		represented by this String
	 * @return
	 */
	public static String getName(String input) {
		if(names == null) {
			initNames();
		}
		return names.get(input.toLowerCase(Locale.US));
	}
	
	private static synchronized void initNames() {
		if(names == null) {
			final float defaultLoadFactor = 0.75F;
			names = new HashMap<String, String>(2 * NUM_NAMES, defaultLoadFactor);

			Class<FleetAndResources> farClass = FleetAndResources.class;
			for(Field field : farClass.getFields()) {
				Class<?> type = field.getClass();
				String typeName = type.getName();
				if(typeName.equals("String")) {
					String value;
					try {
						value = (String)field.get(null);
						names.put(value.toLowerCase(Locale.US), value);
					}
					catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
