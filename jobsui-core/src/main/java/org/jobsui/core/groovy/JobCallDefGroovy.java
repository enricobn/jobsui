package org.jobsui.core.groovy;

import org.jobsui.core.Job;
import org.jobsui.core.JobFuture;
import org.jobsui.core.JobParameterDefAbstract;
import org.jobsui.core.ui.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobCallDefGroovy<T> extends JobParameterDefAbstract<T> {
    private final Job job;
    private final Map<String, String> mapArguments;

    public JobCallDefGroovy(String key, String name,
                            Job job, Map<String, String> mapArguments) {
        super(key, name, null, false, false);
        this.job = job;
        this.mapArguments = mapArguments;
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        final UIChoice component = (UIChoice) ui.create(UIChoice.class);
        if (getDependencies().isEmpty()) {
            evaluate(component, Collections.<String, Object>emptyMap());
        }
        return component;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
        evaluate(((UIChoice)widget.getComponent()), values);
    }

    private void evaluate(UIChoice component, Map<String, Object> values) {
        Map<String, Object> mappedValues = new HashMap<>();

        for (Map.Entry<String, String> entry : mapArguments.entrySet()) {
            mappedValues.put(entry.getValue(), values.get(entry.getKey()));
        }

        JobFuture future = job.run(mappedValues);

        Object value = future.get();

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
}
