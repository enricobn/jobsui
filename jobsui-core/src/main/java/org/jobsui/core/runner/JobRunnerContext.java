package org.jobsui.core.runner;

import org.jobsui.core.Job;
import org.jobsui.core.JobParameterDef;
import org.jobsui.core.ui.*;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.functions.FuncN;

import java.io.Serializable;
import java.util.*;
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

    public void notifyInitialValue() {
        widgets.getWidgets().forEach(JobRunnerContext::notifyInitialValue);
    }

    public void observeDependencies() {
        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef<? extends Serializable>> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = getDependenciesObservables(dependencies);

                final Observable<Map<String, Serializable>> observable = combineDependenciesObservables(dependencies, observables);

                observable.subscribe(objects -> {
                    // all dependencies are valid
                    if (objects.size() == dependencies.size()) {
                        final UIWidget widget = widgets.get(jobParameterDef);
                        widget.getComponent().setEnabled(true);
                        try {
                            jobParameterDef.onDependenciesChange(widget, objects);
                        } catch (Exception e) {
                            JavaFXUI.showErrorStatic("Error on onDependenciesChange for parameter " + jobParameterDef.getName(), e);
                            widget.setValidationMessages(Collections.singletonList(e.getMessage()));
                            widget.getComponent().setValue(null);
                            widget.getComponent().setEnabled(false);
                        }
                    }
                });
            }
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

    private static <T1 extends Serializable, C> void notifyInitialValue(ParameterAndWidget<T1, C> entry) {
        final T1 value = entry.getWidget().getComponent().getValue();
        if (entry.getJobParameterDef().validate(value).isEmpty()) {
            entry.getWidget().getComponent().notifySubscribers();
        }
    }

    private Observable<Map<String, Serializable>> combineDependenciesObservables(final List<JobParameterDef<? extends Serializable>> dependencies,
                                                                                        List<Observable<Serializable>> observables) {
        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                Map<String, Serializable> result = new HashMap<>();

                int i = 0;
                for (JobParameterDef dependency : dependencies) {
                    final Serializable arg = (Serializable) args[i++];
                    if (addValidatedValue(result, dependency, arg)) break;
                }
                return result;
            }

            private <T1 extends Serializable> boolean addValidatedValue(Map<String, Serializable> result, JobParameterDef<T1> dependency, T1 arg) {
                final List<String> validate = dependency.validate(arg);
                if (!validate.isEmpty()) {
                    return true;
                }
                result.put(dependency.getKey(), arg);
                return false;
            }
        });
    }

    private List<Observable<Serializable>> getDependenciesObservables(List<JobParameterDef<? extends Serializable>> dependencies) {
        List<Observable<Serializable>> observables = new ArrayList<>();
        for (JobParameterDef dependency : dependencies) {
            final UIWidget widget = widgets.get(dependency);
            if (widget == null) {
                throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                        dependency.getKey() + "\".");
            }
            observables.add(widget.getComponent().getObservable());
        }
        return observables;
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
