package org.jobsui.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.JobAbstract;
import org.jobsui.core.JobFuture;
import org.jobsui.core.JobParameterDef;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobGroovy<T> extends JobAbstract<T> {
    private final String key;
    private final String name;
    private final List<JobParameterDef<?>> parameterDefs;
    private final Script run;
    private final Script validate;
    private final File projectFolder;

    public JobGroovy(GroovyShell shell, String key, String name, List<JobParameterDef<?>> parameterDefs,
                     String runScript, String validateScript, File projectFolder) {
        this.key = key;
        this.name = name;
        this.parameterDefs = parameterDefs;
        this.projectFolder = projectFolder;
        this.run = shell.parse(runScript);
        this.validate = validateScript == null ? null : shell.parse(validateScript);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<JobParameterDef<?>> getParameterDefs() {
        return parameterDefs;
    }

    @Override
    public JobFuture<T> run(final Map<String, Object> values) {
        return new JobFuture<T>() {
            @Override
            public T get() {
                run.setProperty("values", values);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    run.setProperty(entry.getKey(), entry.getValue());
                }

                run.setProperty("projectFolder", projectFolder);
                try {
                    return (T) run.run();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot execute run for job with key \"" + getKey() + "\".", e);
                }
            }
        };
    }

    @Override
    public List<String> validate(Map<String, Object> values) {
        if (validate == null) {
            return Collections.emptyList();
        }
        validate.setProperty("values", values);
        return (List<String>) validate.run();
    }

    public String getKey() {
        return key;
    }
}