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

package com.wikaba.ogapp.agent.parsers.pages;

import android.util.Log;

import com.wikaba.ogapp.agent.FleetAndResources;
import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.ResourceItem;
import com.wikaba.ogapp.agent.parsers.AbstractParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class ResourcesParser extends AbstractParser<List<ResourceItem>> {
    private final static String OVERMARK = "overmark";
    private final static String UNDERMARK = "undermark";
    private final static String CLASS = "class";
    private final static String CLASS_METAL = "metall"; //energyManagerIcon
    private final static String CLASS_CRYSTAL = "crystal";//energyManagerIcon
    private final static String CLASS_DEUTERIUM = "deuterium";//energyManagerIcon
    private final static String ARRAY_BEGIN = "<table cellpadding=\"0\" class=\"list listOfResourceSettingsPerPlanet";
    private final static String ARRAY_END = "</table>";

    @Override
    public List<ResourceItem> parse(InputStream strea, OgameResources ressources) {
        throw new NoSuchMethodError("Resources must be created from a raw request");
    }

    @Override
    public List<ResourceItem> parse(String raw, OgameResources ressources) {
        int begin = raw.indexOf(ARRAY_BEGIN);
        int end = raw.indexOf(ARRAY_END, begin + ARRAY_BEGIN.length()) + ARRAY_END.length();

        Log.d("AgentService", begin + " " + end);
        if (begin > 0 && end > begin) {
            String sub = raw.substring(begin, end + 1);

            System.out.println(sub);
            return fetchResourcesFromXmlString(sub);
        }
        return null;
    }

    /**
     * Retransform the array to a corresponding list of resources elements
     *
     * @param xml_compatible_array
     * @return
     */
    private List<ResourceItem> fetchResourcesFromXmlString(String xml_compatible_array) {
        try {
            Document doc = Jsoup.parse(xml_compatible_array);

            Elements elements = doc.getElementsByTag("tr");

            List<ResourceItem> resources_result = new ArrayList<>();
            ResourceItem resource_item = null;

            for (Element element : elements) {
                boolean has_overmark = false;
                boolean has_undermark = false;

                Elements spans = element.getElementsByTag("span");
                if (spans != null && spans.size() > 0) {
                    //retrieve the current element corresponding to the actual player resources
                    for (Element span : spans) {
                        if (span.hasClass(CLASS_METAL)) {
                            if (resource_item != null) resources_result.add(resource_item);
                            resource_item = new ResourceItem(FleetAndResources.Resources.METAL);
                        } else if (span.hasClass(CLASS_CRYSTAL)) {
                            if (resource_item != null) resources_result.add(resource_item);
                            resource_item = new ResourceItem(FleetAndResources.Resources.CRYSTAL);
                        } else if (span.hasClass(CLASS_DEUTERIUM)) {
                            if (resource_item != null) resources_result.add(resource_item);
                            resource_item = new ResourceItem(FleetAndResources.Resources.DEUTERIUM);
                        }
                    }

                    //retrieve the consumption AND the production
                    if (resource_item != null) {
                        for (Element span : spans) {
                            if (span.hasClass(OVERMARK) && !has_overmark) {
                                resource_item.resource_consumption = span.text();
                                has_overmark = true;
                            } else if (span.hasClass(UNDERMARK) && !has_undermark) {
                                resource_item.resource_production = Long.parseLong(span.text()
                                        .trim().replace(".", ""));
                                has_undermark = true;
                            }
                        }
                    }
                }

                if (resource_item != null) resources_result.add(resource_item);
                resource_item = null;
            }


            return resources_result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getRightNextText(XmlPullParser xpp) throws IOException, XmlPullParserException {
        int type = xpp.next();

        if (type == XmlPullParser.TEXT) {
            return xpp.getText();
        }
        return null;
    }
}
