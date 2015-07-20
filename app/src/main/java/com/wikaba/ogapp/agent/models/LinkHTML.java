package com.wikaba.ogapp.agent.models;

/**
 * Created by kevinleperf on 20/07/15.
 */
public class LinkHTML implements Comparable<LinkHTML> {
    public String href;
    public String text;

    @Override
    public int compareTo(LinkHTML another) {
        if (text == null && (another == null || another.text == null)) return 0;
        if (text != null) return text.compareTo(another.text);
        return -1;
    }

    @Override
    public String toString() {
        return text;
    }
}
