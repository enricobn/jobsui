package org.jobsui.core.edit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/14/16.
 */
/**
 * use JobsUIPreferences
 */
@Deprecated
class EditProjectConfiguration {
    private static final Preferences preferences = Preferences.userNodeForPackage(EditProjectConfiguration.class);
    private static final String RECENT_PROJECTS = "recentProjects";
    private static final String COUNT = "count";

    private List<File> recentProjects;

    private EditProjectConfiguration(List<File> recentProjects) {
        this.recentProjects = recentProjects;
    }

    static EditProjectConfiguration load() throws BackingStoreException {
        List<File> projects = new ArrayList<>();
        if (preferences.nodeExists(RECENT_PROJECTS)) {
            Preferences recentProjectsNode = preferences.node(RECENT_PROJECTS);
            int count = recentProjectsNode.getInt(COUNT, 0);
            for (int i = 0; i < count; i++) {
                String project = recentProjectsNode.get(Integer.toString(i), null);
                if (project != null) {
                    projects.add(new File(project));
                }
            }
        }
        return new EditProjectConfiguration(projects);
    }

    static void save(EditProjectConfiguration configuration) throws BackingStoreException {
        if (preferences.nodeExists(RECENT_PROJECTS)) {
            Preferences recentProjectsNode = preferences.node(RECENT_PROJECTS);
            recentProjectsNode.clear();
        }
        Preferences recentProjectsNode = preferences.node(RECENT_PROJECTS);
        recentProjectsNode.putInt(COUNT, configuration.recentProjects.size());

        for (int i = 0; i < configuration.recentProjects.size(); i++) {
            recentProjectsNode.put(Integer.toString(i), configuration.recentProjects.get(i).getAbsolutePath());
        }
        preferences.flush();
    }

    void addRecentProject(File projectFolder) {
        recentProjects.remove(projectFolder);
        recentProjects.add(0, projectFolder);
        recentProjects = recentProjects.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<File> getRecentProjects() {
        return Collections.unmodifiableList(recentProjects);
    }

    /**
     *
     * @return null if no valid project is found in recent projects
     */
    File getFirstRecentValidProject() {
        List<File> validProjects = recentProjects.stream()
                .filter(file -> file.exists() && file.isDirectory())
                .collect(Collectors.toList());
        if (validProjects.isEmpty()) {
            return null;
        }
        return validProjects.get(0);
    }
}
