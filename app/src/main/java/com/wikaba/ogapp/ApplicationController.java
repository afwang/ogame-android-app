/*
	Copyright 2014 Kevin Le Perf

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

import android.app.Application;

import com.wikaba.ogapp.database.DatabaseManager;
import com.wikaba.ogapp.utils.OgameAgentManager;

/**
 * Created by kevinleperf on 18/07/15.
 */
public class ApplicationController extends Application {
	private static ApplicationController _static_application_controller;

	public static ApplicationController getInstance() {
		return _static_application_controller;
	}

	private DatabaseManager _database_manager;
	private OgameAgentManager agentManager;

	@Override
	public void onCreate() {
		super.onCreate();

		_static_application_controller = this;
		_database_manager = DatabaseManager.getInstance();
		_database_manager.startSession(this);
		agentManager = OgameAgentManager.getInstance();
	}

	public OgameAgentManager getAgentManager() {
		return agentManager;
	}
}
