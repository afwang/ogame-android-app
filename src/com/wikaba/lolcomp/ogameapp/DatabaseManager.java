package com.wikaba.lolcomp.ogameapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseManager {
	
	private static final int VERSION = 1;
	private static final String DB_NAME = "ogameapp.db";
	
	private Context context;
	private SQLiteDatabase database;
	
	public DatabaseManager(Context ctx) {
		this.context = ctx;
		database = null;
	}
	
	public boolean addAccount(String username, String passwd) {
		if(database == null) {
			DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
			database = dbh.getWritableDatabase();
		}
		//check if username and password already entered into DB
		String userCol = AccountsContract.USERNAME;
		Cursor alreadyExists = database.query(AccountsContract.ACCOUNTS_TABLE, new String[] {userCol}, userCol + "=?", new String[] {username}, null, null, null);
		if(alreadyExists.getCount() > 0) {
			return false;
		}
		
		ContentValues cv = new ContentValues();
		cv.put(AccountsContract.USERNAME, username);
		cv.put(AccountsContract.PASSWORD, passwd);
		database.insert(AccountsContract.ACCOUNTS_TABLE, null, cv);
		database.close();
		database = null;
		return true;
	}
	
	public boolean removeAccount(String username, String universe) {
		if(database == null) {
			DBHelper dbh = new DBHelper(context, DB_NAME, VERSION);
			database = dbh.getWritableDatabase();
		}
		String whereClause = AccountsContract.USERNAME + "=? and " + AccountsContract.UNIVERSE + "=?";
		database.delete(AccountsContract.ACCOUNTS_TABLE, whereClause, new String[] {username, universe});
		return true;
	}

	private class DBHelper extends SQLiteOpenHelper {

		public static final String CREATE_ACCOUNTS_TABLE =
				"CREATE TABLE if not exists " + AccountsContract.ACCOUNTS_TABLE
				+ " (" + BaseColumns._ID + " integer PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT, "
				+ AccountsContract.UNIVERSE + " text, "
				+ AccountsContract.USERNAME + " text, "
				+ AccountsContract.PASSWORD + " text);";
		
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
}