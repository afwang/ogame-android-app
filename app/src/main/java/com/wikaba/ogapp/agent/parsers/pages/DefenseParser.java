package com.wikaba.ogapp.agent.parsers.pages;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.DefenseInformation;
import com.wikaba.ogapp.agent.parsers.DefaultItemParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class DefenseParser extends DefaultItemParser<DefenseInformation> {
    @Override
    protected DefenseInformation createDefaultItemInformationInstance() {
        return new DefenseInformation();
    }
}
