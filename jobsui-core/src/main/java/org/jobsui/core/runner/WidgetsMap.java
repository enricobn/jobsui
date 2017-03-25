package org.jobsui.core.runner;

import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.ui.UIWidget;

import java.util.*;

/**
 * Created by enrico on 3/11/17.
 */
class WidgetsMap<C> {
    private final Map<String,ParameterAndWidget<C>> widgets = new LinkedHashMap<>();

    WidgetsMap() {
    }

    void add(ParameterAndWidget<C> parameterAndWidget) {
        widgets.put(parameterAndWidget.getJobParameterDef().getKey(), parameterAndWidget);
    }

    UIWidget<C> get(JobParameterDef jobParameterDef) {
        ParameterAndWidget<C> parameterAndWidget = widgets.get(jobParameterDef.getKey());
        if (parameterAndWidget == null) {
            throw new IllegalArgumentException("Cannot find widget for parameter " + jobParameterDef);
        }
        return parameterAndWidget.getWidget();
    }

    Collection<ParameterAndWidget<C>> getWidgets() {
        return widgets.values();
    }
}
