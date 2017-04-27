package org.jobsui.core;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.JobsUITheme;

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

    void saveBookmark(Project project, Job job, Bookmark bookmark);

    boolean existsBookmark(Project project, Job job, String name);

    boolean deleteBookmark(Project project, Job job, String name);

    void setEditDividerPosition(double position);

    double getEditDividerPosition();

    void setEditWidth(double width);

    void setEditHeight(double height);

    double getEditWidth();

    double getEditHeight();

    double getRunWidth();

    void setRunWidth(double runWidth);

    double getRunHeight();

    void setRunHeight(double runHeight);
}
