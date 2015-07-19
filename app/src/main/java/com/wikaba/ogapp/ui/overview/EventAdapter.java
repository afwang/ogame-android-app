package com.wikaba.ogapp.ui.overview;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.FleetAndResources;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.IntegerMissionMap;
import com.wikaba.ogapp.utils.AndroidMissionMap;
import com.wikaba.ogapp.utils.NameBridge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class EventAdapter extends BaseAdapter {
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
        if (eventList != null) {
            size = eventList.size();
        }
        this.eventList = new ArrayList<FleetEvent>(size);
        if (eventList != null) {
            this.eventList.addAll(eventList);
        }

        if (textViews == null) {
            throw new IllegalArgumentException("Third argument should not be null");
        }
        this.textViews = textViews;

        res = context.getResources();
        missionBridge = new IntegerMissionMap(new AndroidMissionMap(res));
        nameBridge = new NameBridge(res);
    }

    @Override
    public int getCount() {
        if (eventList != null) {
            return eventList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (eventList != null) {
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
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.fleet_event_view, parent, false);
            holder = new EventViewHolder();
            holder.eta = (TextView) v.findViewById(R.id.eta);
            holder.originCoords = (TextView) v.findViewById(R.id.origin_coords);
            holder.outOrIn = (TextView) v.findViewById(R.id.outgoing_or_incoming);
            holder.destCoords = (TextView) v.findViewById(R.id.dest_coords);
            holder.missionType = (TextView) v.findViewById(R.id.mission_type);
            holder.civilShips = (LinearLayout) v.findViewById(R.id.civil_ship_layout);
            holder.combatShips = (LinearLayout) v.findViewById(R.id.combat_ship_layout);
            holder.resources = (LinearLayout) v.findViewById(R.id.resource_layout);
            v.setTag(holder);
        } else {
            holder = (EventViewHolder) v.getTag();
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
        for (String shipName : civilShipNames) {
            num = data.get(shipName);
            if (num != null && num.longValue() > 0) {
                TextView shipEntry;
                if (layoutIndex < civilShipLayout.getChildCount()) {
                    shipEntry = (TextView) civilShipLayout.getChildAt(layoutIndex);
                } else {
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
        if (layoutIndex < civilShipLayout.getChildCount()) {
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
        for (String shipName : combatShipNames) {
            num = data.get(shipName);
            if (num != null && num.longValue() > 0) {
                TextView shipEntry;
                if (layoutIndex < combatShipLayout.getChildCount()) {
                    shipEntry = (TextView) combatShipLayout.getChildAt(layoutIndex);
                } else {
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

        if (layoutIndex < combatShipLayout.getChildCount()) {
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
        for (String resName : resourceNames) {
            num = data.get(resName);
            if (num != null) {
                TextView resEntry;
                if (layoutIndex < resourceLayout.getChildCount()) {
                    resEntry = (TextView) resourceLayout.getChildAt(layoutIndex);
                } else {
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

        if (layoutIndex < resourceLayout.getChildCount()) {
            int count = resourceLayout.getChildCount() - layoutIndex;
            resourceLayout.removeViews(layoutIndex, count);
        }
    }
}