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

package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.FleetEvent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FleetEventParser extends AbstractParser<List<FleetEvent>> {

    public List<FleetEvent> parse(InputStream stream, OgameResources ressources) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        StringBuffer buffer = new StringBuffer(1024);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parse(buffer.toString(), ressources);
    }

    public List<FleetEvent> parse(String raw, OgameResources ressources) {
        List<FleetEvent> eventList = new LinkedList<>();

        Document document = Jsoup.parse(raw);

        Element table_event = null;
        if (document != null) {
            Elements tables = document.getElementsByTag("table");
            for (Element element : tables) {
                if ("eventContent".equals(element.id())) {
                    table_event = element;
                    break;
                }
            }
        }

        if (table_event != null) {
            Elements trs = table_event.getElementsByTag("tr");

            for (Element tr : trs) {
                if (tr.hasClass("eventFleet")) {
                    FleetEvent event = extractFleetEvent(tr);

                    if (event != null) {
                        eventList.add(event);
                    }
                }
            }
        }

        return eventList;
    }

    public FleetEvent extractFleetEvent(Element tr) {
        if (tr != null) {
            FleetEvent event = new FleetEvent();
            event.data_mission_type = optInt(tr.attr("data-mission-type"));
            event.data_arrival_time = optInt(tr.attr("data-arrival-time"));
            event.data_return_flight = optBoolean(tr.attr("data-return-flight"));


            Elements tds = tr.getElementsByTag("td");

            for (Element td : tds) {
                Element span = td.getElementsByTag("span").first();
                if (td.hasClass("originFleet")) {
                    event.originFleet = stripTags(td.childNodes()).trim();
                } else if (td.hasClass("destFleet")) {
                    event.destFleet = stripTags(td.childNodes()).trim();
                } else if (td.hasClass("coordsOrigin")) {
                    event.coordsOrigin = extractFirstLink(td);
                } else if (td.hasClass("destCoords")) {
                    event.destCoords = extractFirstLink(td);
                } else if (td.hasClass("icon_movement_reserve") || td.hasClass("icon_movement")) {
                    if (span != null) {
                        String composition = span.attr("title");
                        parseFleetResComposition(event, composition);
                    }
                }
            }
            return event;
        }

        return null;
    }

    /**
     * Unescapes the HTML-escaped data in the parameter string htmlEncodedData.
     * The string is then parsed with an XmlPullParser to extract fleet and resource
     * data. The data is inserted into a Map<String, Long> object. This Map is then
     * returned.
     *
     * @param htmlEncodedData - HTML-escaped string containing the details of the fleet breakdown and composition
     * @return a Map<String, Long> object containing fleet and resource composition. Keys are listed in
     * class FleetAndResources
     */
    private void parseFleetResComposition(FleetEvent output, String htmlEncodedData) {
        Document document = Jsoup.parse(htmlEncodedData);

        Element table = document.getElementsByTag("table").first();
        if (table != null && table.hasClass("fleetinfo")) {
            Elements children = table.getElementsByTag("tr");
            boolean has_header = false;
            boolean has_ships = false;
            boolean has_res = false;

            for (Element tr : children) {
                try {
                    if (!has_header && tr.children().size() == 1) {
                        has_header = true;
                    } else if (!has_ships && tr.children().size() == 1) {
                        //can be nbsp or shipment - called twice then
                        has_header = true;
                        has_ships = true;
                    } else if (!has_ships && tr.children().size() == 2) {
                        String name = tr.child(0).text().trim();
                        long value = optLong(tr.child(1).text());
                        output.fleet.put(name, value);
                    } else if (!has_res && tr.children().size() == 2) {
                        long value = optLong(tr.child(1).text());
                        if (output.resources.metal == -1) {
                            output.resources.metal = value;
                        } else if (output.resources.crystal == -1) {
                            output.resources.crystal = value;
                        } else if (output.resources.deuterium == -1) {
                            output.resources.deuterium = value;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (output.resources.metal == -1) {
                output.resources.metal = 0;
            }
            if (output.resources.crystal == -1) {
                output.resources.crystal = 0;
            }
            if (output.resources.deuterium == -1) {
                output.resources.deuterium = 0;
            }
        }
    }
}
