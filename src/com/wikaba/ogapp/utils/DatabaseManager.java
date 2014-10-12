package com.wikaba.ogapp.utils;

import java.io.Closeable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseManager implements Closeable {
	public static final String LOG_TAG = "DatabaseManager";
	
	private static final int VERSION = 1;
	private static final String DB_NAME = "ogameapp.db";
	
	private Context context;
	private SQLiteDatabase database;
	
	public DatabaseManager(Context ctx) {
		this.context = ctx;
		database = null;
	}
	
	public long addAccount(String universe, String username, String passwd) {
		if(database == null) {
			open();
		}
		
		ContentValues cv = new ContentValues();
		
		//check if universe and username already entered into DB. Replace if true.
		String whereClause = AccountsContract.UNIVERSE + "=? and " + AccountsContract.USERNAME + "=?";
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
		long id = database.insert(AccountsContract.ACCOUNTS_TABLE, null, cv);
		return id;
	}
	
	public boolean removeAccount(String universe, String username) {
		if(database == null) {
			open();
		}
		String whereClause = AccountsContract.USERNAME + "=? and " + AccountsContract.UNIVERSE + "=?";
		int numDeleted = database.delete(AccountsContract.ACCOUNTS_TABLE, whereClause, new String[] {username, universe});
		return numDeleted > 0;
	}
	
	public AccountCredentials getAccount(String universe, String username) {
		if(database == null) {
			open();
		}
		
		String whereClause = AccountsContract.UNIVERSE + "=? and " + AccountsContract.USERNAME + "=?";
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
			return null;
		}
		
		//There should only be 1 row in the results.
		results.moveToFirst();
		AccountCredentials credentials = new AccountCredentials();
		credentials.universe = results.getString(results.getColumnIndex(AccountsContract.UNIVERSE));
		credentials.username = results.getString(results.getColumnIndex(AccountsContract.USERNAME));
		credentials.passwd = results.getString(results.getColumnIndex(AccountsContract.PASSWORD));
		return credentials;
	}
	
	private void open() {
		DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
		database = dbh.getWritableDatabase();
	}
	
	@Override
	public void close() {
		if(database != null)
			database.close();
	}

	private class DBHelper extends SQLiteOpenHelper {

		public static final String CREATE_ACCOUNTS_TABLE =
				"CREATE TABLE if not exists " + AccountsContract.ACCOUNTS_TABLE
				+ " (" + BaseColumns._ID + " integer PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT, "
				+ AccountsContract.UNIVERSE + " text, "
				+ AccountsContract.USERNAME + " text, "
				+ AccountsContract.PASSWORD + " text);";
//				+ AccountsContract.PHPSESSID_VALUE + " text DEFAULT \"\", "
//				+ AccountsContract.LOGIN_COOKIE_NAME + " text, "
//				+ AccountsContract.LOGIN_COOKIE_VALUE + " text, "
//				+ AccountsContract.PRSESS_COOKIE_NAME + " text, "
//				+ AccountsContract.PRSESS_COOKIE_VALUE + " text);";
		
		public DBHelper(Context ctx, String name, int version) {
			super(ctx, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_ACCOUNTS_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//do nothing.
		}
	}
}

class AccountsContract {
	static final String ACCOUNTS_TABLE = "accounts";
	static final String UNIVERSE = "universe";
	static final String USERNAME = "username";
	static final String PASSWORD = "password";
	
	//Cookie-related column names:
	static final String PHPSESSID_VALUE = "PHPSESSID";
	static final String LOGIN_COOKIE_NAME = "login_cookie_name";
	static final String LOGIN_COOKIE_VALUE = "login_cookie_value";
	static final String PRSESS_COOKIE_NAME = "prsess_cookie_name";
	static final String PRSESS_COOKIE_VALUE = "prsess_cookie_value";
}