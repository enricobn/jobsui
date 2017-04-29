package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;
import org.jobsui.core.utils.JobsUIUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
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

        File folder = JobsUIUtils.createTempDir("test", "export");

        try {
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

        for (String location : exported.getScriptsLocations()) {
            assertThat(exported.getScriptFilesNames(location), is(original.getScriptFilesNames(location)));
            for (String fileName : exported.getScriptFilesNames(location)) {
                assertThat(exported.getScriptContent(location, fileName), is(original.getScriptContent(location, fileName)));
            }
        }

        for (String job : exported.getJobs()) {
            JobXML exportedJobXML = exported.getJobXML(job);
            JobXML originalJobXML = original.getJobXML(job);

            check(originalJobXML, exportedJobXML);
        }

    }

    private void check(JobXML originalJobXML, JobXML exportedJobXML) {
        List<String> exportedSimpleParametersKeys = getKeys(exportedJobXML.getSimpleParameterXMLs());
        List<String> originalSimpleParametersKeys = getKeys(originalJobXML.getSimpleParameterXMLs());
        assertThat(exportedSimpleParametersKeys, is(originalSimpleParametersKeys));

        for (String parameterKey : exportedSimpleParametersKeys) {
            ParameterXML originalParameter = originalJobXML.getParameter(parameterKey);
            ParameterXML exportedParameter = exportedJobXML.getParameter(parameterKey);
            check(originalParameter, exportedParameter);
        }

        List<String> exportedExpressionsKeys = getKeys(exportedJobXML.getExpressionXMLs());
        List<String> originalExpressionsKeys = getKeys(originalJobXML.getExpressionXMLs());
        assertThat(exportedExpressionsKeys, is(originalExpressionsKeys));

        for (String expressionKey : exportedExpressionsKeys) {
            ExpressionXML originalExpression = originalJobXML.getExpression(expressionKey);
            ExpressionXML exportedExpression = exportedJobXML.getExpression(expressionKey);
            check(originalExpression, exportedExpression);
            checkExpression(originalExpression, exportedExpression);
        }
    }

    private void check(ParameterXML originalParameter, ParameterXML exportedParameter) {
        assertThat(exportedParameter, instanceOf(originalParameter.getClass()));
        assertThat(exportedParameter.getKey(), is(originalParameter.getKey()));
        assertThat(exportedParameter.getName(), is(originalParameter.getName()));
        assertThat(exportedParameter.getDependencies(), is(originalParameter.getDependencies()));
        assertThat(exportedParameter.getOrder(), is(originalParameter.getOrder()));

        if (originalParameter instanceof SimpleParameterXML) {
            SimpleParameterXML originalSimpleParameter = (SimpleParameterXML) originalParameter;
            SimpleParameterXML exportedSimpleParameter = (SimpleParameterXML) exportedParameter;
            checkSimpleParameter(originalSimpleParameter, exportedSimpleParameter);
        }
    }

    private void checkSimpleParameter(SimpleParameterXML originalSimpleParameter, SimpleParameterXML exportedSimpleParameter) {
        assertThat(exportedSimpleParameter.getOnDependenciesChangeScript(), is(originalSimpleParameter.getOnDependenciesChangeScript()));
        assertThat(exportedSimpleParameter.getOnInitScript(), is(originalSimpleParameter.getOnInitScript()));
        assertThat(exportedSimpleParameter.getValidateScript(), is(originalSimpleParameter.getValidateScript()));
        assertThat(exportedSimpleParameter.getComponent(), is(originalSimpleParameter.getComponent()));
    }

    private void checkExpression(ExpressionXML originalExpression, ExpressionXML exportedExpression) {
        assertThat(exportedExpression.getEvaluateScript(), is(originalExpression.getEvaluateScript()));
    }

    private static List<String> getKeys(Collection<? extends ParameterXML> params) {
        return params.stream()
                .map(ParameterXML::getKey)
                .collect(Collectors.toList());
    }
}