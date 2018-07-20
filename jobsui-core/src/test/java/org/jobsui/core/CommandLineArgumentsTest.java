package org.jobsui.core;

import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectBuilder;
import org.jobsui.core.ui.UI;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectXML;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandLineArgumentsTest {
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
    @Mock
    private BookmarksStore bookmarksStore;
    @Mock
    private UI ui;

    private URI projectURI = new URI("file://project");

    public CommandLineArgumentsTest() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        when(projectParser.parse(any(File.class))).thenReturn(projectFSXML);
        when(projectBuilder.build(any(ProjectXML.class), eq(bookmarksStore), eq(ui))).thenReturn(project);
        when(project.getJob("job")).thenReturn(job);
        when(fileSystem.getPath(anyString())).thenReturn(projectPath);
        when(projectPath.toFile()).thenReturn(projectFile);
        when(projectFile.isDirectory()).thenReturn(true);
        when(projectFile.exists()).thenReturn(true);
        when(projectPath.toUri()).thenReturn(projectURI);
    }

    @Test
    public void parseRun() throws Exception {
        Consumer<CommandLineArguments> onSuccess = arguments -> {
            assertSame(arguments.getAction(), StartAction.Run);
            assertEquals("job", arguments.getJob());
        };

        Consumer<List<String>> onFailure = error -> fail(error.toString());

        assertTrue(CommandLineArguments.parse(new String[]{"-run", "projectFSXML", "job"},
                projectParser, projectBuilder, fileSystem, onSuccess, onFailure, bookmarksStore));
    }

    @Test
    public void parseEdit() throws Exception {
        Consumer<CommandLineArguments> onSuccess = arguments -> {
            assertTrue(arguments.getAction() == StartAction.Edit);
            assertTrue(arguments.getProjectFSXML() == projectFSXML);
        };

        Consumer<List<String>> onFailure = error -> fail(error.toString());

        assertTrue(CommandLineArguments.parse(new String[]{"-edit", "projectFSXML"},
                projectParser, projectBuilder, fileSystem, onSuccess, onFailure, bookmarksStore));
    }
}