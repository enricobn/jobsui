package org.jobsui.core;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.history.RunHistory;
import org.jobsui.core.history.RunHistoryStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.JobsUITheme;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIPreferencesImpl implements JobsUIPreferences {
    private static final String EDIT_DIVIDER_POSITION = "dividerPosition";
    private static final String RUN_DIVIDER_POSITION = "dividerPosition";
    static final String THEME = "theme";
    static final String SIZE = "size";
    private static final String EDIT_WIDTH = "editWidth";
    private static final String EDIT_HEIGHT = "editHeight";
    private static final String RUN_WIDTH = "runWidth";
    private static final String RUN_HEIGHT = "runHeight";
    static final String OTHERS_NODE = "others";
    static final String EDIT_NODE = "edit";
    static final String RUN_NODE = "run";
    static final String LAST_OPENED_PROJECTS_NODE = "lastOpenedProjects";
    static final String OPENED_PROJECT_PATH_PREFIX = "path_";
    static final String OPENED_PROJECT_NAME_PREFIX = "name_";
    static final String PROJECTS_HOME = "projectsHome";
    private final transient Preferences lastOpenedProjectsNode;
    private final transient Preferences othersNode;
    private final transient Preferences editNode;
    private final transient Preferences runNode;
    private final BookmarksStore bookmarksStore;
    private final RunHistoryStore runHistoryStore;
    private final transient List<OpenedItem> lastOpenedProjects = new ArrayList<>();
    private JobsUITheme theme;
    private double editDividerPosition;
    private double editWidth;
    private double editHeight;
    private double runWidth;
    private double runHeight;
    private double runDividerPosition;
    private File projectsHome;

    private JobsUIPreferencesImpl(Preferences preferences, BookmarksStore bookmarksStore, RunHistoryStore runHistoryStore) {
        lastOpenedProjectsNode = preferences.node(LAST_OPENED_PROJECTS_NODE);
        othersNode = preferences.node(OTHERS_NODE);
        editNode = preferences.node(EDIT_NODE);
        runNode = preferences.node(RUN_NODE);
        this.bookmarksStore = bookmarksStore;
        this.runHistoryStore = runHistoryStore;
    }

    public static JobsUIPreferencesImpl get(Preferences preferences, BookmarksStore bookmarkStore,
                                            RunHistoryStore runHistoryStore) {
        JobsUIPreferencesImpl jobsUIPreferences = new JobsUIPreferencesImpl(preferences, bookmarkStore, runHistoryStore);
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
    public void registerOpenedProject(URL url, String name) {
        OpenedItem openedItem = new OpenedItem(url.toString(), name);
        lastOpenedProjects.removeIf(item -> item.getUrl().equals(openedItem.getUrl()));
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
        // I cannot cache bookmarks since they depend on job's classloader
        ArrayList<Bookmark> bookmarks = new ArrayList<>(bookmarksStore.getBookmarks(project, job).values());
        bookmarks.sort(Comparator.comparing(Bookmark::getName));
        return bookmarks;
    }

    @Override
    public void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException {
        // I cannot cache bookmarks since they depend on job's classloader
        bookmarksStore.save(project, job, bookmark);
    }

    @Override
    public boolean existsBookmark(Project project, Job job, String name) {
        return bookmarksStore.existsBookmark(project, job, name);
    }

    @Override
    public boolean deleteBookmark(Project project, Job job, Bookmark bookmark) {
        return bookmarksStore.delete(project, job, bookmark);
    }

    @Override
    public void setEditDividerPosition(double position) {
        this.editDividerPosition = position;
        save();
    }

    @Override
    public double getEditDividerPosition() {
        return editDividerPosition;
    }

    @Override
    public void setEditWidth(double width) {
        this.editWidth = width;
        save();
    }

    @Override
    public void setEditHeight(double height) {
        this.editHeight = height;
        save();
    }

    @Override
    public double getEditWidth() {
        return editWidth;
    }

    @Override
    public double getEditHeight() {
        return editHeight;
    }

    @Override
    public double getRunWidth() {
        return runWidth;
    }

    @Override
    public void setRunWidth(double runWidth) {
        this.runWidth = runWidth;
        save();
    }

    @Override
    public double getRunHeight() {
        return runHeight;
    }

    @Override
    public void setRunHeight(double runHeight) {
        this.runHeight = runHeight;
        save();
    }

    @Override
    public void setRunDividerPosition(double position) {
        this.runDividerPosition = position;
        save();
    }

    @Override
    public double getRunDividerPosition() {
        return runDividerPosition;
    }

    @Override
    public BookmarksStore getBookmarksStore() {
        return bookmarksStore;
    }

    @Override
    public void removeLastOpenedItem(OpenedItem openedItem) {
        lastOpenedProjects.removeIf(item -> item.getUrl().equals(openedItem.getUrl()));
        save();
    }

    @Override
    public File getProjectsHome() {
        return projectsHome;
    }

    @Override
    public void setProjectsHome(File projectsHome) {
        this.projectsHome = projectsHome;
        save();
    }

    @Override
    public Optional<RunHistory> getLastRun(Project project, Job job) {
        return runHistoryStore.getLast(project, job);
    }

    @Override
    public void saveLastRun(Project project, Job job, RunHistory runHistory) throws IOException {
        runHistoryStore.save(project, job, runHistory);
    }

    private void load() {
        int length = lastOpenedProjectsNode.getInt(SIZE, 0);
        for (int i = 0; i < length; i++) {
            String openedProjectPath = lastOpenedProjectsNode.get(OPENED_PROJECT_PATH_PREFIX + i, null);
            String openedProjectName = lastOpenedProjectsNode.get(OPENED_PROJECT_NAME_PREFIX + i, null);
            if (openedProjectPath != null) {
                lastOpenedProjects.add(new OpenedItem(openedProjectPath, openedProjectName));
            }
        }
        String theme = othersNode.get(THEME, JobsUITheme.Material.name());
        this.theme = JobsUITheme.valueOf(theme);
        this.editDividerPosition = editNode.getDouble(EDIT_DIVIDER_POSITION, 0.4);
        this.editWidth = editNode.getDouble(EDIT_WIDTH, 800);
        this.editHeight = editNode.getDouble(EDIT_HEIGHT, 800);
        this.runWidth = runNode.getDouble(RUN_WIDTH, 600);
        this.runHeight = runNode.getDouble(RUN_HEIGHT, 600);
        this.runDividerPosition = runNode.getDouble(RUN_DIVIDER_POSITION, 0.4);
        String projectsHome = Paths.get(
                System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
                "jobsui")
                .toFile()
                .getAbsolutePath();
        this.projectsHome = new File(runNode.get(PROJECTS_HOME, projectsHome));
    }

    private void save() {
        try {
            saveTH();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTH() throws BackingStoreException {
        lastOpenedProjectsNode.clear();

        lastOpenedProjectsNode.putInt(SIZE, lastOpenedProjects.size());

        for (int i = 0; i < lastOpenedProjects.size(); i++) {
            OpenedItem openedItem = lastOpenedProjects.get(i);
            lastOpenedProjectsNode.put("path_" + i, openedItem.getUrl());
            lastOpenedProjectsNode.put("name_" + i, openedItem.getName());
        }

        lastOpenedProjectsNode.flush();

        othersNode.clear();
        othersNode.put(THEME, theme.name());
        othersNode.flush();

        editNode.clear();
        editNode.putDouble(EDIT_DIVIDER_POSITION, editDividerPosition);
        editNode.putDouble(EDIT_WIDTH, editWidth);
        editNode.putDouble(EDIT_HEIGHT, editHeight);
        editNode.flush();

        runNode.clear();
        runNode.putDouble(RUN_WIDTH, runWidth);
        runNode.putDouble(RUN_HEIGHT, runHeight);
        runNode.putDouble(RUN_DIVIDER_POSITION, runDividerPosition);
        runNode.put(PROJECTS_HOME, projectsHome.getAbsolutePath());
        runNode.flush();
    }

}
