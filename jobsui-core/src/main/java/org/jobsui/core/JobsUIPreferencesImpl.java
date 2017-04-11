package org.jobsui.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIPreferencesImpl implements JobsUIPreferences {
    private final Preferences lastOpenedProjectsNode;
    private final List<OpenedItem> lastOpenedProjects = new ArrayList<>();

    private JobsUIPreferencesImpl() {
        Preferences prefs = Preferences.userNodeForPackage(JobsUIPreferencesImpl.class);
        lastOpenedProjectsNode = prefs.node("lastOpenedProjects");
    }

    private void load() {
        int length = lastOpenedProjectsNode.getInt("size", 0);
        for (int i = 0; i < length; i++) {
            String openedProjectPath = lastOpenedProjectsNode.get("path_" + i, null);
            String openedProjectName = lastOpenedProjectsNode.get("name_" + i, null);
            if (openedProjectPath != null) {
                lastOpenedProjects.add(new OpenedItem(openedProjectPath, openedProjectName));
            }
        }
    }

    private void save() throws Exception {
        lastOpenedProjectsNode.clear();

        lastOpenedProjectsNode.putInt("size", lastOpenedProjects.size());

        for (int i = 0; i < lastOpenedProjects.size(); i++) {
            OpenedItem openedItem = lastOpenedProjects.get(i);
            lastOpenedProjectsNode.put("path_" + i, openedItem.url);
            lastOpenedProjectsNode.put("name_" + i, openedItem.name);
        }

        lastOpenedProjectsNode.flush();
    }

    public static JobsUIPreferences get() {
        JobsUIPreferencesImpl preferences = new JobsUIPreferencesImpl();
        preferences.load();
        return preferences;
    }

    @Override
    public List<OpenedItem> getLastOpenedItems() {
        List<OpenedItem> reversed = new ArrayList<>(this.lastOpenedProjects);
        Collections.reverse(reversed);
        return reversed;
    }

    @Override
    public void registerOpenedProject(URL url, String name) throws Exception {
        OpenedItem openedItem = new OpenedItem(url.toString(), name);
        lastOpenedProjects.removeIf(item -> item.url.equals(openedItem.url));
        lastOpenedProjects.add(openedItem);
        save();
    }
}
