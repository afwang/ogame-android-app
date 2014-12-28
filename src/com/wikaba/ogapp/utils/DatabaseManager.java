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
 * This class is (should be) thread-safe.
 */

import java.io.Closeable;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
		
		Cursor alreadyExists = database.query(
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
		
		Cursor results = database.query(
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
		
		Cursor results = database.query(
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
		return credentials;
	}
	
	/**
	 * Retrieve and return all account info stored in the database.
	 * This method does not return passwords.
	 * @return ArrayList of all account credentials with the password
	 * 		field set to the empty string. Returns null on error.
	 */
	public ArrayList<AccountCredentials> getAllAccounts() {
		ArrayList<AccountCredentials> allAccs = null;
		if(database == null) {
			open();
		}
		
		Cursor results = database.query(
				AccountsContract.ACCOUNTS_TABLE,
				new String[] {BaseColumns._ID, AccountsContract.UNIVERSE, AccountsContract.USERNAME},
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
				allAccs.add(cred);
			} while(results.moveToNext());
		}
		
		return allAccs;
	}
	
	/**
	 * Retrieve all cookies from the cookies table in the database.
	 * @return ArrayList of HttpCookie objects. 
	 */
	public ArrayList<HttpCookie> getCookies() {
		ArrayList<HttpCookie> cookies = null;
		return cookies;
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
				+ " (" + CookiesContract.NAME + " text PRIMARY KEY ON CONFLICT REPLACE, "
				+ CookiesContract.VALUE + " text NOT NULL ON CONFLICT IGNORE, "
				+ CookiesContract.EXPIRATION + " integer, "
				+ CookiesContract.DOMAIN + " text NOT NULL ON CONFLICT IGNORE, "
				+ CookiesContract.PATH + " text DEFAULT '/', "
				+ CookiesContract.SECURE + " integer DEFAULT 0, "
				+ CookiesContract.HTTP_ONLY + " integer DEFAULT 0) "
				+ "WITHOUT ROWID;";

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
}

class AccountsContract {
	static final String ACCOUNTS_TABLE = "accounts";
	static final String UNIVERSE = "universe";
	static final String USERNAME = "username";
	static final String PASSWORD = "password";
}

class CookiesContract {
	static final String COOKIES_TABLE = "cookies";
	static final String NAME = "name";
	static final String VALUE = "value";
	static final String EXPIRATION = "expires";
	static final String DOMAIN = "domain";
	static final String PATH = "path";
	static final String SECURE = "secure";
	static final String HTTP_ONLY = "http_only";
}