package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.IOException;
import java.util.Map;

/**
 * Created by enrico on 4/17/17.
 */
public interface BookmarksStore {

    void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException;

    Map<String, Bookmark> getBookmarks(Project project, Job job);

    boolean existsBookmark(Project project, Job job, String name);

    boolean deleteBookmark(Project project, Job job, String name);
}
