package com.wikaba.ogapp.utils;

import com.wikaba.ogapp.agent.OgameAgent;

import de.greenrobot.event.EventBus;

/**
 * <p>The agent package should be separated from the application code so that it can be easily
 * moved into its own library in the future. This class is used to associate some Android-specific data
 * with an OgameAgent without modifying the source code of the agent package.</p>
 *
 * Created by afwang on 9/27/15.
 */
public class OgameAgentWrapper {
	private OgameAgent agent;
	private EventBus bus;

	public OgameAgentWrapper(OgameAgent theAgent, EventBus eventBus) {
		agent = theAgent;
		bus = eventBus;
	}

	public OgameAgent getAgent() {
		return agent;
	}

	public EventBus getBus() {
		return bus;
	}
}
