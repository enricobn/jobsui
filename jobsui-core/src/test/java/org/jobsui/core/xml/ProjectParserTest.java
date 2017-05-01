package org.jobsui.core.xml;

import org.jobsui.core.groovy.ProjectGroovy;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
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

    @Test
    public void parse_wizard() throws Exception {
        JobParser parser = new JobParserImpl();
        URL url = ProjectParserTest.class.getResource("/simplejob/simpleWithWizard.xml");
        JobXML job = parser.parse("simpleWithWizard", url);

        assertThat(job.getWizardSteps().size(), is(2));

        WizardStep firstStep = job.getWizardSteps().get(0);
        assertThat(firstStep.getName(), is("First"));
        assertThat(firstStep.getDependencies(), is(Collections.singletonList("name")));
        assertThat(firstStep.getValidateScript(), nullValue());

        WizardStep secondStep = job.getWizardSteps().get(1);
        assertThat(secondStep.getName(), is("Second"));
        assertThat(secondStep.getDependencies(), is(Collections.singletonList("dependent")));
        assertThat(secondStep.getValidateScript(), notNullValue());
    }
}