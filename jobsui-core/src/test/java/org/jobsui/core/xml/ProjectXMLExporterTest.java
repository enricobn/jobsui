package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by enrico on 4/8/17.
 */
public class ProjectXMLExporterTest {

    @Test
    public void simplejob() throws Exception {
        test("/simplejob");
    }

    @Test
    public void external() throws Exception {
        test("/external");
    }

    private void test(String resource) throws Exception {
        URL fileResource = getClass().getResource(resource);
        File file = new File(fileResource.getPath());

        ProjectFSXML projectFSXML = new ProjectParserImpl().parse(file);

        ProjectXMLExporter projectXMLExporter = new ProjectXMLExporter();

        File folder = File.createTempFile("test", "export");
        assertThat(folder.delete(), is(true));

        try {
            assertThat(folder.mkdir(), is(true));
            projectXMLExporter.export(projectFSXML, folder);

            ProjectFSXML exportedProjectFSXML = new ProjectParserImpl().parse(folder);

            check(projectFSXML, exportedProjectFSXML);

        } finally {
            FileUtils.deleteDirectory(folder);
        }
    }

    private void check(ProjectFSXML original, ProjectFSXML exported) throws IOException {
        assertThat(exported.getId(), is(original.getId()));
        assertThat(exported.getName(), is(original.getName()));
        assertThat(exported.getJobs(), is(original.getJobs()));
        assertThat(exported.getImports(), is(original.getImports()));
        assertThat(exported.getLibraries(), is(original.getLibraries()));
        assertThat(exported.getScriptsLocations(), is(original.getScriptsLocations()));

        Charset utf8 = Charset.forName("UTF-8");

        for (String location : exported.getScriptsLocations()) {
            File exportedRoot = new File(exported.getFolder(), location);
            Collection<File> scriptFiles = original.getScriptFiles(location);
            for (File scriptFile : scriptFiles) {
                File exportedScriptFile = new File(exportedRoot, scriptFile.getName());
                String originalFileContent = FileUtils.readFileToString(scriptFile, utf8);
                String exportedFileContent = FileUtils.readFileToString(exportedScriptFile, utf8);
                assertThat(scriptFile.getName(), originalFileContent, is(exportedFileContent));
            }
        }

    }
}