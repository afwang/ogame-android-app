/*
	Copyright 2015 Kevin Le Perf

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
package com.wikaba.ogapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.wikaba.ogapp.AgentService;
import com.wikaba.ogapp.ApplicationController;
import com.wikaba.ogapp.R;
import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.events.OnLoggedEvent;
import com.wikaba.ogapp.events.OnLoginEvent;
import com.wikaba.ogapp.events.OnLoginRequested;
import com.wikaba.ogapp.utils.AccountCredentials;
import com.wikaba.ogapp.utils.FragmentStackManager;
import com.wikaba.ogapp.utils.SystemFittableActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by kevinleperf on 03/07/15.
 */
public class NoAccountActivity extends SystemFittableActivity {


    private static final int ALL_ACCS_LOADER_ID = 0;

    private MaterialDialog _login_progress;

    @Bind(R.id.uniSelect)
    protected Spinner uniSpinner;

    @Bind(R.id.username)
    protected EditText usernameField;

    @Bind(R.id.password)
    protected EditText passwdField;

    @Bind(R.id.lang)
    protected EditText langField;

    @Bind(R.id.login)
    protected ButtonRectangle loginButton;

    @Bind(R.id.pw_checkbox)
    protected CheckBox pwCheckBox;

    @Bind(R.id.existingAccList)
    protected Spinner existingAccs;


    private ArrayList<AccountCredentials> existingAccountsCredentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unsetInsets();
        ButterKnife.bind(this);

        String[] uniNames = getResources().getStringArray(R.array.universe_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, uniNames);
        uniSpinner.setAdapter(adapter);

        String account_spinner = getString(R.string.account_spinner);
        existingAccountsCredentials = AccountsManager.getInstance(this).getAllAccountCredentials();
        String[] accountNames = new String[existingAccountsCredentials.size() + 1];
        accountNames[0] = getString(R.string.select_an_account);
        int i = 1;
        for (AccountCredentials cred : existingAccountsCredentials) {
            accountNames[i] = String.format(account_spinner, cred.username, cred.universe);
            i++;
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountNames);
        existingAccs.setAdapter(adapter);
        existingAccs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                position -= 1;

                if (position >= 0) {
                    AccountCredentials cred = existingAccountsCredentials.get(position);
                    //TODO ?
                    EventBus.getDefault().postSticky(new OnLoginRequested(cred));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public int getContentView() {
        return R.layout.activity_no_account;
    }

    @Override
    protected FragmentStackManager getFragmentStackManager() {
        return new FragmentStackManager(this, 0) {
            @Override
            public void pop() {

            }

            @Override
            public void push(int new_index, Bundle arguments) {

            }

            @Override
            public boolean isMainView() {
                return true;
            }

            @Override
            public boolean navigationBackEnabled() {
                return false;
            }

            @Override
            public boolean isNavigationDrawerEnabled() {
                return false;
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        Intent service = new Intent(this, AgentService.class);
        startService(service);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        dismissLogin();
        super.onPause();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.remove) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int rowPosition = info.position;
            AccountCredentials creds = existingAccountsCredentials.get(rowPosition);

            AccountsManager dbmanager = ApplicationController.getInstance().getAccountsManager();
            dbmanager.removeAccount(creds.universe, creds.username);
            existingAccountsCredentials.remove(rowPosition);

            //TODO NOTIFY SPINNER CHANGE
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @OnClick(R.id.login)
    public void onLoginClicked() {
        String username = usernameField.getText().toString().trim();
        String passwd = passwdField.getText().toString().trim();
        String lang = langField.getText().toString().trim();
        if (lang == null || lang.length() == 0) lang = "en";
        View selectedView = uniSpinner.getSelectedView();
        if (selectedView == null || username.length() == 0 || passwd.length() == 0) {
            //TODO SHOW SNACKBAR
            return;
        }

        TextView selectedText = (TextView) selectedView;
        String universe = selectedText.getText().toString();

        AccountCredentials acc = new AccountCredentials();
        acc.universe = universe;
        acc.username = username;
        acc.passwd = passwd;
        acc.lang = lang;
        addAccount(acc);
    }

    @OnClick(R.id.pw_checkbox)
    public void onPasswordCheckBoxClicked() {
        int inputType = (pwCheckBox.isChecked()) ?
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwdField.setInputType(inputType);
    }


    public void addAccount(AccountCredentials creds) {
        AccountsManager manager = ApplicationController.getInstance().getAccountsManager();
        long accountRowId = manager.addAccount(creds.universe, creds.username, creds.passwd, creds.lang);

        if (accountRowId < 0) {
            return;
        }

        AccountCredentials activeAccount = new AccountCredentials(creds);
        activeAccount.id = accountRowId;

        //TODO OPEN LOADER
        EventBus.getDefault().post(new OnLoginRequested(activeAccount));
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void onProgressLogin(OnLoginEvent event) {
        if (event.isPendingLogin()) {
            if (_login_progress == null || !_login_progress.isShowing()) {
                _login_progress = new MaterialDialog.Builder(this)
                        .title(R.string.login)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .cancelable(false)
                        .show();
            }
        } else {
            dismissLogin();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void onLoggedEvent(OnLoggedEvent event) {
        if (event.isConnected()) {
            startOverviewActivity();
        } else {
            //possible race condition with http service returning false so dismiss any dialog possible
            //which could prevent app usability during onCOnfigurationChanged
            dismissLogin();
        }
    }

    private void dismissLogin() {
        if (_login_progress != null && _login_progress.isShowing()) {
            _login_progress.dismiss();
        }
        _login_progress = null;
    }

    private void startOverviewActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}
