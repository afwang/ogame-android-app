package com.wikaba.ogapp.utils;

import java.util.HashMap;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class AccountAuthLoader extends AsyncTaskLoader<HashMap<String, String>> {

	public AccountAuthLoader(Context context, String username, String passwd, int serverNum) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		//nothing special needs to be done here. Just call forceLoad()
		forceLoad();
	}

	@Override
	public HashMap<String, String> loadInBackground() {
		//TODO: Check in database if account credentials has a row. Check if this row
		//has the cookies for the specified server.
		//If yes, try logging in to the specified server with the given cookies.
		//If no, log in to the server to authenticate.
		return null;
	}

}
