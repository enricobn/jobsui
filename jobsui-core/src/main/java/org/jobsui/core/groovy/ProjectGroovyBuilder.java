package org.jobsui.core.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.Project;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class ProjectGroovyBuilder {

    public ProjectGroovy build(ProjectXML projectXML) throws Exception {
        Map<String, JobGroovy<Serializable>> jobs = new HashMap<>();

        for (JobXML jobXML : projectXML.getJobs().values()) {
            jobs.put(jobXML.getId(), build(projectXML, jobXML));
        }

        Map<String, Project> projects = new HashMap<>();

        JobParser jobParser = new JobParser();

        ProjectGroovyBuilder projectGroovyBuilder = new ProjectGroovyBuilder();
        projectXML.getImports().entrySet().forEach(entry -> {
            try {
                ProjectXML refProjectXML = jobParser.loadProject(new File(projectXML.getProjectFolder(), entry.getValue()));
                projects.put(entry.getKey(), projectGroovyBuilder.build(refProjectXML));
            } catch (Exception e) {
                // TODO
                throw new RuntimeException(e);
            }
        });

        ProjectGroovy projectGroovy = new ProjectGroovy(projectXML.getId(), projectXML.getName(), jobs, projects);

        for (JobGroovy<?> job : jobs.values()) {
            job.init(projectGroovy);
        }
        return projectGroovy;
    }

    private static <T> JobGroovy<T> build(ProjectXML projectXML, JobXML jobXML) throws Exception {
        GroovyShell groovyShell = createGroovyShell(projectXML);
        groovyShell.setProperty("projectFolder", projectXML.getProjectFolder());
        Map<String, JobDependencyGroovy> jobDependencyXMLMap = new LinkedHashMap<>();

        List<JobParameterDefGroovy> jobParameterDefs = new ArrayList<>();
        List<JobExpressionGroovy> jobExpressions = new ArrayList<>();

        List<SimpleParameterXML> sortedSimpleParameterXML = jobXML.getSimpleParameterXMLs().stream()
                .sorted(Comparator.comparing(SimpleParameterXML::getOrder))
                .collect(Collectors.toList());

        for (SimpleParameterXML simpleParameterXML : sortedSimpleParameterXML) {
            JobParameterDefGroovy parameterDef = new JobParameterDefGroovySimple(
                    groovyShell,
                    simpleParameterXML.getKey(),
                    simpleParameterXML.getName(),
                    simpleParameterXML.getCreateComponentScript(),
                    simpleParameterXML.getOnDependenciesChangeScript(),
                    simpleParameterXML.getValidateScript(),
                    simpleParameterXML.isOptional(),
                    simpleParameterXML.isVisible());
            jobDependencyXMLMap.put(parameterDef.getKey(), parameterDef);
            jobParameterDefs.add(parameterDef);
        }

        for (ExpressionXML expressionXML : jobXML.getExpressionXMLs()) {
            JobExpressionGroovy jobExpressionGroovy = new JobExpressionGroovy(
                    groovyShell,
                    expressionXML.getKey(),
                    expressionXML.getName(),
                    expressionXML.getEvaluateScript());
            jobDependencyXMLMap.put(jobExpressionGroovy.getKey(), jobExpressionGroovy);
            jobExpressions.add(jobExpressionGroovy);
        }

        for (CallXML callXML : jobXML.getCallXMLs()) {
            JobCallDefGroovy<Serializable> call = new JobCallDefGroovy<>(
                    callXML.getKey(),
                    callXML.getName(),
                    callXML.getProject(),
                    callXML.getJob(),
                    callXML.getMap());
            jobDependencyXMLMap.put(call.getKey(), call);
            jobParameterDefs.add(call);
        }

        addDependencies(jobXML.getSimpleParameterXMLs(), jobDependencyXMLMap);
        addDependencies(jobXML.getExpressionXMLs(), jobDependencyXMLMap);
        addDependencies(jobXML.getCallXMLs(), jobDependencyXMLMap);

        List<JobParameterDefGroovy> sorteJobParameterDefs = JobDependency.sort(jobParameterDefs);

        return new JobGroovy<>(groovyShell, jobXML.getId(), jobXML.getName(), sorteJobParameterDefs, jobExpressions,
                jobXML.getRunScript(), jobXML.getValidateScript());
    }

    private static void addDependencies(List<? extends JobDependency> parameterXMLs, Map<String, JobDependencyGroovy> jobDependencyXMLMap) {
        for (JobDependency parameterXML : parameterXMLs) {
            JobDependencyGroovy jobDependencyXML = jobDependencyXMLMap.get(parameterXML.getKey());
            for (String depKey : parameterXML.getDependencies()) {
                jobDependencyXML.addDependency(depKey);
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
