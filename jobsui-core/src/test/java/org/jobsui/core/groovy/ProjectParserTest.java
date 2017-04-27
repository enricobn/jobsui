package org.jobsui.core.groovy;

import org.jobsui.core.job.Job;
import org.jobsui.core.xml.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by enrico on 5/4/16.
 */
public class ProjectParserTest {
    private static ProjectXML projectXML;

    @BeforeClass
    public static void setUpStatic() throws Exception {
        ProjectParser parser = new ProjectParserImpl();
        projectXML = parser.parse(ProjectParserTest.class.getResource("/simplejob"));
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
        JobParser parser = new JobParserImpl();
        URL url = ProjectParserTest.class.getResource("/simplejob/simple.xml");
        JobXML job = parser.parse("simple", url);
        ExpressionXML inv = job.getExpression("inv");
        assertThat(inv.isVisible(), is(false));
    }
}