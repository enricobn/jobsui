package org.jobsui.core.xml;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class XMLJobsUIVersionParserTest {

    @Test
    public void given_a_job_file_without_a_jobsUIVersion_when_get_the_jobsUIVersion_then_the_version_should_be_000() throws Exception {
        URL url = ProjectParserTest.class.getResource("/simplejob/simple.xml");
        String version = XMLJobsUIVersionParser.getInJob(url);

        assertEquals("0.0.0", version);
    }

    @Test
    public void given_a_job_file_with_a_jobsUIVersion_when_get_the_jobsUIVersion_then_the_version_should_be_that() throws Exception {
        URL url = ProjectParserTest.class.getResource("/simplejob/simpleWithJobsUIVersion100.xml");
        String version = XMLJobsUIVersionParser.getInJob(url);

        assertEquals("1.0.0", version);
    }

    @Test
    public void given_a_project_file_without_a_jobsUIVersion_when_get_the_jobsUIVersion_then_the_version_should_be_000() throws Exception {
        URL url = ProjectParserTest.class.getResource("/simplejob/project.xml");
        String version = XMLJobsUIVersionParser.getInProject(url);

        assertEquals("0.0.0", version);
    }
}