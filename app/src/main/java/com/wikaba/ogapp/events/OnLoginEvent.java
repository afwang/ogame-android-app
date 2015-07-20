package com.wikaba.ogapp.events;

/**
 * Created by kevinleperf on 20/07/15.
 */
public class OnLoginEvent {
    private boolean _pending_login;

    public OnLoginEvent() {
        _pending_login = false;
    }

    public OnLoginEvent(boolean pending_login) {
        _pending_login = pending_login;
    }

    public boolean isPendingLogin() {
        return _pending_login;
    }

    @Override
    public String toString() {
        return "pending_login :: " + _pending_login;
    }
}
