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

import android.support.v4.util.LongSparseArray;

import com.squareup.okhttp.OkHttpClient;
import com.wikaba.ogapp.agent.OgameAgent;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusBuilder;

/**
 * <p>This class manages the OgameAgent objects. An integer key is used to map to each agent
 * object. The integer key is the same key as the database key for each account.
 * get()ing an outdated version of an OgameAgent object (unless we have an integer overflow, but
 * that won't happen since users don't really have 2+ billion Ogame accounts).</p>
 *
 * Created by afwang on 9/3/15.
 */
public class OgameAgentManager {
	private static final OgameAgentManager instance = new OgameAgentManager();

	private LongSparseArray<OgameAgentWrapper> agents;

	private OgameAgentManager() {
		agents = new LongSparseArray<>();
	}

	public static OgameAgentManager getInstance() {
		return instance;
	}

	/**
	 * Retrieves the OgameAgent object tied to the given (long) integer {@code key}. If the
	 * agent does not exist in this Manager's internal storage, then null will be returned.
	 *
	 * @param key key of the OgameAgent object. This should have been received from the client
	 * 		when building or adding the OgameAgent object to this manager.
	 * @return OgameAgent object associated with the given key. Null otherwise.
	 */
	public OgameAgent get(long key) {
		OgameAgent anAgent = null;
		//We must synchronize here since there's no guarantee that
		//LongSparseArray's get() method is thread-safe across all
		//implementations of Android.
		synchronized(agents) {
			OgameAgentWrapper wrapper = agents.get(key);
			if(wrapper != null) {
				anAgent = wrapper.getAgent();
			}
		}
		return anAgent;
	}

	/**
	 * <p>If an OgameAgent associated with the given ID in {@code credentials} is already managed
	 * by this instance of OgameAgentManager, then the key to that OgameAgent object is returned.</p>
	 *
	 * <p>Otherwise, an atomic operation takes place:
	 * <ol>
	 * 		<li>An OgameAgent object is built with the given credentials and OkHttpClient.</li>
	 * 		<li>The object is registered to by managed by this manager.</li>
	 * </ol>
	 * </p>
	 *
	 * <p>The key used to register the object with the manager is returned.</p>
	 * @param credentials
	 * @param client
	 * @return
	 */
	public long getOrBuild(AccountCredentials credentials, OkHttpClient client) {
		long id = credentials.getId();
		if(id < 0) {
			return -1;
		}
		OgameAgent test = get(id);
		if(test != null) {
			return id;
		}

		OgameAgent newAgent = new OgameAgent(
				credentials.getUsername(),
				credentials.getPasswd(),
				credentials.getUniverse(),
				credentials.getLang(),
				client
		);
		EventBusBuilder busBuilder = EventBus.builder();
		EventBus agentBus = busBuilder.build();
		synchronized(agents) {
			//Check once more to ensure that the agent is not part of the LongSparseArray.
			//There may be another concurrent call to getOrBuild that added the same object
			//before us.
			OgameAgentWrapper wrapper = agents.get(id);
			if(wrapper == null) {
				wrapper = new OgameAgentWrapper(newAgent, agentBus);
				agents.append(id, wrapper);
			}
		}
		return id;
	}

	/**
	 * Removes the OgameAgent object tied to the given (long) integer {@code key} from this
	 * manager.
	 *
	 * @param key key of the OgameAgent object. This should have been received from the client
	 * 		when building or adding the OgameAgent object to this manager.
	 */
	public void remove(long key) {
		//We must synchronize here since there's no guarantee that
		//LongSparseArray's remove() method is thread-safe across all
		//implementations of Android.
		synchronized(agents) {
			agents.remove(key);
		}
	}
}