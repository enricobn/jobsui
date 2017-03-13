package org.jobsui.core.runner;

import org.jobsui.core.Job;
import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 3/11/17.
 */
public class JobRunnerContext<T extends Serializable, C> {
    private final WidgetsMap<C> widgets;

    public JobRunnerContext(Job<T> job, UI<C> ui, UIWindow<C> window) throws UnsupportedComponentException {
        widgets = new WidgetsMap<>();

        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            widgets.add(createWidget(ui, window, (JobParameterDef<Serializable>)jobParameterDef));
        }
    }

    public WidgetsMap<C> getWidgets() {
        return widgets;
    }

    private static <T extends Serializable, C> ParameterAndWidget<T, C> createWidget(final UI<C> ui, UIWindow<C> window,
                                                                 final JobParameterDef<T> jobParameterDef)
            throws UnsupportedComponentException {
        final UIComponent<T, C> component = jobParameterDef.createComponent(ui);
        if (component == null) {
            throw new IllegalStateException("Cannot create component for parameter with key \""
                    + jobParameterDef.getKey() + "\"");
        }

        final UIWidget<T, C> widget = window.add(jobParameterDef.getName(), component);
        if (widget == null) {
            throw new IllegalStateException("Cannot create widget for parameter with key \""
                    + jobParameterDef.getKey() + "\"");
        }

        widget.setVisible(jobParameterDef.isVisible());

        final Observable<T> observable = widget.getComponent().getObservable();

        observable.subscribe(o -> {
            setValidationMessage(jobParameterDef.validate(o), jobParameterDef, widget, ui);
        });
        return new ParameterAndWidget<>(jobParameterDef, widget);
    }


    private static <T extends Serializable> void setValidationMessage(List<String> validate, JobParameterDef<T> jobParameterDef,
                                                                      UIWidget<T, ?> widget, UI<?> ui) {
        if (!jobParameterDef.isVisible()) {
            if (!validate.isEmpty()) {
                ui.showMessage(jobParameterDef.getName() + ": " + JobsUIUtils.getMessagesAsString(validate));
            }
        } else {
            widget.setValidationMessages(validate);
        }
    }

}
