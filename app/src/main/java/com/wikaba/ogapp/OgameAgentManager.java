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

package com.wikaba.ogapp;

import android.support.v4.util.LongSparseArray;

import com.squareup.okhttp.OkHttpClient;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.utils.AccountCredentials;

/**
 * <p>This class manages the OgameAgent objects. An integer key is used to map to each agent
 * object. The integer key only grows in size, so we do not need to worry about a consumer
 * get()ing an outdated version of an OgameAgent object (unless we have an integer overflow, but
 * that won't happen since users don't really have 2+ billion Ogame accounts).</p>
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

	/**
	 * Build the OgameAgent with the given AccountCredentials.
	 * @param credentials
	 * @return the integer (long) key for the OgameAgent within this OgameAgentManager's map
	 */
	public long buildOgameAgent(AccountCredentials credentials, OkHttpClient httpClient) {
		OgameAgent newAgent = new OgameAgent(
				credentials.username,
				credentials.passwd,
				credentials.universe,
				credentials.lang,
				httpClient
		);
		long id = credentials.id;
		synchronized(agents) {
			agents.append(id, newAgent);
		}
		return id;
	}

	public OgameAgent get(long key) {
		OgameAgent anAgent = null;
		//We must synchronize here since there's no guarantee that
		//LongSparseArray's get() method is thread-safe across all
		//implementations of Android.
		synchronized(agents) {
			anAgent = agents.get(key);
		}
		return anAgent;
	}

	public void remove(long key) {
		//We must synchronize here since there's no guarantee that
		//LongSparseArray's remove() method is thread-safe across all
		//implementations of Android.
		synchronized(agents) {
			agents.remove(key);
		}
	}
}