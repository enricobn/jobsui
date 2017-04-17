package org.jobsui.core;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.job.Job;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by enrico on 4/17/17.
 */
public class BookmarksStoreFSImpl implements BookmarksStore {
    private final File root;

    public static BookmarksStoreFSImpl getUser() {
        File userRootDir =
                new File(System.getProperty("java.util.prefs.userRoot",
                        System.getProperty("user.home")));
        return new BookmarksStoreFSImpl(new File(userRootDir, ".jobsui"));
    }

    public BookmarksStoreFSImpl(File root) {
        this.root = root;
    }

    @Override
    public void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException {
        File jobRoot = new File(new File(root, bookmark.getProjectId()), bookmark.getJobId());
        if (!jobRoot.exists()) {
            jobRoot.mkdirs();
        }

        File file = new File(jobRoot, bookmark.getName() + ".xml");

        try (FileWriter fileWriter = new FileWriter(file)) {
            XStream xstream = new XStream();
            if (job.getClassLoader() != null) {
                xstream.setClassLoader(job.getClassLoader());
            }
            xstream.toXML(bookmark, fileWriter);
        }
    }

    @Override
    public List<Bookmark> getBookmarks(Project project, Job job) {
        File jobRoot = new File(new File(root, project.getId()), job.getId());
        if (!jobRoot.exists()) {
            return Collections.emptyList();
        }

        String[] list = jobRoot.list((dir, name) -> name.endsWith(".xml"));
        if (list == null) {
            return Collections.emptyList();
        }

        List<Bookmark> result = new ArrayList<>();

        XStream xstream = new XStream();
        if (job.getClassLoader() != null) {
            xstream.setClassLoader(job.getClassLoader());
        }

        for (String fileName : list) {
            File file = new File(jobRoot, fileName);
            result.add((Bookmark) xstream.fromXML(file));
        }

        result.sort(Comparator.comparing(Bookmark::getName));

        return result;
    }
}
