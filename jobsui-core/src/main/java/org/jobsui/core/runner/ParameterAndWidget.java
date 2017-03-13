package org.jobsui.core.runner;

import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.UIWidget;

import java.io.Serializable;

/**
 * Created by enrico on 3/11/17.
 */
public class ParameterAndWidget<T extends Serializable, C> {
    private final JobParameterDef<T> jobParameterDef;
    private final UIWidget<T, C> widget;

    ParameterAndWidget(JobParameterDef<T> jobParameterDef, UIWidget<T, C> widget) {
        this.jobParameterDef = jobParameterDef;
        this.widget = widget;
    }

    public UIWidget<T, C> getWidget() {
        return widget;
    }

    public JobParameterDef<T> getJobParameterDef() {
        return jobParameterDef;
    }
}
