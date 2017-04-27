package org.jobsui.core.groovy;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobParameterAbstract;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobResult;
import org.jobsui.core.ui.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobCallGroovy<T extends Serializable> extends JobParameterAbstract implements JobParameterGroovy {
    private final String projectRef;
    private final String jobRef;
    private final Map<String, String> mapArguments;
    private Job job = null;

    public JobCallGroovy(String key, String name, String projectRef, String jobRef, Map<String, String> mapArguments) {
        super(key, name, null, false, false);
        this.projectRef = projectRef;
        this.jobRef = jobRef;
        this.mapArguments = mapArguments;
    }

    @Override
    public <C> UIComponent<C> createComponent(UI<C> ui) throws UnsupportedComponentException {
        @SuppressWarnings("unchecked")
        final UIChoice<C> component = ui.create(UIChoice.class);
        if (getDependencies().isEmpty()) {
            evaluate(component, Collections.emptyMap());
        }
        return component;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
        evaluate(((UIChoice)widget.getComponent()), values);
    }

    private void evaluate(UIChoice component, Map<String, Serializable> values) {
        Map<String, Object> mappedValues = new HashMap<>();

        for (Map.Entry<String, String> entry : mapArguments.entrySet()) {
            mappedValues.put(entry.getValue(), values.get(entry.getKey()));
        }

        JobResult result = job.run(mappedValues);

        if (result.getException() != null) {
            throw new RuntimeException(result.getException());
        }

        Object value = result.get();

        try {
            if (value == null) {
                component.setItems(Collections.emptyList());
            } else {
                component.setItems(Collections.singletonList(value));
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error in evaluate script for expression whit key \"" +
                    getKey() + "\"", e);
        }

    }

    public Map<String, String> getMapArguments() {
        return mapArguments;
    }

    @Override
    public void init(ProjectGroovy project) {
        Project projectToCall;
        if ("this".equals(projectRef)) {
            projectToCall = project;
        } else {
            projectToCall = project.getProject(projectRef);
        }
        if (projectToCall == null) {
            throw new IllegalStateException("Cannot find project \"" + projectRef + "\" for Call \"" + getName() + "\".");
        }

        Job<Object> jobToCall = projectToCall.getJob(jobRef);
        if (jobToCall == null) {
            throw new IllegalStateException("Cannot find job \"" + jobRef + "\" in project \"" + projectRef +
                    "\" for Call \"" + getName() + "\".");
        }
        job = jobToCall;
    }

    @Override
    public boolean isCalculated() {
        return true;
    }
}
