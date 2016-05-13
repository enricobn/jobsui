package org.bef.core.groovy;

import groovy.lang.GroovyShell;
import org.bef.core.JobAbstract;
import org.bef.core.JobFuture;
import org.bef.core.JobParameterDef;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobGroovy<T> extends JobAbstract<T> {
    private final GroovyShell shell;
    private final String key;
    private final String name;
    private final List<JobParameterDef<?>> parameterDefs;
    private final String runScript;
    private final String validateScript;

    public JobGroovy(GroovyShell shell, String key, String name, List<JobParameterDef<?>> parameterDefs, String runScript, String validateScript) {
        this.shell = shell;
        this.key = key;
        this.name = name;
        this.parameterDefs = parameterDefs;
        this.runScript = runScript;
        this.validateScript = validateScript;
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
    public JobFuture<T> run(final Map<String, Object> parameters) {
        return new JobFuture<T>() {
            @Override
            public T get() {
                shell.setProperty("values", parameters);
                return (T) shell.evaluate(runScript);
            }
        };
    }

    @Override
    public List<String> validate(Map<String, Object> parameters) {
        if (validateScript == null) {
            return Collections.emptyList();
        }
        shell.setProperty("values", parameters);
        return (List<String>) shell.evaluate(validateScript);
    }

    public String getKey() {
        return key;
    }
}
