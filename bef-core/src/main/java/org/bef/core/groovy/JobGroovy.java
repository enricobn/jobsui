package org.bef.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
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
    private final String key;
    private final String name;
    private final List<JobParameterDef<?>> parameterDefs;
    private final Script run;
    private final Script validate;

    public JobGroovy(GroovyShell shell, String key, String name, List<JobParameterDef<?>> parameterDefs, String runScript, String validateScript) {
        this.key = key;
        this.name = name;
        this.parameterDefs = parameterDefs;
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
    public JobFuture<T> run(final Map<String, Object> parameters) {
        return new JobFuture<T>() {
            @Override
            public T get() {
                run.setProperty("values", parameters);
                return (T) run.run();
            }
        };
    }

    @Override
    public List<String> validate(Map<String, Object> parameters) {
        if (validate == null) {
            return Collections.emptyList();
        }
        validate.setProperty("values", parameters);
        return (List<String>) validate.run();
    }

    public String getKey() {
        return key;
    }
}
