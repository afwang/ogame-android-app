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
}
