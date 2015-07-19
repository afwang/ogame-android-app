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

import java.util.HashMap;

/**
 * This class provides a mapping from universe names to the domains of the universes.
 * This class is not thread-safe. Perhaps it will be modified in the future to be thread-safe.
 * e.g. "Zagadra" would be mapped to "s126-en.ogame.gameforge.com"
 *
 * @author afwang
 */
public class NameToURI {
    protected static final HashMap<String, String> nameMap = new HashMap<String, String>();
    private static boolean initializedYet = false;

    /*
     * This method should be used to populate nameMap with the mappings from universe names to domains
     */
    private static void init() {
        initializedYet = true;

        nameMap.put("Ganimede", "s133-%s.ogame.gameforge.com");
        nameMap.put("Antares", "s127-%s.ogame.gameforge.com");
        nameMap.put("Andromeda", "s101-%s.ogame.gameforge.com");
        nameMap.put("Betelgeuse", "s128-%s.ogame.gameforge.com");
        nameMap.put("Electra", "s105-%s.ogame.gameforge.com");
        nameMap.put("Jupiter", "s110-%s.ogame.gameforge.com");
        nameMap.put("Nekkar", "s114-%s.ogame.gameforge.com");
        nameMap.put("Orion", "s115-%s.ogame.gameforge.com");
        nameMap.put("Pegasus", "s116-%s.ogame.gameforge.com");
        nameMap.put("Quantum", "s117-%s.ogame.gameforge.com");
        nameMap.put("Rigel", "s118-%s.ogame.gameforge.com");
        nameMap.put("Sirius", "s119-%s.ogame.gameforge.com");
        nameMap.put("Taurus", "s120-%s.ogame.gameforge.com");
        nameMap.put("Ursa", "s121-%s.ogame.gameforge.com");
        nameMap.put("Vega", "s122-%s.ogame.gameforge.com");
        nameMap.put("Wasat", "s123-%s.ogame.gameforge.com");
        nameMap.put("Xalynth", "s124-%s.ogame.gameforge.com");
        nameMap.put("Yakini", "s125-%s.ogame.gameforge.com");
        nameMap.put("Zagadra", "s126-%s.ogame.gameforge.com");
        nameMap.put("Universe 1", "s1-%s.ogame.gameforge.com");
        nameMap.put("Universe 20", "s20-%s.ogame.gameforge.com");
        nameMap.put("Universe 30", "s30-%s.ogame.gameforge.com");
        nameMap.put("Universe 35", "s35-%s.ogame.gameforge.com");
        nameMap.put("Universe 44", "s44-%s.ogame.gameforge.com");
    }

    /**
     * Retrieves the domain for the universe named by universeName.
     *
     * @param universeName - the name of the universe
     * @return the domain of the universe named by universeName
     */
    public static String getDomain(String universeName) {
        if (!initializedYet) {
            init();
        }

        return nameMap.get(universeName);
    }
}
