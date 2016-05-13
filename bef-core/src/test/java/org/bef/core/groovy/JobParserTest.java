package org.bef.core.groovy;

import org.bef.core.Job;
import org.bef.core.JobParameterDef;
import org.bef.core.Project;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<?> job = project.getJob("simple");

        assertThat(job, is(notNullValue()));
        assertThat(job.getParameterDefs().size(), is(3));
        assertThat(job.getParameter("name").getName(), is("Name"));
        assertThat(job.getParameter("surname").getName(), is("Surname"));
        assertThat(job.getParameter("inv").getName(), is("Inv"));

        assertEquals(job.getParameter("inv").getDependencies().get(0), job.getParameter("name"));
        assertEquals(job.getParameter("inv").getDependencies().get(1), job.getParameter("surname"));
    }

    @Test
    public void test_that_inv_parameter_in_Simplejob_is_invisible() throws Exception {
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<?> job = project.getJob("simple");
        JobParameterDef inv = job.getParameter("inv");
        assertThat(inv.isVisible(), equalTo(false));
    }
}