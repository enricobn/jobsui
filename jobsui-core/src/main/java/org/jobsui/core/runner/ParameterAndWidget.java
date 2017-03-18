package org.jobsui.core.runner;

import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.UIWidget;

/**
 * Created by enrico on 3/11/17.
 */
class ParameterAndWidget<C> {
    private final JobParameterDef jobParameterDef;
    private final UIWidget<C> widget;

    ParameterAndWidget(JobParameterDef jobParameterDef, UIWidget<C> widget) {
        this.jobParameterDef = jobParameterDef;
        this.widget = widget;
    }

    UIWidget<C> getWidget() {
        return widget;
    }

    JobParameterDef getJobParameterDef() {
        return jobParameterDef;
    }
}
