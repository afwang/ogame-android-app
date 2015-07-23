package com.wikaba.ogapp.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by kevinleperf on 30/03/15.
 */
public abstract class FragmentStackManager {
    protected SystemFittableActivity _activity;
    protected FragmentManager _fragment_manager;
    protected int _index;
    protected int _landing_view;
    protected Fragment _current_fragment;
    protected String _current_fragment_tag;

    public FragmentStackManager(SystemFittableActivity parent, int landing_view) {
        _index = -1;
        _activity = parent;
        _landing_view = landing_view;
        _fragment_manager = _activity.getSupportFragmentManager();
    }

    protected Fragment findByTag(String tag, Class klass) {
        Fragment fragment = _fragment_manager.findFragmentByTag(tag);

        if (fragment == null) {
            try {
                fragment = (Fragment) klass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return fragment;
    }


    public void onResume() {
        if (_index < 0) {
            push(0, null);
        }
    }

    public boolean onBackPressed() {
        pop();

        return _index >= 0;
    }

    public abstract void pop();

    public boolean canBePopped() {
        return _index > 0;
    }

    public abstract void push(int new_index, Bundle arguments);

    protected void instantiateCurrent(boolean withInsets) {
        if (_activity != null) {
            if (withInsets) {
                _activity.setInsets();
            } else {
                _activity.unsetInsets();
            }
        }
        replace();
    }

    public void replace(){
        _fragment_manager.beginTransaction().replace(_landing_view, _current_fragment, _current_fragment_tag)
                .setCustomAnimations(0, 0)
                .commit();
    }

    protected void commitIndex(int new_index) {
        _index = new_index;
    }

    public abstract boolean isMainView();

    public abstract boolean navigationBackEnabled();

    public abstract boolean isNavigationDrawerEnabled();

    public int getCurrentPosition() {
        return _index;
    }
}
