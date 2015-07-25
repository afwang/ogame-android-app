package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.PlanetResources;
import com.wikaba.ogapp.agent.models.Resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;

/**
 * Created by kevinleperf on 25/07/15.
 */
public class PlanetResourceParser extends AbstractParser<PlanetResources> {
    @Override
    public PlanetResources parse(InputStream stream, OgameResources resources) {
        return parse(consumeStream(stream), resources);
    }

    @Override
    public PlanetResources parse(String raw, OgameResources resources) {
        PlanetResources instance = new PlanetResources();

        Document document = Jsoup.parse(raw);
        Element metal_box = document.getElementById("metal_box");
        Element crystal_box = document.getElementById("crystal_box");
        Element deuterium_box = document.getElementById("deuterium_box");

        instance.metal = extractResourceFromElement(metal_box);
        instance.crystal = extractResourceFromElement(crystal_box);
        instance.deuterium = extractResourceFromElement(deuterium_box);

        return instance;
    }

    private Resource extractResourceFromElement(Element element) {
        if (element != null) {
            String content = element.attr("title");

            if (content != null && content.length() > 0) {
                Document obtained = Jsoup.parse(content);

                Elements spans = obtained.getElementsByTag("span");
                Resource resource = new Resource();

                resource.current_quantity = extractLong(spans, 0);
                resource.storage_capacity = extractLong(spans, 1);
                resource.current_production = extractLong(spans, 2);
                resource.stash_quantity = extractLong(spans, 3);
                return resource;
            }
        }
        return null;
    }

    private long extractLong(Elements elements, int index) {
        if (elements != null && elements.size() > index) {
            Element element = elements.get(index);
            if (element != null && element.text() != null) {
                return optLong(element.text());
            }
        }
        return -1;
    }
}
