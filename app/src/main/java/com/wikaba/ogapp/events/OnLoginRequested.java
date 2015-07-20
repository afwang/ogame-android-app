/*
	Copyright 2015 Kevin Le Perf

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

package com.wikaba.ogapp.events;

import com.wikaba.ogapp.utils.AccountCredentials;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class OnLoginRequested {
    private AccountCredentials _account_crendentials;


    public OnLoginRequested(AccountCredentials credentials) {
        _account_crendentials = credentials;
    }

    public AccountCredentials getAccountCredentials() {
        return _account_crendentials;
    }

	@Override
	public String toString(){
		return _account_crendentials.toString();
	}
}
