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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenedItem that = (OpenedItem) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
