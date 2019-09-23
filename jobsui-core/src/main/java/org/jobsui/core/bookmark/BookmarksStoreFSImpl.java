package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.jobstore.JobStoreFSImpl;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/17/17.
 */
public class BookmarksStoreFSImpl extends JobStoreFSImpl<Bookmark> implements BookmarksStore {

    public static BookmarksStore getUserStore() {
        return new BookmarksStoreFSImpl(
                Paths.get(
                    System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
            ".jobsui",
                    "bookmarks"
                ).toFile());
    }

    protected BookmarksStoreFSImpl(File root) {
        super(root);
    }

    @Override
    public Map<String, Bookmark> getBookmarks(Project project, Job job) {
        List<Bookmark> result = get(project, job);

        result.sort(Comparator.comparing(it -> it.getName().toLowerCase()));

        return result.stream()
                .collect(Collectors.toMap(
                        Bookmark::getKey,
                        it -> it,
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new));
    }

    @Override
    public boolean existsBookmark(Project project, Job job, String name) {
        List<Bookmark> bookmarks = get(project, job);
        return bookmarks.stream().anyMatch(it -> it.getName().equals(name));
    }

    @Override
    protected String getFileNameWithoutExtension(Bookmark value) {
        return value.getName();
    }

}
