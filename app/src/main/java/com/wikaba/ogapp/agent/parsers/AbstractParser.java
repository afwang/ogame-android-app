package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Created by kevinleperf on 19/07/15.
 */
public abstract class AbstractParser<T> {
    public abstract T parse(InputStream strea, OgameResources ressources);

    public abstract T parse(String raw, OgameResources ressources);

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
}
