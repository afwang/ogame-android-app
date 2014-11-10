package com.wikaba.ogapp.agent;

import java.util.HashMap;

/**
 * This class provides a mapping from universe names to the domains of the universes.
 * This class is not thread-safe. Perhaps it will be modified in the future to be thread-safe.
 * e.g. "Zagadra" would be mapped to "s126-en.ogame.gameforge.com"
 * @author afwang
 *
 */
public class NameToURI {
	private static final HashMap<String, String> nameMap = new HashMap<String, String>();
	private static boolean initializedYet = false;
	
	/*
	 * This method should be used to populate nameMap with the mappings from universe names to domains
	 */
	private static void init() {
		initializedYet = true;
		//TODO: Populate the map with the relations.
		nameMap.put("Zagadra", "s126-en.ogame.gameforge.com");
	}
	
	/**
	 * Retrieves the domain for the universe named by universeName.
	 * @param universeName - the name of the universe
	 * @return the domain of the universe named by universeName
	 */
	public static String getDomain(String universeName) {
		if(!initializedYet) {
			init();
		}
		
		return nameMap.get(universeName);
	}
}
