package org.bef.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.bef.core.Job;
import org.bef.core.JobFuture;
import org.bef.core.JobParameterDef;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobGroovy<T> implements Job<T> {
    private final String name;
    private final List<JobParameterDef<?>> parameterDefs;
    private final String runScript;
    private final String validateScript;

    public JobGroovy(String name, List<JobParameterDef<?>> parameterDefs, String runScript, String validateScript) {
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
                Binding binding = new Binding();
                binding.setProperty("parameters", parameters);
                GroovyShell shell = new GroovyShell(binding);
                return (T) shell.evaluate(runScript);
            }
        };
    }

    @Override
    public List<String> validate(Map<String, Object> parameters) {
        if (validateScript == null) {
            return Collections.emptyList();
        }
        Binding binding = new Binding();
        binding.setProperty("parameters", parameters);
        GroovyShell shell = new GroovyShell(binding);
        return (List<String>) shell.evaluate(validateScript);
    }
}
