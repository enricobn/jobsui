package org.jobsui.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIPreferencesImpl implements JobsUIPreferences {
    private final List<OpenedItem> lastOpenedProjects = new ArrayList<>();

    public static JobsUIPreferences get() {
        JobsUIPreferencesImpl preferences = new JobsUIPreferencesImpl();
        preferences.registerOpenedProject(new OpenedItem("jobsui-core/src/test/resources/simplejob", "simple"));
        preferences.registerOpenedProject(new OpenedItem("jobsui-core/src/test/resources/simplejob", "concat"));
        preferences.registerOpenedProject(new OpenedItem("tgk", "container"));
        preferences.registerOpenedProject(new OpenedItem("tgk", "pluto"));
        return preferences;
    }

    @Override
    public List<OpenedItem> getLastOpenedItems() {
        List<OpenedItem> reversed = new ArrayList<>(this.lastOpenedProjects);
        Collections.reverse(reversed);
        return reversed;
    }

    @Override
    public void registerOpenedProject(OpenedItem item) {
        lastOpenedProjects.add(item);
    }
}
