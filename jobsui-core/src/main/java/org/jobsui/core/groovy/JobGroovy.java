package org.jobsui.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.JobAbstract;
import org.jobsui.core.JobFuture;
import org.jobsui.core.JobParameterDef;

import java.io.Serializable;
import java.util.*;

/**
 * Created by enrico on 5/4/16.
 */
public class JobGroovy<T> extends JobAbstract<T> {
    private final String key;
    private final String name;
    private final List<JobParameterDef<? extends Serializable>> parameterDefs;
    private final Script run;
    private final Script validate;
//    private final File projectFolder;
    private final Binding shellBinding;
    private final GroovyShell shell;

    public JobGroovy(GroovyShell shell, String key, String name, List<JobParameterDefGroovy<Serializable>> parameterDefs,
                     String runScript, String validateScript) {
        this.shell = shell;
        this.key = key;
        this.name = name;
        this.parameterDefs = new ArrayList<>();
        // TODO can I remove this loop?
        this.parameterDefs.addAll(parameterDefs);

//        this.projectFolder = projectFolder;
        this.run = shell.parse(runScript);
        this.validate = validateScript == null ? null : shell.parse(validateScript);
        shellBinding = shell.getContext();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<JobParameterDef<? extends Serializable>> getParameterDefs() {
        return parameterDefs;
    }

    @Override
    public JobFuture<T> run(final Map<String, Serializable> values) {
        return () -> {
            // I reset the bindings otherwise I get "global" or previous bindings
            run.setBinding(new Binding(shellBinding.getVariables()));

            run.setProperty("values", values);
            for (Map.Entry<String, Serializable> entry : values.entrySet()) {
                run.setProperty(entry.getKey(), entry.getValue());
            }

//            run.setProperty("projectFolder", projectFolder);
            try {
                @SuppressWarnings("unchecked")
                T result = (T) this.run.run();
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Cannot execute run for job with id \"" + getId() + "\".", e);
            }
        };
    }

    @Override
    public List<String> validate(Map<String, Serializable> values) {
        if (validate == null) {
            return Collections.emptyList();
        }
        // I reset the bindings otherwise I get "global" or previous bindings
        validate.setBinding(new Binding(shellBinding.getVariables()));
        validate.setProperty("values", values);
        return (List<String>) validate.run();
    }

    public String getId() {
        return key;
    }

    public void init(ProjectGroovy project) {
        for (JobParameterDef jobParameterDef : getParameterDefs()) {
            // TODO can I remove cast?
            ((JobParameterDefGroovy)jobParameterDef).init(project);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return shell.getClassLoader();
    }
}
