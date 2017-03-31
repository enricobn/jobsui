package org.jobsui.core.groovy;

import org.jobsui.core.job.Job;
import org.jobsui.core.xml.ExpressionXML;
import org.jobsui.core.xml.JobXML;
import org.jobsui.core.xml.ParameterXML;
import org.jobsui.core.xml.ProjectXML;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserTest {
    private static ProjectXML projectXML;

    @BeforeClass
    public static void setUpStatic() throws Exception {
        JobParser parser = JobParser.getParser("src/test/resources/simplejob");
        projectXML = parser.parse();
    }

    @AfterClass
    public static void tearDownStatic() throws Exception {
        projectXML = null;
    }

    @Test
    public void test_parse() throws Exception {
        ProjectGroovy projectGroovy = new ProjectGroovyBuilder().build(projectXML);
        Job<Object> job = projectGroovy.getJob("simple");

        assertThat(job, is(notNullValue()));
        assertThat(job.getParameter("name").getName(), is("Name"));
        assertThat(job.getParameter("surname").getName(), is("Surname"));
        assertThat(job.getExpression("inv").getName(), is("Inv"));

        assertEquals(job.getExpression("inv").getDependencies().get(0), "name");
        assertEquals(job.getExpression("inv").getDependencies().get(1), "surname");
    }

    @Test
    public void assert_that_is_visible_for_inv_expression_in_Simplejob_is_false() throws Exception {
        final JobXML job = projectXML.getJobs().get("simple");
        ExpressionXML inv = job.getExpression("inv");
        assertThat(inv.isVisible(), is(false));
    }
}