package com.wikaba.ogapp.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wikaba.ogapp.ui.listings.ListingFragmentWithEvent;
import com.wikaba.ogapp.ui.overview.OverviewFragment;
import com.wikaba.ogapp.utils.Constants;
import com.wikaba.ogapp.utils.FragmentStackManager;
import com.wikaba.ogapp.utils.SystemFittableActivity;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class HomeFragmentStackManager extends FragmentStackManager {
    public HomeFragmentStackManager(SystemFittableActivity parent, int landing_view) {
        super(parent, landing_view);
    }

    private Fragment createInternalFragment() {
        return new ListingFragmentWithEvent();
    }

    @Override
    public void pop() {
        if (getCurrentPosition() != Constants.OVERVIEW_INDEX) {
            ((HomeActivity) _activity).goToOverview();
        }
    }

    @Override
    public void push(int new_index, Bundle arguments) {
        switch (new_index) {
            case Constants.RESOURCES_INDEX:
            case Constants.BUILDING_INDEX:
            case Constants.RESEARCH_INDEX:
            case Constants.SHIPYARD_INDEX:
            case Constants.DEFENSE_INDEX:
                _current_fragment = createInternalFragment();
                break;
            default:
                _current_fragment = new OverviewFragment();
        }
        _current_fragment.setArguments(arguments);

        commitIndex(new_index);
    }

    @Override
    public boolean isMainView() {
        return false;
    }

    @Override
    public boolean navigationBackEnabled() {
        return getCurrentPosition() != Constants.OVERVIEW_INDEX;
    }

    @Override
    public boolean isNavigationDrawerEnabled() {
        return true;
    }
}
