package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.jobstore.JobStore;

import java.util.Map;

/**
 * Created by enrico on 4/17/17.
 */
public interface BookmarksStore extends JobStore<Bookmark> {

    /**
     * @return a map of Bookmarks by key
     */
    Map<String, Bookmark> getBookmarks(Project project, Job job);

    boolean existsBookmark(Project project, Job job, String name);

}
