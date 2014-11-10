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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class OverviewFragment extends Fragment
		implements
			ServiceConnection,
			LoaderManager.LoaderCallbacks<List<FleetEvent>>,
			AbsListView.RecyclerListener,
			Runnable {
	static final String ACC_ROWID = "account_row_id";
	
	private static final int LOADER_ID = 0;
	
	private ListView eventFleetView;
	private HomeActivity act;
	private Map<TextView, Long> etaTextViews;
	private Handler handler;
	
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
		etaTextViews = new HashMap<TextView, Long>();
		handler = new Handler();

		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		act.setListener(this);
		if(act.mBound) {
			onServiceConnected(null, null);
		}
	}
	
	@Override
	public Loader<List<FleetEvent>> onCreateLoader(int arg0, Bundle arg1) {
		FleetEventLoader loader = new FleetEventLoader(act);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<FleetEvent>> loader, List<FleetEvent> events) {
		eventFleetView.setAdapter(new EventAdapter(act, events, this));
		final long timeInMillis = 1000;
		handler.postDelayed(this, timeInMillis);
	}

	@Override
	public void onLoaderReset(Loader<List<FleetEvent>> arg0) {
		//Do not have to do anything, since our resource sits in memory.
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
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
			
			this.forceLoad();
		}
		
		@Override
		protected void onStopLoading() {
		}
		
		@Override
		protected void onReset() {
		}

		@Override
		public List<FleetEvent> loadInBackground() {
			act.mAgent.loginToAccount(act.accountRowId);
			return act.mAgent.getFleetEvents(act.accountRowId);
		}
	}
	
	private class EventAdapter extends BaseAdapter {
		private Context context;
		private List<FleetEvent> eventList;
		private AbsListView.RecyclerListener listener;
		
		public EventAdapter(Context ctx, List<FleetEvent> eventList, AbsListView.RecyclerListener listener) {
			context = ctx;
			
			//Copy elements over to ArrayList to ensure random access to the elements in the list.
			this.eventList = new ArrayList<FleetEvent>(eventList.size());
			this.eventList.addAll(eventList);
			this.listener = listener;
		}

		@Override
		public int getCount() {
			if(eventList != null)
				return eventList.size();
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
			etaTextViews.put(holder.eta, event.data_arrival_time);
			
			long currentTime = Calendar.getInstance().getTimeInMillis() / 1000;
			long timeLeft = event.data_arrival_time - currentTime;
			eta.setText(DateUtils.formatElapsedTime(timeLeft));
			originCoords.setText(event.coordsOrigin);
			
			Resources res = getResources();
			outOrIn.setText(event.data_return_flight ? res.getString(R.string.overview_incoming) : res.getString(R.string.overview_outgoing));
			
			destCoords.setText(event.destCoords);
			missionType.setText(IntegerMissionMap.getMission(event.data_mission_type));
			
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
			for(String shipName : civilShipNames) {
				num = data.get(shipName);
				if(num != null && num.longValue() > 0) {
					TextView shipEntry = new TextView(context);
					shipEntry.setText(num.longValue() + ' ' + shipName);
					civilShipLayout.addView(shipEntry);
				}
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
			for(String shipName : combatShipNames) {
				num = data.get(shipName);
				if(num != null && num.longValue() > 0) {
					TextView shipEntry = new TextView(context);
					shipEntry.setText(num.longValue() + ' ' + shipName);
					combatShipLayout.addView(shipEntry);
				}
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
			for(String resName : resourceNames) {
				num = data.get(resName);
				if(num != null && num.longValue() > 0) {
					TextView resEntry = new TextView(context);
					resEntry.setText(num.longValue() + ' ' + resName);
					resourceLayout.addView(resEntry);
				}
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
