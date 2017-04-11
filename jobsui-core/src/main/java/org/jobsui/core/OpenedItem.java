package org.jobsui.core;

/**
 * Created by enrico on 3/29/17.
 */
public class OpenedItem {
    public final String url;
    public final String name;

    public OpenedItem(String url, String name) {
        this.url = url;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + url + ")";
    }

}
