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

package com.wikaba.ogapp.agent.parsers.pages;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.models.OverviewData;
import com.wikaba.ogapp.agent.parsers.AbstractParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class FleetEventParser extends AbstractParser<OverviewData> {

    public OverviewData parse(InputStream stream, OgameResources ressources) {
        return parse(consumeStream(stream).toString(), ressources);
    }

    public OverviewData parse(String raw, OgameResources ressources) {

        OverviewData data = new OverviewData();

        Document document = Jsoup.parse(raw);
        data._fleet_event = getFleetEvent(document);

        Elements div_content_boxs = document.getElementsByClass("content-box-s");

        int index = 0;
        for (Element div_content_box : div_content_boxs) {
            if (div_content_box != null) {
                Element content = div_content_box.getElementsByClass("content").first();
                if (content != null) {
                    Element table = content.getElementsByTag("table").first();
                    if (table != null) {
                        Element tbody = content.getElementsByTag("tbody").first();
                        if (tbody != null) {
                            Elements trs = tbody.getElementsByTag("tr");
                            switch (index) {
                                case 0:
                                    extractBuildingInProgress(data, trs);
                                    break;
                                case 1:
                                    extractResearchInProgress(data, trs);
                                    break;
                                case 2:
                                    extractShipInProgress(data, trs);
                                    break;
                                default://nothing
                            }
                        }
                    }
                }
            }
            index++;
        }

        return data;
    }

    private void extractShipInProgress(OverviewData data, Elements trs) {
        for (Element tr : trs) {
            Elements divs = tr.getElementsByClass("shipSumCount");
            for (Element div : divs) {
                if (div != null) {
                    data._current_ship_count = optLong(div.text());
                    break;
                }
            }

            Element a = tr.getElementsByTag("a").first();
            if (a != null) {
                String href = a.attr("href");
                if (href != null && href.indexOf("shipyard") > 0) {
                    String[] split = href.split("\\?");
                    if (split.length > 1) {
                        for (int i = 1; i < split.length; i++) {
                            if (split[i].indexOf("openTech") >= 0) {
                                split = split[i].split("=");
                                if (split.length > 1) {
                                    data._current_ship_id = optLong(split[1]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void extractResearchInProgress(OverviewData data, Elements trs) {
        extractBuildingInProgress(data, trs);
    }

    private void extractBuildingInProgress(OverviewData data, Elements trs) {
        for (Element tr : trs) {
            Element a = tr.getElementsByTag("a").first();
            if (a != null && a.attr("onclick").length() > 0) {
                String information = a.attr("onclick");
                if (information.indexOf(",") >= 0) {
                    //have information
                    String[] split = information.split(",");
                    if (split.length > 0) {
                        information = split[0];
                        if (information.indexOf("(") >= 0) {
                            split = information.split("\\(");
                            if (split.length > 1) {
                                data._current_building_id = optLong(split[1]);
                            }
                        }
                    }
                }
            }

            Elements spans = tr.getElementsByTag("span");
            for (Element span : spans) {
                if (span != null && span.hasClass("level")) {
                    String text = span.text();
                    if (text != null) {
                        String[] split = span.text().trim().split(" ");
                        if (split.length > 1) {
                            data._current_building_level = optLong(split[1]);
                        }
                    }
                }
            }
        }
    }

    private List<FleetEvent> getFleetEvent(Document document) {
        List<FleetEvent> eventList = new LinkedList<>();

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
