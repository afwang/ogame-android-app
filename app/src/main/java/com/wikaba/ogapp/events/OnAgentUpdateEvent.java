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

	You should have received a copy of the GNU General Public License along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wikaba.ogapp.events;

/**
 * Created by afwang on 9/7/15.
 */
public class OnAgentUpdateEvent {
	private int agentKey;
	private boolean updateSucceeded;

	public OnAgentUpdateEvent() {
		agentKey = -1;
		updateSucceeded = false;
	}

	public OnAgentUpdateEvent(int agentManagerKey) {
		agentKey = agentManagerKey;
		updateSucceeded = true;
	}

	public OnAgentUpdateEvent(int agentManagerKey, boolean wasUpdateSuccessful) {
		agentKey = agentManagerKey;
		updateSucceeded = wasUpdateSuccessful;
	}

	public boolean getWasSuccessful() {
		return updateSucceeded;
	}

	public int getAgentManagerKey() {
		return agentKey;
	}
}
