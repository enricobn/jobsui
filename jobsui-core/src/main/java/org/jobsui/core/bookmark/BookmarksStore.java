package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by enrico on 4/17/17.
 */
public interface BookmarksStore {

    void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException;

    List<Bookmark> getBookmarks(Project project, Job job);

    boolean existsBookmark(Project project, Job job, String name);

    boolean deleteBookmark(Project project, Job job, String name);

    Optional<Bookmark> getBookmark(Project project, Job job, String name);
}
