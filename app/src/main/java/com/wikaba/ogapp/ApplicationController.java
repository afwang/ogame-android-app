package com.wikaba.ogapp;

import android.app.Application;

import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.database.CookiesManager;
import com.wikaba.ogapp.database.DatabaseManager;

/**
 * Created by kevinleperf on 18/07/15.
 */
public class ApplicationController extends Application {
    private static ApplicationController _static_application_controller;

    public static ApplicationController getInstance() {
        return _static_application_controller;
    }

    private DatabaseManager _database_manager;
    private AccountsManager _accounts_manager;
    private CookiesManager _cookies_manager;


    @Override
    public void onCreate() {
        super.onCreate();

        _database_manager = DatabaseManager.getInstance();

        _database_manager.startSession(this);

        _accounts_manager = AccountsManager.getInstance(this);
        _cookies_manager = CookiesManager.getInstance(this);
    }

    public AccountsManager getAccountsManager(){
        return _accounts_manager;
    }

    public CookiesManager getCookiesManager(){
        return _cookies_manager;
    }



}
