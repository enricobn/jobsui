package org.jobsui.core;

import java.util.Objects;

/**
 * Created by enrico on 3/29/17.
 */
public class OpenedItem {
    private final String url;
    private final String name;

    public OpenedItem(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + url + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenedItem that = (OpenedItem) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }
}
