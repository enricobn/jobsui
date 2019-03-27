package org.jobsui.core.bookmark;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.job.CompatibleJobId;
import org.jobsui.core.job.CompatibleProjectId;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/17/17.
 */
public class BookmarksStoreFSImpl implements BookmarksStore {
    private static final Logger LOGGER = Logger.getLogger(BookmarksStoreFSImpl.class.getName());
    private final File root;

    public static BookmarksStore getUserStore() {
        return new BookmarksStoreFSImpl(
                Paths.get(
                    System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
            ".jobsui",
                    "bookmarks"
                ).toFile());
    }

    BookmarksStoreFSImpl(File root) {
        this.root = root;
    }

    @Override
    public void saveBookmark(Project project, Job job, Bookmark bookmark) throws IOException {
        File jobRoot = getJobRoot(project, job);
        if (!jobRoot.exists()) {
            if (!jobRoot.mkdirs()) {
                throw new IOException();
            }
        }

        File file = new File(jobRoot, bookmark.getName() + ".xml");


        try (FileOutputStream out = new FileOutputStream(file);
                Writer fileWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            XStream xstream = new XStream();
            if (job.getClassLoader() != null) {
                xstream.setClassLoader(job.getClassLoader());
            }
            xstream.toXML(bookmark, fileWriter);
        }
    }

    @Override
    public Map<String, Bookmark> getBookmarks(Project project, Job job) {
        File jobRoot = getJobRoot(project, job);
        if (!jobRoot.exists()) {
            return Collections.emptyMap();
        }

        String[] list = jobRoot.list((dir, name) -> name.endsWith(".xml"));
        if (list == null) {
            return Collections.emptyMap();
        }

        List<Bookmark> result = new ArrayList<>();

        XStream xstream = new XStream();
        if (job.getClassLoader() != null) {
            xstream.setClassLoader(job.getClassLoader());
        }

        for (String fileName : list) {
            File file = new File(jobRoot, fileName);
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
                result.add((Bookmark) xstream.fromXML(reader));
            } catch (Exception e) {
                // TODO show a message in UI
                LOGGER.log(Level.SEVERE, "Error loading bookmark from " + file, e);
            }
        }

        result.sort(Comparator.comparing(it -> it.getName().toLowerCase()));

        return result.stream()
                .collect(Collectors.toMap(
                        Bookmark::getKey,
                        it -> it,
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new));
    }

    private File getJobRoot(Project project, Job job) {
        CompatibleProjectId projectId = project.getId().toCompatibleProjectId();
        CompatibleJobId jobId = job.getCompatibleJobId();
        return root.toPath()
                .resolve(projectId.getGroupId())
                .resolve(projectId.getModuleId())
                .resolve(Integer.toString(projectId.getMajorVersion()))
                .resolve(jobId.getId())
                .resolve(Integer.toString(jobId.getMajorVersion()))
                .toFile();
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
