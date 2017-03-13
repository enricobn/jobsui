package org.jobsui.core.runner;

import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.UIWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 3/11/17.
 */
public class WidgetsMap<C> {
    private final List<ParameterAndWidget<Serializable, C>> widgets = new ArrayList<>();

    WidgetsMap() {
    }

    void add(ParameterAndWidget<Serializable, C> parameterAndWidget) {
        widgets.add(parameterAndWidget);
    }

    public UIWidget<?, C> get(JobParameterDef<?> jobParameterDef) {
        for (ParameterAndWidget<?, C> widget : widgets) {
            if (jobParameterDef.equals(widget.getJobParameterDef())) {
                return widget.getWidget();
            }
        }
        return null;
    }

    public List<ParameterAndWidget<Serializable, C>> getWidgets() {
        return widgets;
    }
}
