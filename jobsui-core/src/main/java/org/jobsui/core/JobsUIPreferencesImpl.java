package org.jobsui.core;

import org.jobsui.core.ui.javafx.JobsUITheme;

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
    private final Preferences othersNode;
    private final List<OpenedItem> lastOpenedProjects = new ArrayList<>();
    private JobsUITheme theme;

    private JobsUIPreferencesImpl() {
        Preferences prefs = Preferences.userNodeForPackage(JobsUIPreferencesImpl.class);
        lastOpenedProjectsNode = prefs.node("lastOpenedProjects");
        othersNode = prefs.node("others");
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

    @Override
    public JobsUITheme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(JobsUITheme theme) {
        this.theme = theme;
        save();
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
        String theme = othersNode.get("theme", JobsUITheme.Dark.name());
        this.theme = JobsUITheme.valueOf(theme);
    }

    private void save() {
        try {
            saveTH();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTH() throws Exception {
        lastOpenedProjectsNode.clear();

        lastOpenedProjectsNode.putInt("size", lastOpenedProjects.size());

        for (int i = 0; i < lastOpenedProjects.size(); i++) {
            OpenedItem openedItem = lastOpenedProjects.get(i);
            lastOpenedProjectsNode.put("path_" + i, openedItem.url);
            lastOpenedProjectsNode.put("name_" + i, openedItem.name);
        }

        lastOpenedProjectsNode.flush();

        othersNode.clear();
        othersNode.put("theme", theme.name());
    }

}
