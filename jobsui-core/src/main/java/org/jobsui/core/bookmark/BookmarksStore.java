package org.jobsui.core.bookmark;

import org.jobsui.core.job.Project;
import org.jobsui.core.job.Job;

import java.io.IOException;
import java.util.List;

/**
 * Created by enrico on 4/17/17.
 */
public interface BookmarksStore {

    void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException;

    List<Bookmark> getBookmarks(Project project, Job job);

}
