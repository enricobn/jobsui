package org.jobsui.core;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.JobsUITheme;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIPreferencesImpl implements JobsUIPreferences {
    private final Preferences lastOpenedProjectsNode;
    private final Preferences othersNode;
    private final BookmarksStore bookmarksStore;
    private final List<OpenedItem> lastOpenedProjects = new ArrayList<>();
    private JobsUITheme theme;

    private JobsUIPreferencesImpl(Preferences preferences, BookmarksStore bookmarksStore) {
        lastOpenedProjectsNode = preferences.node("lastOpenedProjects");
        othersNode = preferences.node("others");
        this.bookmarksStore = bookmarksStore;
    }

    public static JobsUIPreferencesImpl get(Preferences preferences, BookmarksStore bookmarkStore) {
        JobsUIPreferencesImpl jobsUIPreferences = new JobsUIPreferencesImpl(preferences, bookmarkStore);
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
        // I cannot cache bookmarks since it depends on job's classloader
        return Collections.unmodifiableList(bookmarksStore.getBookmarks(project, job));
    }

    @Override
    public void saveBookmark(Project project, Job job, Bookmark bookmark) {
        // I cannot cache bookmarks since it depends on job's classloader
        try {
            bookmarksStore.saveBookmark(project, job, bookmark);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Bookmark> bookmarks = new ArrayList<>(getBookmarks(project, job));
        bookmarks.sort(Comparator.comparing(Bookmark::getName));
    }

    @Override
    public boolean existsBookmark(Project project, Job job, String name) {
        return bookmarksStore.existsBookmark(project, job, name);
    }

    @Override
    public boolean deleteBookmark(Project project, Job job, String name) {
        return bookmarksStore.deleteBookmark(project, job, name);
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
