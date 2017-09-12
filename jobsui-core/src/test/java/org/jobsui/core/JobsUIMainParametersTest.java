package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectBuilder;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectXML;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobsUIMainParametersTest {

    @Mock
    private ProjectParser projectParser;
    @Mock
    private ProjectBuilder projectBuilder;
    @Mock
    private ProjectFSXML projectFSXML;
    @Mock
    private Project project;
    @Mock
    private Job job;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private Path projectPath;
    @Mock
    private File projectFile;
    private URI projectURI = new URI("file://project");

    public JobsUIMainParametersTest() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        when(projectParser.parse(any(File.class))).thenReturn(projectFSXML);
        when(projectBuilder.build(any(ProjectXML.class))).thenReturn(project);
        when(project.getJob("job")).thenReturn(job);
        when(fileSystem.getPath(anyString())).thenReturn(projectPath);
        when(projectPath.toFile()).thenReturn(projectFile);
        when(projectFile.isDirectory()).thenReturn(true);
        when(projectFile.exists()).thenReturn(true);
        when(projectPath.toUri()).thenReturn(projectURI);
    }

    @Test
    public void parseRun() throws Exception {
        Consumer<JobsUIMainParameters> onSuccess = params -> {
            assertTrue(params.getAction() == StartAction.Run);
            assertTrue(params.getProject() == project);
            assertTrue(params.getJob() == job);
        };

        Consumer<List<String>> onFailure = error -> fail(error.toString());

        assertTrue(JobsUIMainParameters.parse(new String[]{"-run", "projectFSXML", "job"},
                projectParser, projectBuilder, fileSystem, onSuccess, onFailure));
    }

    @Test
    public void parseEdit() throws Exception {
        Consumer<JobsUIMainParameters> onSuccess = params -> {
            assertTrue(params.getAction() == StartAction.Edit);
            assertTrue(params.getProjectFSXML() == projectFSXML);
        };

        Consumer<List<String>> onFailure = error -> fail(error.toString());

        assertTrue(JobsUIMainParameters.parse(new String[]{"-edit", "projectFSXML"},
                projectParser, projectBuilder, fileSystem, onSuccess, onFailure));
    }
}