package org.jobsui.core;

/**
 * Created by enrico on 3/29/17.
 */
public class OpenedItem {
    public final String project;
    public final String job;

    public OpenedItem(String project, String job) {
        this.project = project;
        this.job = job;
    }

    @Override
    public String toString() {
        return project + '/' + job;
    }

}
