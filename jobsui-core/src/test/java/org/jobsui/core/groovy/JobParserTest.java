package org.jobsui.core.groovy;

import org.jobsui.core.xml.JobXML;
import org.jobsui.core.xml.ParameterXML;
import org.jobsui.core.xml.ProjectXML;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserTest {
    private JobParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new JobParser();
    }

    @Test
    public void test_parse() throws Exception {
        ProjectXML project = parser.loadProject(new File("src/test/resources/simplejob"));
        final JobXML job = project.getJobs().get("simple");

        assertThat(job, is(notNullValue()));
        assertThat(job.getSortedParameters().size(), is(3));
        assertThat(job.getParameter("name").getName(), is("Name"));
        assertThat(job.getParameter("surname").getName(), is("Surname"));
        assertThat(job.getParameter("inv").getName(), is("Inv"));

        assertEquals(job.getParameter("inv").getDependencies().get(0), "name");
        assertEquals(job.getParameter("inv").getDependencies().get(1), "surname");

        assertFalse(job.getParameter("inv").isOptional());
    }

    @Test
    public void test_that_inv_parameter_in_Simplejob_is_invisible() throws Exception {
        ProjectXML project = parser.loadProject(new File("src/test/resources/simplejob"));
        final JobXML job = project.getJobs().get("simple");
        ParameterXML inv = job.getParameter("inv");
        assertThat(inv.isVisible(), equalTo(false));
    }
}