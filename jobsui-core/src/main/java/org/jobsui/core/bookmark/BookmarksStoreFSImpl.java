package org.jobsui.core.bookmark;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by enrico on 4/17/17.
 */
public class BookmarksStoreFSImpl implements BookmarksStore {
    private static final Logger LOGGER = Logger.getLogger(BookmarksStoreFSImpl.class.getName());
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
        File jobRoot = getJobRoot(project, job);
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
        File jobRoot = getJobRoot(project, job);
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
            try {
                result.add((Bookmark) xstream.fromXML(file));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading bookmark from " + file, e);
            }
        }

        result.sort(Comparator.comparing(Bookmark::getName));

        return result;
    }

    private File getJobRoot(Project project, Job job) {
        return new File(new File(root, project.getId()), job.getId());
    }

    @Override
    public boolean existsBookmark(Project project, Job job, String name) {
        File jobRoot = getJobRoot(project, job);
        return new File(jobRoot, name + ".xml").exists();
    }

    @Override
    public boolean deleteBookmark(Project project, Job job, String name) {
        File jobRoot = getJobRoot(project, job);
        File file = new File(jobRoot, name + ".xml");
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}
