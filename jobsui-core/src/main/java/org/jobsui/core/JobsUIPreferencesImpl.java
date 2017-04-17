package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.ui.javafx.JobsUITheme;

import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIPreferencesImpl implements JobsUIPreferences {
    private final Preferences lastOpenedProjectsNode;
    private final Preferences othersNode;
    private final List<OpenedItem> lastOpenedProjects = new ArrayList<>();
    // projectId/jobId/bookmarks
    private final Map<String,Map<String,List<Bookmark>>> bookmarks = new HashMap<>();
    private JobsUITheme theme;

    private JobsUIPreferencesImpl(Preferences preferences) {
        lastOpenedProjectsNode = preferences.node("lastOpenedProjects");
        othersNode = preferences.node("others");
    }

    public static JobsUIPreferencesImpl get(Preferences preferences) {
        JobsUIPreferencesImpl jobsUIPreferences = new JobsUIPreferencesImpl(preferences);
        jobsUIPreferences.load();
        return jobsUIPreferences;
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
        if (theme != this.theme) {
            this.theme = theme;
            save();
        }
    }

    @Override
    public List<Bookmark> getBookmarks(Project project, Job job) {
        Map<String, List<Bookmark>> projectBookmarks = bookmarks.get(project.getId());
        if (projectBookmarks == null) {
            return Collections.emptyList();
        }
        List<Bookmark> jobBookmarks = projectBookmarks.get(job.getId());
        if (jobBookmarks == null) {
            return Collections.emptyList();
        }
        return jobBookmarks;
    }

    @Override
    public void saveBookmark(Bookmark bookmark) {
        Map<String, List<Bookmark>> projectBookmarks = bookmarks.computeIfAbsent(bookmark.getProjectId(), key -> new HashMap<>());
        List<Bookmark> bookmarks = projectBookmarks.computeIfAbsent(bookmark.getJobId(), key -> new ArrayList<>());
        boolean found = false;
        for (int i = 0; !found && i < bookmarks.size(); i++) {
            Bookmark foundBookmark = bookmarks.get(i);
            if (foundBookmark.getName().equals(bookmark.getName())) {
                bookmarks.set(i, bookmark);
                found = true;
            }
        }

        if (!found) {
            bookmarks.add(bookmark);
        }
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
        othersNode.flush();
    }

}
