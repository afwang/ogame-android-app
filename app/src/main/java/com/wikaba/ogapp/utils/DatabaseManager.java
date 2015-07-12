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

/**
 * This class is not thread-safe. Concurrent access to the database
 * should be done with multiple DatabaseManager objects. This sort of behavior
 * is not pleasant, so this class may change in the future.
 */

import java.io.Closeable;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseManager implements Closeable {
	public static final String LOG_TAG = "DatabaseManager";
	
	private static final int VERSION = 2;
	private static final String DB_NAME = "ogameapp.db";
	
	private Context context;
	private volatile SQLiteDatabase database;
	
	public DatabaseManager(Context ctx) {
		this.context = ctx;
		database = null;
	}
	
	/**
	 * Insert a new account to the database.
	 * @param universe
	 * @param username
	 * @param passwd
	 * @return ID of the row inserted or modified.
	 */
	public long addAccount(String universe, String username, String passwd) {
		
		ContentValues cv = new ContentValues();
		
		//check if universe and username already entered into DB. Replace if true.
		long id = -1;
		String whereClause = AccountsContract.UNIVERSE + "=? and " + AccountsContract.USERNAME + "=?";
		if(database == null) {
			open();
		}

		Cursor alreadyExists = null;
		try {
			alreadyExists = database.query(
					AccountsContract.ACCOUNTS_TABLE,
					null,
					whereClause,
					new String[] {universe, username},
					null,
					null,
					null
			);
			if(alreadyExists.getCount() > 0) {
				//There exists a row entry containing the same universe and username.
				//Replace.
				alreadyExists.moveToFirst();
				long rowid = alreadyExists.getLong(alreadyExists.getColumnIndex(BaseColumns._ID));
				cv.put(BaseColumns._ID, rowid);
			}
		}
		finally {
			if(alreadyExists != null) {
				alreadyExists.close();
			}
		}
		
		cv.put(AccountsContract.UNIVERSE, universe);
		cv.put(AccountsContract.USERNAME, username);
		cv.put(AccountsContract.PASSWORD, passwd);
		id = database.insert(AccountsContract.ACCOUNTS_TABLE, null, cv);
		return id;
	}
	
	/**
	 * Remove the specified account from the database.
	 * on the main thread.
	 * @param universe
	 * @param username
	 * @return true if an account was removed from the database.
	 */
	public boolean removeAccount(String universe, String username) {

		String whereClause = AccountsContract.USERNAME + "=? and " + AccountsContract.UNIVERSE + "=?";
		int numDeleted = -1;
		
		if(database == null) {
			open();
		}
		numDeleted = database.delete(AccountsContract.ACCOUNTS_TABLE, whereClause, new String[] {username, universe});
		return numDeleted > 0;
	}
	
	/**
	 * Get the AccountCredentials of an account.
	 * @param universe - the universe of the account
	 * @param username - the username of the account
	 * @return AccountCredentials containing universe, username, and password of the account.
	 */
	public AccountCredentials getAccount(String universe, String username) {
		
		String whereClause = AccountsContract.UNIVERSE + "=? and " + AccountsContract.USERNAME + "=?";
		AccountCredentials credentials = null;
		if(database == null) {
			open();
		}

		Cursor results = null;
		try {
			results = database.query(
					AccountsContract.ACCOUNTS_TABLE,
					null,
					whereClause,
					new String[] {universe, username},
					null,
					null,
					null
			);

			if(results == null || results.getCount() != 1) {
				Log.e(LOG_TAG, "The number of results returned from database query is not 1!");
			}
			else {
				//There should only be 1 row in the results.
				results.moveToFirst();
				credentials = new AccountCredentials();
				credentials.universe = results.getString(results.getColumnIndex(AccountsContract.UNIVERSE));
				credentials.username = results.getString(results.getColumnIndex(AccountsContract.USERNAME));
				credentials.passwd = results.getString(results.getColumnIndex(AccountsContract.PASSWORD));
			}
		}
		finally {
			if(results != null) {
				results.close();
			}
		}

		return credentials;
	}
	
	/**
	 * This thread only reads from the database. It can be called from any thread.
	 * @param rowId - the ID of the row in the database to retrieve
	 * @return AccountCredentials object containing the universe, username, and password of the account
	 * 		associated with rowId. Return null if account does not exist.
	 */
	public AccountCredentials getAccount(long rowId) {
		String whereClause = BaseColumns._ID + "=?";
		AccountCredentials credentials = null;
		if(database == null) {
			open();
		}

		Cursor results = null;
		try {
			results = database.query(
					AccountsContract.ACCOUNTS_TABLE,
					null,
					whereClause, new String[]{Long.toString(rowId)},
					null,
					null,
					null
			);
			if(results == null || results.getCount() != 1) {
				if(results == null) {
					Log.e(LOG_TAG, "Results cursor from database query is null.");
				}
				else {
					Log.e(LOG_TAG, "The number of results returned from database query is " + results.getCount() + '!');
				}
			}
			else {
				//There should only be 1 row in the results.
				results.moveToFirst();
				credentials = new AccountCredentials();
				credentials.universe = results.getString(results.getColumnIndex(AccountsContract.UNIVERSE));
				credentials.username = results.getString(results.getColumnIndex(AccountsContract.USERNAME));
				credentials.passwd = results.getString(results.getColumnIndex(AccountsContract.PASSWORD));
			}
		}
		finally {
			if(results != null) {
				results.close();
			}
		}

		return credentials;
	}
	
	/**
	 * <p>Retrieve and return all account info stored in the database.
	 * This method does not return passwords.</p>
	 *
	 * @return ArrayList of all account credentials.
	 */
	public ArrayList<AccountCredentials> getAllAccounts() {
		ArrayList<AccountCredentials> allAccs = null;
		if(database == null) {
			open();
		}

		Cursor results = null;
		try {
			results = database.query(
					AccountsContract.ACCOUNTS_TABLE,
					new String[] {
						BaseColumns._ID,
						AccountsContract.UNIVERSE,
						AccountsContract.USERNAME,
						AccountsContract.PASSWORD},
					null,
					null,
					null,
					null,
					null
			);
			if(results == null || results.getCount() <= 0) {
				Log.e(LOG_TAG, "The number of results returned from database query is not 1!");
				allAccs = new ArrayList<AccountCredentials>();
			}
			else {
				results.moveToFirst();
				allAccs = new ArrayList<AccountCredentials>(results.getCount());
				do {
					AccountCredentials cred = new AccountCredentials();
					cred.id = results.getLong(results.getColumnIndex(BaseColumns._ID));
					cred.universe = results.getString(results.getColumnIndex(AccountsContract.UNIVERSE));
					cred.username = results.getString(results.getColumnIndex(AccountsContract.USERNAME));
					cred.passwd = results.getString(results.getColumnIndex(AccountsContract.PASSWORD));
					allAccs.add(cred);
				} while(results.moveToNext());
			}
		}
		finally {
			if(results != null) {
				results.close();
			}
		}

		return allAccs;
	}
	
	/**
	 * Retrieve all cookies from the cookies table in the database.
	 * @return ArrayList of HttpCookie objects. This list is guaranteed to have
	 * 		cookies with a non-null, non-empty domain and path. 
	 */
	public ArrayList<HttpCookie> getCookies() {
		ArrayList<HttpCookie> cookies = null;
		if(database == null) {
			DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
			database = dbh.getWritableDatabase();
		}

		Cursor all = null;
		try {
			all = database.query(
					CookiesContract.COOKIES_TABLE,
					null,
					null,
					null,
					null,
					null,
					null
			);

			int nameIndex = all.getColumnIndex(CookiesContract.NAME);
			int valueIndex = all.getColumnIndex(CookiesContract.VALUE);
			int expireIndex = all.getColumnIndex(CookiesContract.EXPIRATION);
			int domainIndex = all.getColumnIndex(CookiesContract.DOMAIN);
			int pathIndex = all.getColumnIndex(CookiesContract.PATH);
			int secureIndex = all.getColumnIndex(CookiesContract.SECURE);

			cookies = new ArrayList<HttpCookie>(all.getCount());

			boolean cursorHasResults = all.moveToFirst();
			HttpCookie cookie;
			if(cursorHasResults) {
				do {
					String name = all.getString(nameIndex);
					String value = all.getString(valueIndex);

					long expiration = 0;
					try {
						expiration = all.getLong(expireIndex);
					}
					catch(Exception e) {
					}

					String domain = all.getString(domainIndex);
					String path = all.getString(pathIndex);

					int secureFlag = 0;
					try {
						secureFlag = all.getInt(secureIndex);
					}
					catch(Exception e) {
					}

					//Currently no use or need to use the HTTP-only flag in cookies for now
					cookie = new HttpCookie(name, value);
					cookie.setVersion(0);

					long currentTimeInSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
					if(currentTimeInSeconds < expiration) {
						long maxAge = expiration - currentTimeInSeconds;
						cookie.setMaxAge(maxAge);
					}

					if(secureFlag != 0) {
						cookie.setSecure(true);
					}

					if(domain != null && domain.length() > 0) {
						cookie.setDomain(domain);
						if(path != null && path.length() > 0) {
							cookie.setPath(path);
						}
						else {
							cookie.setPath("/");
						}
						cookies.add(cookie);
//						System.out.println("Load cookie: " + name + '=' + value + "; expiration=" + expiration + "; domain=" + domain + "; path=" + path);
					}
				} while(all.moveToNext());
			}
		}
		finally {
			if(all != null) {
				all.close();
			}
		}
		return cookies;
	}
	
	/**
	 * Drop the entire cookies table and then save parameter cookie list into the
	 * empty table.
	 * @param cookies
	 */
	public void saveCookies(List<HttpCookie> cookies) {
		if(cookies == null) {
			return;
		}

		if(database == null) {
			DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
			database = dbh.getWritableDatabase();
		}

		database.delete(CookiesContract.COOKIES_TABLE, null, null);
		database.beginTransaction();
		try {
			Iterator<HttpCookie> cookieIter = cookies.iterator();
			while(cookieIter.hasNext()) {
				HttpCookie cookie = cookieIter.next();
				String name = cookie.getName();
				String value = cookie.getValue();
				long maxAgeDelta = cookie.getMaxAge();
				long expiration = Calendar.getInstance().getTimeInMillis() / 1000;
				expiration += maxAgeDelta;
				String domain = cookie.getDomain();
				String path = cookie.getPath();
				boolean secureFlag = cookie.getSecure();

				if(domain != null && domain.length() > 0) {
					if(path != null && path.length() == 0) {
						path = "/";
					}
					ContentValues cv = new ContentValues();
					cv.put(CookiesContract.NAME, name);
					cv.put(CookiesContract.VALUE, value);
					cv.put(CookiesContract.EXPIRATION, Long.valueOf(expiration));
					cv.put(CookiesContract.DOMAIN, domain);
					cv.put(CookiesContract.PATH, path);
					int secureInt = (secureFlag ? 1 : 0);
					cv.put(CookiesContract.SECURE, Integer.valueOf(secureInt));
					//No support for HTTP-only flag yet
					int httpInt = 0;
					cv.put(CookiesContract.HTTP_ONLY, Integer.valueOf(httpInt));
					database.insert(CookiesContract.COOKIES_TABLE, null, cv);
//					System.out.println("Save Cookie: " + name + '=' + value + "; expiration=" + expiration + "; domain=" + domain + "; path=" + path);
				}
			}
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}
	}

	private synchronized void open() {
		if(database == null) {
			DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
			database = dbh.getWritableDatabase();
		}
	}
	
	@Override
	public void close() {
		if(database != null) {
			database.close();
			database = null;
		}
	}

	private class DBHelper extends SQLiteOpenHelper {

		private static final String CREATE_ACCOUNTS_TABLE =
				"CREATE TABLE if not exists " + AccountsContract.ACCOUNTS_TABLE
				+ " (" + BaseColumns._ID + " integer PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT, "
				+ AccountsContract.UNIVERSE + " text, "
				+ AccountsContract.USERNAME + " text, "
				+ AccountsContract.PASSWORD + " text);";
		
		private static final String CREATE_COOKIES_TABLE =
				"CREATE TABLE if not exists " + CookiesContract.COOKIES_TABLE
				+ " (" + BaseColumns._ID + " integer PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT, "
				+ CookiesContract.NAME + " text NOT NULL ON CONFLICT IGNORE, "
				+ CookiesContract.VALUE + " text NOT NULL ON CONFLICT IGNORE, "
				+ CookiesContract.EXPIRATION + " integer DEFAULT 0, "
				+ CookiesContract.DOMAIN + " text NOT NULL ON CONFLICT IGNORE, "
				+ CookiesContract.PATH + " text DEFAULT '/', "
				+ CookiesContract.SECURE + " integer DEFAULT 0, "
				+ CookiesContract.HTTP_ONLY + " integer DEFAULT 0);";

		public DBHelper(Context ctx, String name, int version) {
			super(ctx, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_ACCOUNTS_TABLE);
			db.execSQL(CREATE_COOKIES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//From version 1 -> 2. Create a new cookies table.
			final int includeCookies = 2;
			if(oldVersion < includeCookies) {
				db.execSQL(CREATE_COOKIES_TABLE);
			}
		}
	}

	public static class AccountsContract {
		static final String ACCOUNTS_TABLE = "accounts";
		static final String UNIVERSE = "universe";
		static final String USERNAME = "username";
		static final String PASSWORD = "password";
	}

	public static class CookiesContract {
		static final String COOKIES_TABLE = "cookies";
		static final String NAME = "name";
		static final String VALUE = "value";
		static final String EXPIRATION = "expires";
		static final String DOMAIN = "domain";
		static final String PATH = "path";
		static final String SECURE = "secure";
		static final String HTTP_ONLY = "http_only";
	}
}


