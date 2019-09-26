package org.jobsui.core.xml;

import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponentRegistry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 5/4/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectParserTest {
    private static ProjectXML projectXML;
    @Mock
    private BookmarksStore bookmarkStore;
    @Mock
    private UI ui;
    @Mock
    private UIComponentRegistry uiComponentRegistry;

    @BeforeClass
    public static void setUpStatic() throws Exception {
        ProjectParser parser = new ProjectParserImpl();
        projectXML = parser.parse(ProjectParserTest.class.getResource("/simplejob"));
    }

    @AfterClass
    public static void tearDownStatic() {
        projectXML = null;
    }

    @Before
    public void setUp() {
        when(uiComponentRegistry.getComponentType(anyString())).thenReturn(Optional.empty());
    }

    @Test
    public void test_parse() throws Exception {
        Project project = new ProjectGroovyBuilder().build(projectXML, bookmarkStore, ui);
        Job<Object> job = project.getJob("simple");

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
        JobXML job = parser.parse("simple", url, uiComponentRegistry);
        ExpressionXML inv = job.getExpression("inv");
        assertThat(inv.isVisible(), is(false));
    }

    @Test
    public void parse_page() throws Exception {
        JobParser parser = new JobParserImpl();
        URL url = ProjectParserTest.class.getResource("/simplejob/simpleWithWizard.xml");
        JobXML job = parser.parse("simpleWithWizard", url, uiComponentRegistry);

        assertThat(job.getJobPages().size(), is(2));

        JobPage firstStep = job.getJobPages().get(0);
        assertThat(firstStep.getName(), is("First"));
        assertThat(firstStep.getDependencies(), is(Collections.singleton("name")));
        assertThat(firstStep.getValidateScript(), nullValue());

        JobPage secondStep = job.getJobPages().get(1);
        assertThat(secondStep.getName(), is("Second"));
        assertThat(secondStep.getDependencies(), is(Collections.singleton("dependent")));
        assertThat(secondStep.getValidateScript(), notNullValue());
    }

}