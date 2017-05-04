package org.jobsui.core.groovy;

import com.github.zafarkhaja.semver.Version;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.job.JobAbstract;
import org.jobsui.core.job.JobExpression;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.runner.JobResult;
import org.jobsui.core.runner.JobResultImpl;
import org.jobsui.core.xml.WizardStep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobGroovy<T> extends JobAbstract<T> {
    private final String id;
    private final Version version;
    private final String name;
    private final List<JobParameterGroovy> parameterDefsGroovy;
    private final List<WizardStep> wizardSteps;
    private final List<JobExpression> expressions;
    private final List<JobParameter> parameterDefs;
    private final Script run;
    private final Script validate;
    private final Binding shellBinding;
    private final GroovyShell shell;

    public JobGroovy(GroovyShell shell, String id, Version version, String name, List<JobParameterGroovy> parameterDefsGroovy,
                     List<JobExpressionGroovy> expressions, String runScript, String validateScript, List<WizardStep> wizardSteps) {
        this.shell = shell;
        this.id = id;
        this.version = version;
        this.name = name;
        this.parameterDefsGroovy = parameterDefsGroovy;
        this.wizardSteps = wizardSteps;
        this.expressions = new ArrayList<>();
        this.expressions.addAll(expressions);
        this.parameterDefs = new ArrayList<>();
        this.parameterDefs.addAll(parameterDefsGroovy);
        this.run = shell.parse(runScript);
        this.validate = validateScript == null ? null : shell.parse(validateScript);
        shellBinding = shell.getContext();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<JobParameter> getParameterDefs() {
        return Collections.unmodifiableList(parameterDefs);
    }

    @Override
    public List<JobExpression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public JobResult<T> run(final Map<String, Serializable> values) {
        // I reset the bindings otherwise I get "global" or previous bindings
        run.setBinding(new Binding(shellBinding.getVariables()));

        run.setProperty("values", values);
        for (Map.Entry<String, Serializable> entry : values.entrySet()) {
            run.setProperty(entry.getKey(), entry.getValue());
        }

        try {
            @SuppressWarnings("unchecked")
            T result = (T) this.run.run();
            return new JobResultImpl<>(result);
        } catch (Exception e) {
            return new JobResultImpl<>(new RuntimeException("Cannot execute run for job with id \"" + getId() + "\".", e));
        }
    }

    @Override
    public List<String> validate(Map<String, Serializable> values) {
        if (validate == null) {
            return Collections.emptyList();
        }
        // I reset the bindings otherwise I get "global" or previous bindings
        validate.setBinding(new Binding(shellBinding.getVariables()));
        validate.setProperty("values", values);

        @SuppressWarnings("unchecked")
        List<String> validation = (List<String>) validate.run();
        return validation;
    }

    public String getId() {
        return id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public void init(ProjectGroovy project) {
        for (JobParameterGroovy jobParameter : parameterDefsGroovy) {
            jobParameter.init(project);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return shell.getClassLoader();
    }

    @Override
    public List<WizardStep> getWizardSteps() {
        return wizardSteps;
    }
}
