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

package com.wikaba.ogapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.wikaba.ogapp.agent.FleetAndResources;
import com.wikaba.ogapp.agent.FleetEvent;
import com.wikaba.ogapp.agent.IntegerMissionMap;
import com.wikaba.ogapp.utils.AndroidMissionMap;
import com.wikaba.ogapp.utils.NameBridge;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewFragment extends Fragment
		implements
			AgentServiceConsumer,
			LoaderManager.LoaderCallbacks<List<FleetEvent>>,
			AbsListView.RecyclerListener,
			Runnable,
			View.OnClickListener {
	
	private static final String HEADER_KEY = "com.wikaba.ogapp.OverviewFragment.HEADER";
	static final String USERNAME_KEY = "com.wikaba.ogapp.OverviewFragment.USERNAME";
	static final String UNIVERSE_KEY = "com.wikaba.ogapp.OverviewFragment.UNIVERSE";
	static final String ACC_ROWID = "account_row_id";
	
	private static final int LOADER_ID = 0;
	
	private TextView header;
	private ListView eventFleetView;
	private ProgressBar progressWheel;
	private TextView noEventsText;
	private Button reload;
	private HomeActivity act;
	private Map<TextView, Long> etaTextViews;
	private Handler handler;
	private boolean dataIsLoaded;
	private boolean fragmentRunning;
	public OverviewFragment() {
	}
	
	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		this.act = (HomeActivity)act;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_overview, container, false);
		
		eventFleetView = (ListView)root.findViewById(R.id.eventFleetView);
		progressWheel = (ProgressBar)root.findViewById(R.id.progressBar1);
		noEventsText = (TextView)root.findViewById(R.id.no_events);
		reload = (Button)root.findViewById(R.id.refresh_button);
		header = (TextView)root.findViewById(R.id.name_universe);
		etaTextViews = new HashMap<TextView, Long>();
		handler = new Handler();
		dataIsLoaded = false;
		fragmentRunning = false;
		setHasOptionsMenu(true);
		
		reload.setOnClickListener(this);

		if(savedInstanceState == null) {
			Bundle args = this.getArguments();
			String username;
			String universe;
			if(args != null) {
				username = args.getString(USERNAME_KEY);
				universe = args.getString(UNIVERSE_KEY);
			}
			else {
				username = "ERROR";
				universe = "ERROR";
			}
			
			header.setText(universe + ", " + username);
		}
		else {
			String headerText = savedInstanceState.getString(HEADER_KEY);
			if(headerText == null) {
				headerText = "";
			}
			header.setText(headerText);
		}

		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		final long timeInMillis = 1000;
		act.setListener(this);
		if(act.isBound()) {
			serviceConnected();
		}
		
		if(dataIsLoaded) {
			handler.postDelayed(this, timeInMillis);
		}
		
		fragmentRunning = true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		String uniText = header.getText().toString();
		outState.putString(HEADER_KEY, uniText);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		fragmentRunning = false;
		handler.removeCallbacks(this);
		act.unsetListener();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		act = null;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.overview, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if(itemId == R.id.switch_accounts) {
			act.goToAccountSelector();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Loader<List<FleetEvent>> onCreateLoader(int arg0, Bundle arg1) {
		eventFleetView.setVisibility(View.GONE);
		progressWheel.setVisibility(View.VISIBLE);
		FleetEventLoader loader = new FleetEventLoader(act);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<FleetEvent>> loader, List<FleetEvent> events) {
		progressWheel.setVisibility(View.GONE);
		reload.setVisibility(View.VISIBLE);
		
		Resources res = getResources();
		if(events == null) {
			noEventsText.setText(res.getString(R.string.error_msg));
			events = new ArrayList<FleetEvent>();
			Toast.makeText(act, R.string.login_error, Toast.LENGTH_LONG).show();
		}
		else {
			noEventsText.setText(res.getString(R.string.no_events));
		}
		
		if(events.size() > 0) {
			eventFleetView.setVisibility(View.VISIBLE);
			noEventsText.setVisibility(View.GONE);
		}
		else {
			eventFleetView.setVisibility(View.GONE);
			noEventsText.setVisibility(View.VISIBLE);
		}
		
		eventFleetView.setAdapter(new EventAdapter(act, events, etaTextViews));
		eventFleetView.setRecyclerListener(this);
		final long timeInMillis = 1000;
		if(fragmentRunning)
			handler.postDelayed(this, timeInMillis);
		dataIsLoaded = true;
	}

	@Override
	public void onLoaderReset(Loader<List<FleetEvent>> arg0) {
		//Do not have to do anything, since our resource sits in memory.
	}

	/**
	 * <p>This is the callback available to indicate to us when the
	 * service is connected and ready for action!</p>
	 */
	@Override
	public void serviceConnected() {
		if(act == null) {
			return;
		}

		if(fragmentRunning) {
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}

	@Override
	public void serviceDisconnected() {
		getLoaderManager().destroyLoader(LOADER_ID);
		act.unsetListener();
	}

	@Override
	public void onMovedToScrapHeap(View v) {
		EventViewHolder holder = (EventViewHolder)v.getTag();
		etaTextViews.remove(holder.eta);
	}

	@Override
	public void run() {
		Iterator<Entry<TextView, Long>> entryiter = etaTextViews.entrySet().iterator();
		while(entryiter.hasNext()) {
			Entry<TextView, Long> anEntry = entryiter.next();
			TextView textview = anEntry.getKey();
			long arrivalInEpoch = anEntry.getValue().longValue();
			long currentTime = Calendar.getInstance().getTimeInMillis() / 1000;
			
			long timeLeft = arrivalInEpoch - currentTime;
			if(timeLeft <= 0) {
				textview.setText(getResources().getString(R.string.overview_arrived));
			}
			else {
				textview.setText(DateUtils.formatElapsedTime(timeLeft));
			}
		}
		final long timeInMillis = 1000;
		handler.postDelayed(this, timeInMillis);
	}

	@Override
	public void onClick(View v) {
		if(v == reload) {
			eventFleetView.setVisibility(View.GONE);
			noEventsText.setVisibility(View.GONE);
			progressWheel.setVisibility(View.VISIBLE);
			reload.setVisibility(View.INVISIBLE);
			if(act.isBound()) {
				getLoaderManager().getLoader(LOADER_ID).onContentChanged();
			}
			Toast.makeText(act, R.string.reload_in_progress, Toast.LENGTH_SHORT).show();
		}
	}

	private static class FleetEventLoader extends AsyncTaskLoader<List<FleetEvent>> {
		private List<FleetEvent> oldData;
		private HomeActivity act;

		public FleetEventLoader(HomeActivity context) {
			super(context);
			oldData = null;
			act = context;
		}

		@Override
		protected void onStartLoading() {
			if(oldData != null)
				deliverResult(oldData);
			
			boolean contentChanged = takeContentChanged();
			if(oldData == null || contentChanged) {
				this.forceLoad();
			}
		}

		@Override
		public void deliverResult(List<FleetEvent> data) {
			oldData = data;
			super.deliverResult(data);
		}
		
		@Override
		protected void onStopLoading() {
		}
		
		@Override
		protected void onReset() {
			if(oldData != null) {
				oldData.clear();
				oldData = null;
			}
		}

		@Override
		public List<FleetEvent> loadInBackground() {
			AgentService service = act.getAgentService();
			return service.getFleetEvents(act.getAccountRowId());
		}
	}

	private static class EventAdapter extends BaseAdapter {
		private Context context;
		private List<FleetEvent> eventList;
		private Map<TextView, Long> textViews;
		private IntegerMissionMap missionBridge;
		private NameBridge nameBridge;
		private Resources res;
		
		public EventAdapter(Context ctx, List<FleetEvent> eventList, Map<TextView, Long> textViews) {
			context = ctx;
			
			//Copy elements over to ArrayList to ensure random access to the elements in the list.
			int size = 0;
			if(eventList != null) {
				size = eventList.size();
			}
			this.eventList = new ArrayList<FleetEvent>(size);
			if(eventList != null) {
				this.eventList.addAll(eventList);
			}
			
			if(textViews == null) {
				throw new IllegalArgumentException("Third argument should not be null");
			}
			this.textViews = textViews;
			
			res = context.getResources();
			missionBridge = new IntegerMissionMap(new AndroidMissionMap(res));
			nameBridge = new NameBridge(res);
		}

		@Override
		public int getCount() {
			if(eventList != null) {
				return eventList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if(eventList != null) {
				return null;
			}
			return eventList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			EventViewHolder holder;
			if(v == null) {
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.fleet_event_view, parent, false);
				holder = new EventViewHolder();
				holder.eta = (TextView)v.findViewById(R.id.eta);
				holder.originCoords = (TextView)v.findViewById(R.id.origin_coords);
				holder.outOrIn = (TextView)v.findViewById(R.id.outgoing_or_incoming);
				holder.destCoords = (TextView)v.findViewById(R.id.dest_coords);
				holder.missionType = (TextView)v.findViewById(R.id.mission_type);
				holder.civilShips = (LinearLayout)v.findViewById(R.id.civil_ship_layout);
				holder.combatShips = (LinearLayout)v.findViewById(R.id.combat_ship_layout);
				holder.resources = (LinearLayout)v.findViewById(R.id.resource_layout);
				v.setTag(holder);
			}
			else {
				holder = (EventViewHolder)v.getTag();
			}
			
			FleetEvent event = eventList.get(position);
			TextView eta = holder.eta;
			TextView originCoords = holder.originCoords;
			TextView outOrIn = holder.outOrIn;
			TextView destCoords = holder.destCoords;
			TextView missionType = holder.missionType;
			LinearLayout civilShips = holder.civilShips;
			LinearLayout combatShips = holder.combatShips;
			LinearLayout resources = holder.resources;
			
			//We have to add the text view to the Map whether we are creating a new view or recycling an old one.
			//The recycled view might have went through the recycler listener (in which case it was removed from the
			//Map)
			textViews.put(holder.eta, event.data_arrival_time);
			
			long currentTime = Calendar.getInstance().getTimeInMillis() / 1000;
			long timeLeft = event.data_arrival_time - currentTime;
			eta.setText(DateUtils.formatElapsedTime(timeLeft));
			originCoords.setText(event.coordsOrigin);
			
			Resources res = context.getResources();
			outOrIn.setText(event.data_return_flight ? res.getString(R.string.overview_incoming) : res.getString(R.string.overview_outgoing));
			
			destCoords.setText(event.destCoords);
			missionType.setText(missionBridge.getMission(event.data_mission_type));
			
			addCivilData(event, civilShips);
			addCombatData(event, combatShips);
			addResourceData(event, resources);
			
			return v;
		}
		
		private void addCivilData(FleetEvent eventData, LinearLayout civilShipLayout) {
			Map<String, Long> data = eventData.fleetResources;
			final String[] civilShipNames = {
					FleetAndResources.SC,
					FleetAndResources.LC,
					FleetAndResources.COLONY,
					FleetAndResources.RC,
					FleetAndResources.EP
			};
			
			Long num;
			int layoutIndex = 0;
			for(String shipName : civilShipNames) {
				num = data.get(shipName);
				if(num != null && num.longValue() > 0) {
					TextView shipEntry;
					if(layoutIndex < civilShipLayout.getChildCount()) {
						shipEntry = (TextView)civilShipLayout.getChildAt(layoutIndex);
					}
					else {
						shipEntry = new TextView(context);
						int size = res.getDimensionPixelSize(R.dimen.fleet_event_ship_text);
						shipEntry.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
						civilShipLayout.addView(shipEntry);
					}
					
					shipName = nameBridge.getName(shipName);
					StringBuilder textBuilder = new StringBuilder();
					textBuilder.append(num.longValue())
					.append(' ')
					.append(shipName);
					String shipStr = textBuilder.toString();
					shipEntry.setText(shipStr);

					layoutIndex++;
				}
			}
			
			//Remove extraneous views
			/* One way to think about layoutIndex after finishing the above loop
			 * is that layoutIndex is the number of children in the layout with
			 * the updated data. The child views starting at position layoutIndex
			 * to the end should be removed. Thus, we only have to remove
			 * children from the layout when layoutIndex is less than
			 * civilShipLayout.getChildCount().
			 */
			if(layoutIndex < civilShipLayout.getChildCount()) {
				int count = civilShipLayout.getChildCount() - layoutIndex;
				civilShipLayout.removeViews(layoutIndex, count);
			}
		}
		
		private void addCombatData(FleetEvent eventData, LinearLayout combatShipLayout) {
			Map<String, Long> data = eventData.fleetResources;
			final String[] combatShipNames = {
					FleetAndResources.LF,
					FleetAndResources.HF,
					FleetAndResources.CR,
					FleetAndResources.BS,
					FleetAndResources.BC,
					FleetAndResources.BB,
					FleetAndResources.DS,
					FleetAndResources.RIP
			};
			
			Long num;
			int layoutIndex = 0;
			for(String shipName : combatShipNames) {
				num = data.get(shipName);
				if(num != null && num.longValue() > 0) {
					TextView shipEntry;
					if(layoutIndex < combatShipLayout.getChildCount()) {
						shipEntry = (TextView)combatShipLayout.getChildAt(layoutIndex);
					}
					else {
						shipEntry = new TextView(context);
						int size = res.getDimensionPixelSize(R.dimen.fleet_event_ship_text);
						shipEntry.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
						combatShipLayout.addView(shipEntry);
					}
					
					shipName = nameBridge.getName(shipName);
					StringBuilder textBuilder = new StringBuilder();
					textBuilder.append(num.longValue())
					.append(' ')
					.append(shipName);
					String shipStr = textBuilder.toString();
					shipEntry.setText(shipStr);
					
					layoutIndex++;
				}
			}
			
			if(layoutIndex < combatShipLayout.getChildCount()) {
				int count = combatShipLayout.getChildCount() - layoutIndex;
				combatShipLayout.removeViews(layoutIndex, count);
			}
		}
		
		private void addResourceData(FleetEvent eventData, LinearLayout resourceLayout) {
			Map<String, Long> data = eventData.fleetResources;
			final String[] resourceNames = {
					FleetAndResources.METAL,
					FleetAndResources.CRYSTAL,
					FleetAndResources.DEUT
			};
			
			Long num;
			int layoutIndex = 0;
			for(String resName : resourceNames) {
				num = data.get(resName);
				if(num != null) {
					TextView resEntry;
					if(layoutIndex < resourceLayout.getChildCount()) {
						resEntry = (TextView)resourceLayout.getChildAt(layoutIndex);
					}
					else {
						resEntry = new TextView(context);
						int size = res.getDimensionPixelSize(R.dimen.fleet_event_ship_text);
						resEntry.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
						resourceLayout.addView(resEntry);
					}
					
					resName = nameBridge.getName(resName);
					StringBuilder textBuilder = new StringBuilder();
					textBuilder.append(num.longValue())
					.append(' ')
					.append(resName);
					String resStr = textBuilder.toString();
					resEntry.setText(resStr);
					
					layoutIndex++;
				}
			}

			if(layoutIndex < resourceLayout.getChildCount()) {
				int count = resourceLayout.getChildCount() - layoutIndex;
				resourceLayout.removeViews(layoutIndex, count);
			}
		}
	}
	
	private static class EventViewHolder {
		TextView eta;
		TextView originCoords;
		TextView outOrIn;
		TextView destCoords;
		TextView missionType;
		LinearLayout civilShips;
		LinearLayout combatShips;
		LinearLayout resources;
	}
}
