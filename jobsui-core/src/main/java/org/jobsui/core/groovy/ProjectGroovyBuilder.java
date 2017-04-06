package org.jobsui.core.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jobsui.core.Project;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class ProjectGroovyBuilder {
    private static final Logger LOGGER = Logger.getLogger(ProjectGroovyBuilder.class.getName());

    public ProjectGroovy build(ProjectXML projectXML) throws Exception {
        LOGGER.info("Building project " + projectXML.getId());

        LOGGER.info("Creating groovy shell for project " + projectXML.getId());
        GroovyShell groovyShell = createGroovyShell(projectXML);
        groovyShell.setProperty("projectRelativeURL", toGroovyFunction(projectXML::getRelativeURL));
        LOGGER.info("Created groovy shell for project " + projectXML.getId());

        Map<String, JobGroovy<Serializable>> jobs = new HashMap<>();

        for (String job : projectXML.getJobs()) {
            jobs.put(projectXML.getJobId(job), build(groovyShell, JobParserImpl.parse(projectXML, job)));
        }

        Map<String, Project> projects = new HashMap<>();

        ProjectParserImpl projectParser = new ProjectParserImpl();

        ProjectGroovyBuilder projectGroovyBuilder = new ProjectGroovyBuilder();
        projectXML.getImports().entrySet().forEach(entry -> {
            try {
//                ProjectParser projectParser = projectXML.getJobParser(entry.getValue());
                ProjectXML refProjectXML = projectParser.parse(projectXML.getRelativeURL(entry.getValue()));
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
        LOGGER.info("Built project " + projectXML.getId());
        return projectGroovy;
    }

    private static <T> JobGroovy<T> build(GroovyShell groovyShell, JobXML jobXML) throws Exception {
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
                    simpleParameterXML.getComponent(),
                    simpleParameterXML.getOnInitScript(),
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

    private static <T,R> Object toGroovyFunction(Function<T,R> function) {
        return new Object() {
            public R call(T arg) throws Exception {
                return function.apply(arg);
            }
        };
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
        GroovyScriptEngine engine = new GroovyScriptEngine(projectXML.getScripsLocationsURLS());
        GroovyClassLoader cl = engine.getGroovyClassLoader();

        for (String library : projectXML.getLibraries()) {
            String[] split = library.split(":");
            File file = IvyUtils.resolveArtifact(split[0], split[1], split[2]);

            cl.addURL(file.toURI().toURL());
        }

        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports("org.jobsui.core", "org.jobsui.core.ui");

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        return new GroovyShell(cl, compilerConfiguration);
    }

}
