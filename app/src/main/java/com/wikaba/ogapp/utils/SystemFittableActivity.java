package com.wikaba.ogapp.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wikaba.ogapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by kevinleperf on 31/03/15.
 */
public abstract class SystemFittableActivity extends ActionBarActivity {
    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    @Bind(R.id.toolbar)
    public Toolbar _toolbar;
    protected FragmentStackManager _stack_manager;
    protected ActionBarDrawerToggle _actionbar_toggle;
    @Bind(R.id.parent)
    View _parent;
    /*
    @Bind(R.id.container)
    FrameLayout _container;
    */
    private int mStatusBarHeight;
    private SystemBarTintManager _tint_manager;
    private int _primary_color_dark;
    private int _primary_color;
    private boolean _not_set;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _not_set = true;

        //checkLollipopFullTransparency();
        super.onCreate(savedInstanceState);

        setContentView(getContentView());

        ButterKnife.bind(this);

        if (_toolbar != null) {
            setSupportActionBar(_toolbar);
        }

        _primary_color_dark = getResources().getColor(R.color.colorPrimaryDark);
        _primary_color = getResources().getColor(R.color.colorPrimary);

        _tint_manager = getSystemBarTintManager();
        _tint_manager.setStatusBarTintEnabled(true);
        _tint_manager.setNavigationBarTintEnabled(true);


        _stack_manager = getFragmentStackManager();

        _tint_manager.setTintColor(0x00000000);
        setSystemColor(0x0, 0);

        if (findViewById(R.id.container) != null && Build.VERSION.SDK_INT >= 14) {
            findViewById(R.id.container).setFitsSystemWindows(false);
        }
    }

    public abstract int getContentView();

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        if (_not_set) {
            _not_set = false;
            super.setSupportActionBar(toolbar);
        }
    }

    protected SystemBarTintManager getSystemBarTintManager() {
        if (_tint_manager == null) ;
        _tint_manager = new SystemBarTintManager(this);
        return _tint_manager;
    }

    protected abstract FragmentStackManager getFragmentStackManager();

    private void setSystemColor(int systembar_color, int navigation_color) {
        _tint_manager.setStatusBarTintColor(systembar_color);
        _tint_manager.setNavigationBarTintColor(navigation_color);
    }

    public void setTintColor() {

    }

    public void setPaddingTop(int dimen) {
        //map.setPadding(0, config.getPixelInsetTop(), config.getPixelInsetRight(), config.getPixelInsetBottom());
    }

    @Override
    public void onResume() {
        super.onResume();
        _stack_manager.onResume();
        invalidateActionBar(getFragmentStackManager().getCurrentPosition());
    }

    @Override
    public void onBackPressed() {
        if (!_stack_manager.onBackPressed()) {
            super.onBackPressed();
        }

        invalidateActionBar(getFragmentStackManager().getCurrentPosition());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void push(int fragment_type, Bundle arguments) {
        _stack_manager.push(fragment_type, arguments);
        invalidateActionBar(fragment_type);
    }

    public void invalidateActionBar(int fragment_type) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setHomeButtonEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            final boolean isMainView = getFragmentStackManager().isMainView();


            if (getFragmentStackManager().navigationBackEnabled() && _actionbar_toggle != null) {
                _actionbar_toggle.setDrawerIndicatorEnabled(isMainView);
                _actionbar_toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
            bar.setDisplayHomeAsUpEnabled(isMainView);
        }
    }

    public void checkLollipopFullTransparency() {
        //TODO UPDATE SystemBarTintManager to support the statusBarColor mode only for 5.0+
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        mStatusBarHeight = 0;
    }

    private int getmStatusBarHeight() {
        if (mStatusBarHeight <= 0)
            mStatusBarHeight = getInternalDimensionSize(getResources(), STATUS_BAR_HEIGHT_RES_NAME);
        return mStatusBarHeight;
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void unsetInsets() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        _parent.setPadding(0, 0, 0, 0);
        _toolbar.setVisibility(View.GONE);
        _toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        setSystemColor(Color.TRANSPARENT, Color.TRANSPARENT);
    }

    protected int getPaddingInsetTop(boolean with_actionbar) {
        return getPaddingInsetTop(with_actionbar, getSystemBarTintManager().getConfig());
    }

    private int getPaddingInsetTop(boolean with_actionbar, SystemBarTintManager.SystemBarConfig config) {
        if (Build.VERSION.SDK_INT >= 21) {
            return getmStatusBarHeight();
        }
        return config.getPixelInsetTop(with_actionbar);
    }

    public void setInsets() {
        setSystemColor(_primary_color_dark, Color.BLACK);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        SystemBarTintManager.SystemBarConfig config = _tint_manager.getConfig();
        _parent.setPadding(0, getPaddingInsetTop(false, config),
                config.getPixelInsetRight(), config.getPixelInsetBottom());
        _toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        _toolbar.setVisibility(View.VISIBLE);
    }

    public Toolbar getToolbar() {
        if (_toolbar == null)
            return (Toolbar) findViewById(R.id.toolbar);
        return _toolbar;
    }
}
