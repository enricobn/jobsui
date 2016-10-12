package org.jobsui.core.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class ProjectGroovyBuilder {

    public ProjectGroovy build(ProjectXML projectXML) throws Exception {
        Map<String, JobGroovy<?>> jobs = new HashMap<>();

        for (JobXML jobXML : projectXML.getJobs().values()) {
            jobs.put(jobXML.getKey(), build(projectXML, jobXML));
        }

        ProjectGroovy projectGroovy = new ProjectGroovy(projectXML, jobs);

        for (JobGroovy<?> job : jobs.values()) {
            job.init(projectGroovy);
        }
        return projectGroovy;
    }

    private <T> JobGroovy<T> build(ProjectXML projectXML, JobXML jobXML) throws Exception {
        GroovyShell groovyShell = createGroovyShell(projectXML);
        Map<String, JobParameterDefGroovy<?>> parameterDefsMap = new HashMap<>();

        for (SimpleParameterXML simpleParameterXML : jobXML.getSimpleParameterXMLs()) {
            JobParameterDefGroovy<?> parameterDef = new JobParameterDefGroovySimple<>(
                    projectXML.getProjectFolder(),
                    groovyShell,
                    simpleParameterXML.getKey(),
                    simpleParameterXML.getName(),
                    simpleParameterXML.getCreateComponentScript(),
                    simpleParameterXML.getOnDependenciesChangeScript(),
                    simpleParameterXML.getValidateScript(),
                    simpleParameterXML.isOptional(),
                    simpleParameterXML.isVisible());
            parameterDefsMap.put(parameterDef.getKey(), parameterDef);
        }

        for (ExpressionXML expressionXML : jobXML.getExpressionXMLs()) {
            JobParameterDefGroovy<?> parameterDef = new JobExpressionDefGroovy<>(
                    projectXML.getProjectFolder(),
                    groovyShell,
                    expressionXML.getKey(),
                    expressionXML.getName(),
                    expressionXML.getEvaluateScript());
            parameterDefsMap.put(parameterDef.getKey(), parameterDef);
        }

        for (CallXML callXML : jobXML.getCallXMLs()) {
            JobCallDefGroovy<?> call = new JobCallDefGroovy<>(
                    callXML.getKey(),
                    callXML.getName(),
                    callXML.getProject(),
                    callXML.getJob(),
                    callXML.getMap());
            parameterDefsMap.put(call.getKey(), call);
        }

        addDependencies(jobXML.getSimpleParameterXMLs(), parameterDefsMap);
        addDependencies(jobXML.getExpressionXMLs(), parameterDefsMap);
        addDependencies(jobXML.getCallXMLs(), parameterDefsMap);

        List<JobParameterDefGroovy<?>> sorted = jobXML.getSortedParameters().stream()
                .map(parameterDefsMap::get).collect(Collectors.toList());

        return new JobGroovy<>(groovyShell, jobXML.getKey(), jobXML.getName(), sorted,
                jobXML.getRunScript(), jobXML.getValidateScript(), projectXML.getProjectFolder());
    }

    private void addDependencies(List<? extends ParameterXML> parameterXMLs, Map<String, JobParameterDefGroovy<?>> parameterDefs) {
        for (ParameterXML parameterXML : parameterXMLs) {
            JobParameterDefGroovy<?> jobParameterDefGroovy = parameterDefs.get(parameterXML.getKey());
            for (String depKey : parameterXML.getDependencies()) {
                JobParameterDefGroovy<?> dep = parameterDefs.get(depKey);
                jobParameterDefGroovy.addDependency(dep);
            }
        }
    }

    private static GroovyShell createGroovyShell(ProjectXML projectXML) throws IOException, ParseException {
        GroovyClassLoader cl;

        if (projectXML.getGroovyFiles().isEmpty()) {
            cl = new GroovyClassLoader();
        } else {
            // TODO
            final File groovy = new File(projectXML.getProjectFolder(), "groovy");
            GroovyScriptEngine engine = new GroovyScriptEngine(new URL[]{ groovy.toURI().toURL() });
            cl = engine.getGroovyClassLoader();
        }

        for (String library : projectXML.getLibraries()) {
            String[] split = library.split(":");
            File file = IvyUtils.resolveArtifact(split[0], split[1], split[2]);
            cl.addURL(file.toURI().toURL());
        }

        for (File fileLibrary : projectXML.getFileLibraries()) {
            cl.addURL(fileLibrary.toURI().toURL());
        }

        return new GroovyShell(cl);
    }
}
