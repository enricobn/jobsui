package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by enrico on 4/8/17.
 */
public class ProjectXMLExporterTest {

    @Test
    public void simplejob() throws Exception {
        export("/simplejob");
    }

    @Test
    public void external() throws Exception {
        export("/external");
    }

    private void export(String resource) throws Exception {
        URL fileResource = getClass().getResource(resource);
        File file = new File(fileResource.getPath());

        ProjectFSXML projectFSXML = new ProjectParserImpl().parse(file);

        ProjectXMLExporter projectXMLExporter = new ProjectXMLExporter();

        List<JobXML> jobs = projectFSXML.getJobs().stream()
                .map(job -> {
                    try {
                        return JobParserImpl.parse(projectFSXML, job);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        File folder = File.createTempFile("test", "export");
        folder.delete();

        try {
            folder.mkdir();
            projectXMLExporter.export(projectFSXML, folder, jobs);
        } finally {
            FileUtils.deleteDirectory(folder);
        }
    }
}