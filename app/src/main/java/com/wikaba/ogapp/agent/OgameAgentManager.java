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

import android.support.v4.util.LongSparseArray;

/**
 * <p>This class manages the OgameAgent objects. An integer key is used to map to each agent
 * object. The integer key only grows in size, so we do not need to worry about a consumer
 * get()ing an outdated version of an OgameAgent object (unless we have an integer overflow, but
 * that won't happen since users don't really have 2+ billion Ogame accounts)</p>
 *
 * Created by afwang on 9/3/15.
 */
public class OgameAgentManager {
	private static final OgameAgentManager instance = new OgameAgentManager();

	private volatile long key;
	private volatile LongSparseArray<OgameAgent> agents;

	private OgameAgentManager() {
		key = 0;
		agents = new LongSparseArray<>();
	}

	public static OgameAgentManager getInstance() {
		return instance;
	}

	public void add(OgameAgent theAgent) {
		synchronized(agents) {
			agents.append(key, theAgent);
			key++;
		}
	}

	public OgameAgent get(long key) {
		OgameAgent anAgent = null;
		synchronized(agents) {
			anAgent = agents.get(key);
		}
		return anAgent;
	}

	public void remove(long key) {
		synchronized(agents) {
			agents.remove(key);
		}
	}
}