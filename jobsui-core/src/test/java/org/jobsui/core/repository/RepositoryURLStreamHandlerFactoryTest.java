package org.jobsui.core.repository;

import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by enrico on 5/6/17.
 */
public class RepositoryURLStreamHandlerFactoryTest {

    @Before
    public void setUp() throws Exception {
        URL repositoryUrl = getClass().getResource("/repository");

        RepositoryImpl repository = new RepositoryImpl(repositoryUrl);

        RepositoryURLStreamHandlerFactory.getInstance().add("repo", repository);
    }

    @After
    public void tearDown() throws Exception {
        RepositoryURLStreamHandlerFactory.getInstance().clear();
    }

    @Test
    public void testSimple() throws Exception {
        URL url = new URL(RepositoryURLStreamHandlerFactory.PROTOCOL + ":test/simple/1.0.0");

        ProjectXML projectXML = new ProjectParserImpl().parse(url);

        assertThat(projectXML.getName(), is("Simple project"));
    }

}