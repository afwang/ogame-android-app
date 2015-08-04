package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.OgameResources;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.utils.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;

/**
 * Created by kevinleperf on 22/07/15.
 */
public abstract class DefaultItemParser<T extends AbstractItemInformation> extends AbstractParser<T> {
    protected abstract T createDefaultItemInformationInstance();

    @Override
    public T parse(InputStream stream, OgameResources resources) {
        return parse(consumeStream(stream), resources);
    }

    @Override
    public T parse(String raw, OgameResources resources) {
        T information = createDefaultItemInformationInstance();
        information.setCurrentCardinal(0);
        information.setBuildable(Constants.Buildable.BUILDABLE);

        Document document = Jsoup.parse(raw);
        Elements divs = document.getElementsByTag("div");

        for (Element div : divs) {
            if ("content".equals(div.id())) {
                Element span = div.getElementsByTag("span").first();
                if (span != null && "level".equals(span.className().trim())) {
                    String[] split = stripTags(span.childNodes()).trim().split(" ");
                    if (split.length > 0) {
                        String cardinal = split[split.length - 1];
                        try {
                            information.setCurrentCardinal(optLong(cardinal));
                        } catch (Exception e) {
                            //
                        }
                    }
                }

                Elements internal_divs = div.getElementsByTag("div");
                if (internal_divs != null) {
                    for (Element internal_div : internal_divs) {
                        if ("costs_wrap".equals(internal_div.className())) {
                            extractCostsWrap(information, internal_div);
                        } else if ("build-it_wrap".equals(internal_div.className())) {
                            extractBuildItWrap(information, internal_div);
                        }
                    }
                }

                Element ul = div.getElementsByTag("ul").first();
                if (ul != null) {
                    Elements lis = ul.getElementsByTag("li");
                    if (lis != null) {
                        for (Element li : lis) {
                            if (li != null) {//&& li.hasClass("production_info")) {
                                span = li.getElementsByTag("span").first();
                                if (span != null && "buildDuration".equals(span.id())) {
                                    String time = span.text().trim();
                                    long duration = 0;
                                    String split[] = time.split(" ");
                                    for (String sub_string : split) {
                                        duration += extractDurationFromString(sub_string);
                                    }
                                    information.setDuration(duration);
                                } else if (span != null && "possibleInTime".equals(span.id())) {
                                    information.setPossibleInSeconds(optLong(span.text().trim()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return information;
    }

    private void extractCostsWrap(T information, Element current_element) {
        Element ul = current_element.getElementsByTag("ul").first();
        if (ul == null) return;

        Elements lis = ul.getElementsByTag("li");
        if (lis != null) {
            for (Element li : lis) {
                extractCost(information, li);
            }
        }
    }

    private void extractCost(T information, Element li) {
        if (li == null) return;

        long cost = -1;
        Elements divs = li.getElementsByTag("div");
        if (divs != null) {
            for (Element div : divs) {
                if (div.hasClass("cost")) cost = optLong(stripTags(div.childNodes()).trim());
                if (div.hasClass("overmark"))
                    information.setBuildable(Constants.Buildable.IMPOSSIBLE);
            }
        }

        if (li.hasClass("metal")) information.setMetalCost(cost);
        else if (li.hasClass("crystal")) information.setCrystalCost(cost);
        else if (li.hasClass("deuterium")) information.setDeuteriumCost(cost);
    }

    private void extractBuildItWrap(T information, Element current_element) {
        if (current_element == null) return;
        Element a = current_element.getElementsByTag("a").first();
        if (a == null) return;

        if (a.hasClass("build-it_disabled")) {
            information.setBuildable(Constants.Buildable.DISABLED);
        } else if (a.hasClass("build-it_premium")) {
            //information.setActivable(true);//but can not build for now
        } else {
            //
        }
    }
}
