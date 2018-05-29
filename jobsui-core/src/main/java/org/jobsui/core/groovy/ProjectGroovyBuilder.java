package org.jobsui.core.groovy;

import com.github.zafarkhaja.semver.Version;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.bookmark.SavedLink;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectBuilder;
import org.jobsui.core.job.ProjectId;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class ProjectGroovyBuilder implements ProjectBuilder {
    private static final Logger LOGGER = Logger.getLogger(ProjectGroovyBuilder.class.getName());

    @Override
    public Project build(ProjectXML projectXML, BookmarksStore bookmarksStore) throws Exception {
        LOGGER.info("Building project " + projectXML.getId());

        LOGGER.info("Creating groovy shell for project " + projectXML.getId());
        GroovyShell groovyShell = createGroovyShell(projectXML);
        groovyShell.setProperty("projectRelativeURL", toGroovyFunction(projectXML::getRelativeURL));

        LOGGER.info("Created groovy shell for project " + projectXML.getId());

        Map<String, JobGroovy<Serializable>> jobs = new HashMap<>();

        projectXML.getJobs()
                .forEach(jobXML -> {
                    try {
                        jobs.put(jobXML.getId(), build(groovyShell, jobXML));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Map<String, Project> projects = new HashMap<>();

        ProjectParserImpl projectParser = new ProjectParserImpl();

        ProjectGroovyBuilder projectGroovyBuilder = new ProjectGroovyBuilder();
        projectXML.getImports().forEach((key, value) -> {
            try {
//                ProjectParser projectParser = projectXML.getJobParser(entry.getValue());
                ProjectXML refProjectXML = projectParser.parse(projectXML.getRelativeURL(value));
                projects.put(key, projectGroovyBuilder.build(refProjectXML, bookmarksStore));
            } catch (Exception e) {
                // TODO
                throw new RuntimeException(e);
            }
        });

        ProjectGroovy projectGroovy = new ProjectGroovy(ProjectId.of(projectXML.getId(), projectXML.getVersion()),
                projectXML.getName(), jobs, projects);

        groovyShell.setProperty("saved", toGroovyFunction((String jobId) ->
                bookmarksStore.getBookmarks(projectGroovy, jobs.get(jobId)).values().stream()
                .map(it -> new SavedLink(it.getKey(), jobId, it.getName()))
                .collect(Collectors.toList())
            )
        );

        for (JobGroovy<?> job : jobs.values()) {
            job.init(projectGroovy);
        }
        LOGGER.info("Built project " + projectXML.getId());
        return projectGroovy;
    }

    private static <T> JobGroovy<T> build(GroovyShell groovyShell, JobXML jobXML) throws Exception {
        Map<String, JobDependencyGroovy> jobDependencyXMLMap = new LinkedHashMap<>();

        List<JobParameterGroovy> jobParameters = new ArrayList<>();
        List<JobExpressionGroovy> jobExpressions = new ArrayList<>();

        List<SimpleParameterXML> sortedSimpleParameterXML = jobXML.getSimpleParameterXMLs().stream()
                .sorted(Comparator.comparing(SimpleParameterXML::getOrder))
                .collect(Collectors.toList());

        for (SimpleParameterXML simpleParameterXML : sortedSimpleParameterXML) {
            JobParameterGroovy parameterDef = new JobParameterGroovySimple(
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
            jobParameters.add(parameterDef);
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
            JobCallGroovy<Serializable> call = new JobCallGroovy<>(
                    callXML.getKey(),
                    callXML.getName(),
                    callXML.getProject(),
                    callXML.getJob(),
                    callXML.getMap());
            jobDependencyXMLMap.put(call.getKey(), call);
            jobParameters.add(call);
        }

        addDependencies(jobXML.getSimpleParameterXMLs(), jobDependencyXMLMap);
        addDependencies(jobXML.getExpressionXMLs(), jobDependencyXMLMap);
        addDependencies(jobXML.getCallXMLs(), jobDependencyXMLMap);

        List<JobParameterGroovy> sortedJobParameterDefs = JobDependency.sort(jobParameters);

        return new JobGroovy<>(groovyShell, jobXML.getId(), Version.valueOf(jobXML.getVersion()), jobXML.getName(), sortedJobParameterDefs, jobExpressions,
                jobXML.getRunScript(), jobXML.getValidateScript(), jobXML.getWizardSteps());
    }

    private static <T,R> Object toGroovyFunction(Function<T,R> function) {
        return new Object() {
            public R call(T arg) {
                return function.apply(arg);
            }
        };
    }

    private static <T,U,R> Object toGroovyFunction(BiFunction<T,U,R> function) {
        return new Object() {
            public R call(T arg, U arg1) {
                return function.apply(arg, arg1);
            }
        };
    }

    private static void addDependencies(List<? extends JobDependency> jobDependencies,
                                        Map<String, JobDependencyGroovy> jobDependencyGroovyMap) {
        for (JobDependency jobDependency : jobDependencies) {
            JobDependencyGroovy jobDependencyGroovy = jobDependencyGroovyMap.get(jobDependency.getKey());
            for (String depKey : jobDependency.getDependencies()) {
                jobDependencyGroovy.addDependency(depKey);
            }
        }
    }

    private static GroovyShell createGroovyShell(ProjectXML projectXML) throws IOException, ParseException {
        GroovyScriptEngine engine = new GroovyScriptEngine(projectXML.getScripsLocationsURLS());
        GroovyClassLoader cl = engine.getGroovyClassLoader();

        for (ProjectLibraryXML library : projectXML.getLibraries()) {
            File file = IvyUtils.resolveArtifact(library.getGroupId(), library.getArtifactId(), library.getVersion());

            cl.addURL(file.toURI().toURL());
        }

        ImportCustomizer importCustomizer = new ImportCustomizer();
//        importCustomizer.addStarImports("org.jobsui.core", "org.jobsui.core.ui");

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        return new GroovyShell(cl, compilerConfiguration);
    }

}
