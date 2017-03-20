package org.jobsui.core.runner;

import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.ui.UIWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 3/11/17.
 */
class WidgetsMap<C> {
    private final List<ParameterAndWidget<C>> widgets = new ArrayList<>();

    WidgetsMap() {
    }

    void add(ParameterAndWidget<C> parameterAndWidget) {
        widgets.add(parameterAndWidget);
    }

    UIWidget<C> get(JobParameterDef jobParameterDef) {
        for (ParameterAndWidget<C> widget : widgets) {
            if (jobParameterDef.equals(widget.getJobParameterDef())) {
                return widget.getWidget();
            }
        }
        return null;
    }

    List<ParameterAndWidget<C>> getWidgets() {
        return widgets;
    }
}
