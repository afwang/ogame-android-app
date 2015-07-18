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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import com.wikaba.ogapp.ApplicationController;
import com.wikaba.ogapp.utils.AccountCredentials;

import java.util.ArrayList;

public class AccountsLoader extends AsyncTaskLoader<ArrayList<AccountCredentials>> {
    private ArrayList<AccountCredentials> oldData;

    public AccountsLoader(Context ctx) {
        super(ctx);
        oldData = null;
    }

    @Override
    protected void onStartLoading() {
        if (oldData != null) {
            deliverResult(oldData);
        }

        this.forceLoad();
    }

    @Override
    protected void onStopLoading() {
    }

    @Override
    protected void onReset() {
    }

    @Override
    public void deliverResult(ArrayList<AccountCredentials> newData) {
        oldData = newData;
        super.deliverResult(newData);
    }

    @Override
    public ArrayList<AccountCredentials> loadInBackground() {
        ArrayList<AccountCredentials> allAccs = ApplicationController
                .getInstance().getAccountsManager().getAccountsCredentialsWithoutPassword();
        return allAccs;
    }
}
