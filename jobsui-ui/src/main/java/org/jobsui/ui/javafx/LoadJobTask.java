package org.jobsui.ui.javafx;

import javafx.concurrent.Task;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.utils.Tuple2;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by enrico on 3/30/17.
 */
class LoadJobTask extends Task<Tuple2<Project,Job<Serializable>>> {
    private final URL url;
    private final String jobId;
    private final BookmarksStore bookmarkStore;

    LoadJobTask(URL url, String jobId, BookmarksStore bookmarkStore) {
        this.url = url;
        this.jobId = jobId;
        this.bookmarkStore = bookmarkStore;
    }

    @Override
    protected Tuple2<Project,Job<Serializable>> call() throws Exception {
        ProjectParser projectParser = new ProjectParserImpl();
        ProjectXML projectXML;
        try {
            projectXML = projectParser.parse(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Project project;
        try {
            project = new ProjectGroovyBuilder().build(projectXML, bookmarkStore);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final Job<Serializable> job = project.getJob(jobId);

        if (job == null) {
            throw new RuntimeException("Cannot find job with id \"" + jobId + "\" in folder " +
                    url + " .");
        }
        return new Tuple2<>(project, job);
    }
}
