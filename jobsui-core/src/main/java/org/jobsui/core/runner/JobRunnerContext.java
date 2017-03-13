package org.jobsui.core.runner;

import org.jobsui.core.Job;
import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.functions.FuncN;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 3/11/17.
 */
public class JobRunnerContext<T extends Serializable, C> {
    private final WidgetsMap<C> widgets;
    private final Job<T> job;

    public JobRunnerContext(Job<T> job, UI<C> ui, UIWindow<C> window) throws UnsupportedComponentException {
        this.job = job;
        widgets = new WidgetsMap<>();

        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            widgets.add(createWidget(ui, window, (JobParameterDef<Serializable>)jobParameterDef));
        }
    }

    /**
     * Creates an Observable that emits a JobValidation, with the validation status of the job, when all values are set or
     * a value is changed.
     */
    public Observable<JobValidation> validationObserver() {
        List<Observable<?>> observables = widgets.getWidgets().stream()
                .map(widget -> widget.getWidget().getComponent().getObservable())
                .collect(Collectors.toList());

        return Observable.combineLatest(observables, new FuncN<JobValidation>() {
            @Override
            public JobValidation call(Object... args) {
                int i = 0;

                JobValidation jobValidation = new JobValidation();

                Map<String, Serializable> values = new HashMap<>();

                for (final ParameterAndWidget<Serializable, C> entry : widgets.getWidgets()) {
                    Serializable value = (Serializable) args[i++];
                    if (!isValid(entry, value)) {
                        jobValidation.invalidate();
                        break;
                    }
                    values.put(entry.getJobParameterDef().getKey(), value);
                }

                if (!jobValidation.isValid()) {
                    return jobValidation;
                }

                jobValidation.setMessages(job.validate(values));

                return jobValidation;
            }

            private boolean isValid(ParameterAndWidget<Serializable, C> entry, Serializable value) {
                final JobParameterDef<Serializable> parameterDef = entry.getJobParameterDef();
                final List<String> validate = parameterDef.validate(value);

                return validate.isEmpty();
            }
        });
    }

    /**
     * Creates an Observable that emits a map with all valid values, when all values are set or
     * a value is changed.
     */
    public Observable<Map<String,Serializable>> valuesChangeObserver() {
        List<Observable<?>> observables = widgets.getWidgets().stream()
                .map(widget -> widget.getWidget().getComponent().getObservable())
                .collect(Collectors.toList());

        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                int i = 0;

                Map<String, Serializable> values = new HashMap<>();

                for (final ParameterAndWidget<Serializable, C> entry : widgets.getWidgets()) {
                    Serializable value = (Serializable) args[i++];
                    if (isValid(entry, value)) {
                        values.put(entry.getJobParameterDef().getKey(), value);
                    }
                }

                return values;
            }

            private boolean isValid(ParameterAndWidget<Serializable, C> entry, Serializable value) {
                final JobParameterDef<Serializable> parameterDef = entry.getJobParameterDef();
                final List<String> validate = parameterDef.validate(value);

                return validate.isEmpty();
            }
        });
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
