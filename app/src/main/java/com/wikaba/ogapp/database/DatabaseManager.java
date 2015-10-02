package com.wikaba.ogapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import com.wikaba.ogapp.ApplicationController;
import com.wikaba.ogapp.utils.AccountCredentials;
import de.greenrobot.dao.query.QueryBuilder;
import greendao.DaoMaster;
import greendao.DaoSession;

/**
 * This class is not thread-safe. Concurrent access to the database
 * should be done with multiple DatabaseManager objects. This sort of behavior
 * is not pleasant, so this class may change in the future.
 *
 * Update by kevinleperf on 04/07/15.
 * Note : to make it ThreadSafe, write access mut use the lock() and unlock() method in the subsequent
 * controllers
 */
public class DatabaseManager {
	private static final String DATABASE_NAME = "ogame_orm";
	private static DaoSession sDaoSession;

	private static DatabaseManager ourInstance = new DatabaseManager();

	public static DatabaseManager getInstance() {
		return ourInstance;
	}

	private DatabaseManager() {

	}

	public void startSession(final Context context) {
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		sDaoSession = daoMaster.newSession();
		QueryBuilder.LOG_SQL = false;
		QueryBuilder.LOG_VALUES = false;
	}

	protected DaoSession getSession() {
		return sDaoSession;
	}



	/**
	 * Insert a new account to the database.
	 * @param universe
	 * @param username
	 * @param passwd
	 * @return ID of the row inserted or modified.
	 */
	public long addAccount(String universe, String username, String passwd, String lang) {
		return AccountsManager.getInstance().addAccount(universe, username, passwd, lang);
	}

	/**
	 * Remove the specified account from the database.
	 * on the main thread.
	 * @param universe
	 * @param username
	 * @return true if an account was removed from the database.
	 */
	public boolean removeAccount(String universe, String username) {
		return AccountsManager.getInstance().removeAccount(universe, username);
	}

	/**
	 * Get the AccountCredentials of an account.
	 * @param universe - the universe of the account
	 * @param username - the username of the account
	 * @return AccountCredentials containing universe, username, and password of the account.
	 */
	public AccountCredentials getAccount(String universe, String username) {
		return AccountsManager.getInstance().getAccountCredentials(universe, username);
	}

	/**
	 * This thread only reads from the database. It can be called from any thread.
	 * @param rowId - the ID of the row in the database to retrieve
	 * @return AccountCredentials object containing the universe, username, and password of the account
	 * 		associated with rowId. Return null if account does not exist.
	 */
	public AccountCredentials getAccount(long rowId) {
		return AccountsManager.getInstance().getAccountCredentials(rowId);
	}

	/**
	 * <p>Retrieve and return all account info stored in the database.
	 * This method does not return passwords.</p>
	 *
	 * @return ArrayList of all account credentials.
	 */
	public ArrayList<AccountCredentials> getAllAccounts() {
		return AccountsManager.getInstance().getAllAccountCredentials();
	}

	/**
	 * Retrieve all cookies from the cookies table in the database.
	 * @return ArrayList of HttpCookie objects. This list is guaranteed to have
	 * 		cookies with a non-null, non-empty domain and path.
	 */
	public ArrayList<HttpCookie> getCookies() {
		ArrayList<HttpCookie> cookies = CookiesManager.getInstance().getAllHttpCookies();
		return cookies;
	}

	/**
	 * Drop the entire cookies table and then save parameter cookie list into the
	 * empty table.
	 * @param cookies
	 */
	public void saveCookies(List<HttpCookie> cookies) {
		CookiesManager.getInstance().saveCookies(cookies);
	}
}
