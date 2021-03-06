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

package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.LinkHTML;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by kevinleperf on 19/07/15.
 */
public abstract class AbstractParser<T> {
    public abstract T parse(InputStream stream, OgameResources resources);

    public abstract T parse(String raw, OgameResources resources);

    protected String consumeStream(InputStream stream) {
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
        return buffer.toString();
    }

    /**
     * Pre-condition: The current state of the XmlPullParser xpp is at a START_TAG
     *
     * @param xpp       the xpp instance used to parse the content
     * @param attrName  the attribute used in the current tag
     * @param attrValue the expected attribute value
     * @return
     */
    protected boolean hasAttrValue(XmlPullParser xpp, String attrName, String attrValue) {
        //Scan attributes for "class" attribute.
        int attrsize = xpp.getAttributeCount();
        int index;
        boolean attrFound = false;
        for (index = 0; index < attrsize; index++) {
            String currentAttrName = xpp.getAttributeName(index);
            if (currentAttrName.equals(attrName)) {
                attrFound = true;
                break;
            }
        }

        if (!attrFound)
            return false;
        else {
            String currentAttrValue = xpp.getAttributeValue(index);
            if (currentAttrValue.equals(attrValue)) {
                return true;
            }
        }

        return false;
    }

    protected LinkHTML extractFirstLink(Element element) {
        Elements elements = element.getElementsByTag("a");
        if (elements != null && elements.size() > 0) {
            element = elements.get(0);
            LinkHTML link = new LinkHTML();
            link.href = element.attr("href");
            link.text = element.text();
            return link;
        }
        return null;
    }

    protected String stripTags(List<Node> node_list) {
        StringBuilder builder = new StringBuilder();
        stripTagsInternal(builder, node_list);
        return builder.toString();
    }

    private void stripTagsInternal(StringBuilder builder, List<Node> node_list) {
        for (Node node : node_list) {
            if (node instanceof TextNode) {
                builder.append(node.toString());
            } else {
                //TODO LINEAR CALL WITH TEMPORARY LIST
                stripTagsInternal(builder, node.childNodes());
            }
        }
    }

    protected int optInt(String str) {
        try {
            if (str != null) {
                return Integer.parseInt(str.replaceAll("\\.", "").replaceAll(",", "").trim());
            }
        } catch (Exception e) {

        }
        return 0;
    }

    protected long optLong(String str) {
        try {
            if (str != null) {
                return Long.parseLong(str.replaceAll("\\.", "").replaceAll(",", "").trim());
            }
        } catch (Exception e) {

        }
        return 0;
    }

    protected boolean optBoolean(String str) {
        try {
            if (str != null) {
                return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1");
            }
        } catch (Exception e) {

        }
        return false;
    }

    protected long extractDurationFromString(String string) {
        if (string == null || string.length() == 0) {
            return 0;
        }

        long result = optLong(string.replaceAll("[^\\d.]", ""));
        if (result == 0) return 0;

        if (string.indexOf("j") >= 0) {
            result *= 24 * 60 * 60;
        } else if (string.indexOf("h") >= 0) {
            result *= 60 * 60;
        } else if (string.indexOf("m") >= 0) {
            result *= 60;
        }
        return result;
    }
}
