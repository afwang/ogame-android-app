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

package com.wikaba.ogapp.loaders;

import android.support.v4.content.AsyncTaskLoader;

import com.wikaba.ogapp.AgentService;
import com.wikaba.ogapp.HomeActivity;
import com.wikaba.ogapp.agent.FleetEvent;

import java.util.List;

public class FleetEventLoader extends AsyncTaskLoader<List<FleetEvent>> {
	private List<FleetEvent> oldData;
	private HomeActivity act;

	public FleetEventLoader(HomeActivity context) {
		super(context);
		oldData = null;
		act = context;
	}

	@Override
	protected void onStartLoading() {
		if(oldData != null)
			deliverResult(oldData);

		boolean contentChanged = takeContentChanged();
		if(oldData == null || contentChanged) {
			this.forceLoad();
		}
	}

	@Override
	public void deliverResult(List<FleetEvent> data) {
		oldData = data;
		super.deliverResult(data);
	}

	@Override
	protected void onStopLoading() {
	}

	@Override
	protected void onReset() {
		if(oldData != null) {
			oldData.clear();
			oldData = null;
		}
	}

	@Override
	public List<FleetEvent> loadInBackground() {
		AgentService service = act.getAgentService();
		return service.getFleetEvents(act.getAccountRowId());
	}
}
