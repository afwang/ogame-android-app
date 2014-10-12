package com.wikaba.ogapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class NoAccountFragment extends Fragment implements OnClickListener {
	
	Spinner uniSpinner;
	EditText usernameField;
	EditText passwdField;
	Button loginButton;
	HomeActivity act;
	
	public NoAccountFragment() {
	}
	
	@Override
	public void onAttach(Activity act) {
		this.act = (HomeActivity)act;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_no_acc, parent, false);
		uniSpinner = (Spinner)root.findViewById(R.id.uniSelect);
		usernameField = (EditText)root.findViewById(R.id.username);
		passwdField = (EditText)root.findViewById(R.id.password);
		loginButton = (Button)root.findViewById(R.id.login);
		
		loginButton.setOnClickListener(this);
		return root;
	}
	
	@Override
	public void onClick(View v) {
		String username = usernameField.getText().toString();
		String passwd = passwdField.getText().toString();
		
	}
}
