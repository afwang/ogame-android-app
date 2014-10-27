package com.wikaba.ogapp;

import java.util.List;

import com.wikaba.ogapp.agent.FleetEvent;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class OverviewFragment extends Fragment implements ServiceConnection, LoaderManager.LoaderCallbacks<List<FleetEvent>> {
	static final String ACC_ROWID = "account_row_id";
	
	private ListView eventFleetView;
	private HomeActivity act;
	
	public OverviewFragment() {
	}
	
	@Override
	public void onAttach(Activity act) {
		this.act = (HomeActivity)act;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle args = getArguments();
		
		View root = inflater.inflate(R.layout.fragment_overview, container, false);
		
		eventFleetView = (ListView)root.findViewById(R.id.eventFleetView);

		return root;
	}
	
	@Override
	public Loader<List<FleetEvent>> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<List<FleetEvent>> arg0,
			List<FleetEvent> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<List<FleetEvent>> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		//TODO: Get fleet event data. Set adapter for eventFleetView
		//Start AsyncTaskLoader.
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		//TODO: Stop AsyncTaskLoader.
	}
	
	private static class FleetEventLoader extends AsyncTaskLoader<List<FleetEvent>> {

		public FleetEventLoader(Context context) {
			super(context);
		}
		
		@Override
		protected void onStartLoading() {
		}
		
		@Override
		protected void onStopLoading() {
		}
		
		@Override
		protected void onReset() {
		}

		@Override
		public List<FleetEvent> loadInBackground() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
