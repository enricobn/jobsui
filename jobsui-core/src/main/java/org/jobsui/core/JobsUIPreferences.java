package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.ui.javafx.JobsUITheme;

import java.net.URL;
import java.util.List;

/**
 * Created by enrico on 3/29/17.
 */
public interface JobsUIPreferences {

    List<OpenedItem> getLastOpenedItems();

    void registerOpenedProject(URL url, String name) throws Exception;

    JobsUITheme getTheme();

    void setTheme(JobsUITheme theme);

    List<Bookmark> getBookmarks(Project project, Job job);

    void saveBookmark(Bookmark bookmark);

}
