/*
	Copyright 2014 Alexander Wang

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

package com.wikaba.ogapp.ui.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wikaba.ogapp.ApplicationController;
import com.wikaba.ogapp.R;
import com.wikaba.ogapp.database.AccountsManager;
import com.wikaba.ogapp.events.OnLoginRequested;
import com.wikaba.ogapp.loaders.AccountsLoader;
import com.wikaba.ogapp.ui.main.HomeActivity;
import com.wikaba.ogapp.utils.AccountCredentials;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

@Deprecated
public class NoAccountFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<AccountCredentials>>,
        AdapterView.OnItemClickListener {

    private static final int ALL_ACCS_LOADER_ID = 0;

    @Bind(R.id.uniSelect)
    protected Spinner uniSpinner;

    @Bind(R.id.username)
    protected EditText usernameField;

    @Bind(R.id.password)
    protected EditText passwdField;

    @Bind(R.id.lang)
    protected EditText langField;

    @Bind(R.id.login)
    protected Button loginButton;

    @Bind(R.id.pw_checkbox)
    protected CheckBox pwCheckBox;

    @Bind(R.id.existingAccList)
    protected ListView existingAccs;

    private HomeActivity act;
    private ArrayList<AccountCredentials> allAccounts;

    public NoAccountFragment() {
    }

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        this.act = (HomeActivity) act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_no_acc, parent, false);

        ButterKnife.bind(this, root);

        String[] uniNames = getResources().getStringArray(R.array.universe_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, android.R.layout.simple_list_item_1, uniNames);
        uniSpinner.setAdapter(adapter);

        getLoaderManager().initLoader(ALL_ACCS_LOADER_ID, null, this);

        registerForContextMenu(existingAccs);

        return root;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = act.getMenuInflater();
        inflater.inflate(R.menu.accounts, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.remove) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int rowPosition = info.position;
            AccountCredentials creds = allAccounts.get(rowPosition);

            AccountsManager dbmanager = ApplicationController.getInstance().getAccountsManager();
            dbmanager.removeAccount(creds.universe, creds.username);
            allAccounts.remove(rowPosition);
            AccountAdapter adapter = (AccountAdapter) existingAccs.getAdapter();
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @OnClick(R.id.login)
    public void onLoginClicked() {
        String username = usernameField.getText().toString();
        String passwd = passwdField.getText().toString();
        String lang = langField.getText().toString();
        if (lang == null || lang.length() == 0) lang = "en";
        View selectedView = uniSpinner.getSelectedView();
        if (selectedView == null) {
            Toast.makeText(act, "Please select a valid universe.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView selectedText = (TextView) selectedView;
        String universe = selectedText.getText().toString();

        AccountCredentials acc = new AccountCredentials();
        acc.universe = universe;
        acc.username = username;
        acc.passwd = passwd;
        acc.lang = lang;
        //act.addAccount(acc);
    }

    @OnClick(R.id.pw_checkbox)
    public void onPasswordCheckBoxClicked() {
        int inputType = (pwCheckBox.isChecked()) ?
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwdField.setInputType(inputType);
    }

    @Deprecated
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        AccountCredentials cred = allAccounts.get(position);
        act.setActiveAccount(cred);
        //act.goToLogin();
        EventBus.getDefault().post(new OnLoginRequested(cred));
    }

    @Override
    public Loader<ArrayList<AccountCredentials>> onCreateLoader(int id, Bundle args) {
        return new AccountsLoader(act);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AccountCredentials>> loader, ArrayList<AccountCredentials> data) {
        AccountAdapter adapter = new AccountAdapter(act, data);
        existingAccs.setAdapter(adapter);
        existingAccs.setOnItemClickListener(this);
        allAccounts = data;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AccountCredentials>> loader) {
    }

    public static class AccountAdapter extends BaseAdapter {
        private ArrayList<AccountCredentials> accs;
        private Context ctx;

        public AccountAdapter(Context cont, ArrayList<AccountCredentials> accs) {
            this.accs = accs;
            ctx = cont;
        }

        @Override
        public int getCount() {
            return accs.size();
        }

        @Override
        public Object getItem(int position) {
            return accs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = (TextView) inflater.inflate(R.layout.account_text_view, parent, false);

            } else {
                v = (TextView) convertView;
            }

            StringBuilder strb = new StringBuilder();
            AccountCredentials creds = accs.get(position);
            strb.append(creds.username)
                    .append(" in ")
                    .append(creds.universe);

            v.setText(strb.toString());

            return v;
        }
    }
}
