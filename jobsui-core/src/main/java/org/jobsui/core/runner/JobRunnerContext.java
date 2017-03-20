package org.jobsui.core.runner;

import org.jobsui.core.Job;
import org.jobsui.core.JobDependency;
import org.jobsui.core.JobExpression;
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
    private final List<JobDependency> sortedJobDependencies;

    public JobRunnerContext(Job<T> job, UI<C> ui, UIWindow<C> window) throws Exception {
        this.job = job;
        widgets = new WidgetsMap<>();

        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            widgets.add(createWidget(ui, window, jobParameterDef));
        }
        sortedJobDependencies = job.getSortedDependencies();
    }

    public void notifyInitialValue() {
        for (JobExpression jobExpression : job.getExpressions()) {
            if (jobExpression.getDependencies().isEmpty()) {
                jobExpression.evaluate(Collections.emptyMap());
            }
        }
        widgets.getWidgets().forEach(JobRunnerContext::notifyInitialValue);
    }

    public void observeDependencies() {
        for (final JobDependency jobDependency : sortedJobDependencies) {
            final List<String> dependencies = jobDependency.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = getDependenciesObservables(dependencies);

                final Observable<Map<String, Serializable>> observable = combineDependenciesObservables(dependencies, observables);

                observable.subscribe(objects -> {
                    // all dependencies are valid
                    if (objects.size() == dependencies.size()) {
                        if (jobDependency instanceof JobParameterDef) {
                            JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                            final UIWidget widget = widgets.get(jobParameterDef);
                            widget.getComponent().setEnabled(true);
                            try {
                                jobParameterDef.onDependenciesChange(widget, objects);
                            } catch (Exception e) {
                                JavaFXUI.showErrorStatic("Error on onDependenciesChange for parameter " + jobDependency.getName(), e);
                                widget.setValidationMessages(Collections.singletonList(e.getMessage()));
                                widget.getComponent().setValue(null);
                                widget.getComponent().setEnabled(false);
                            }
                        } else if (jobDependency instanceof JobExpression) {
                            JobExpression jobExpression = (JobExpression) jobDependency;
                            jobExpression.onDependenciesChange(objects);
                        } else {
                            throw new IllegalStateException("Unknown type " + jobDependency.getClass());
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
        observables.addAll(job.getExpressions().stream().map(JobExpression::getObservable).collect(Collectors.toList()));

        return Observable.combineLatest(observables, new FuncN<JobValidation>() {
            @Override
            public JobValidation call(Object... args) {
                int i = 0;

                JobValidation jobValidation = new JobValidation();

                Map<String, Serializable> values = new HashMap<>();

                for (JobDependency jobDependency : sortedJobDependencies) {
                    Serializable value = (Serializable) args[i++];
                    if (jobDependency instanceof JobParameterDef) {
                        JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                        if (!isValid(jobParameterDef, value)) {
                            jobValidation.invalidate();
                            break;
                        }
                        values.put(jobParameterDef.getKey(), value);
                    } else if (jobDependency instanceof JobExpression) {
                        JobExpression jobExpression = (JobExpression) jobDependency;
                        values.put(jobExpression.getKey(), value);
                    } else {
                        throw new IllegalStateException("Unknown type " + jobDependency.getClass());
                    }
                }

                if (!jobValidation.isValid()) {
                    return jobValidation;
                }

                jobValidation.setMessages(job.validate(values));

                return jobValidation;
            }

            private boolean isValid(JobParameterDef jobParameterDef, Serializable value) {
                final List<String> validate = jobParameterDef.validate(value);
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
        observables.addAll(job.getExpressions().stream().map(JobExpression::getObservable).collect(Collectors.toList()));

        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                int i = 0;

                Map<String, Serializable> values = new HashMap<>();

                for (final ParameterAndWidget<C> entry : widgets.getWidgets()) {
                    Serializable value = (Serializable) args[i++];
                    if (isValid(entry, value)) {
                        values.put(entry.getJobParameterDef().getKey(), value);
                    }
                }

                for (JobExpression jobExpression : job.getExpressions()) {
                    Serializable value = (Serializable) args[i++];
                    values.put(jobExpression.getKey(), value);
                }

                return values;
            }

            private boolean isValid(ParameterAndWidget<C> entry, Serializable value) {
                final JobParameterDef parameterDef = entry.getJobParameterDef();
                final List<String> validate = parameterDef.validate(value);

                return validate.isEmpty();
            }
        });
    }

    private static <C> void notifyInitialValue(ParameterAndWidget<C> entry) {
        final Serializable value = entry.getWidget().getComponent().getValue();
        if (entry.getJobParameterDef().validate(value).isEmpty()) {
            entry.getWidget().getComponent().notifySubscribers();
        }
    }

    private Observable<Map<String, Serializable>> combineDependenciesObservables(final List<String> dependencies,
                                                                                        List<Observable<Serializable>> observables) {
        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                Map<String, Serializable> result = new HashMap<>();

                int i = 0;
                for (String dependency : dependencies) {
                    final Serializable arg = (Serializable) args[i++];
                    JobParameterDef jobParameterDef = job.getParameter(dependency);
                    if (jobParameterDef != null) {
                        if (addValidatedValue(result, jobParameterDef, arg)) {
                            break;
                        }
                    } else {
                        result.put(dependency, arg);
                    }
                }
                return result;
            }

            private boolean addValidatedValue(Map<String, Serializable> result, JobParameterDef dependency,
                                              Serializable arg) {
                final List<String> validate = dependency.validate(arg);
                if (!validate.isEmpty()) {
                    return true;
                }
                result.put(dependency.getKey(), arg);
                return false;
            }
        });
    }

    private List<Observable<Serializable>> getDependenciesObservables(List<String> dependencies) {
        List<Observable<Serializable>> observables = new ArrayList<>();
        for (String dependency : dependencies) {
            JobParameterDef jobParameterDef = job.getParameter(dependency);
            if (jobParameterDef != null) {
                final UIWidget widget = widgets.get(jobParameterDef);
                if (widget == null) {
                    throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                            dependency + "\".");
                }
                observables.add(widget.getComponent().getObservable());
            } else {
                JobExpression jobExpression = job.getExpression(dependency);
                observables.add(jobExpression.getObservable());
            }
        }
        return observables;
    }

    private static <C> ParameterAndWidget<C> createWidget(final UI<C> ui, UIWindow<C> window,
                                                                 final JobParameterDef jobParameterDef)
            throws UnsupportedComponentException {
        final UIComponent<C> component = jobParameterDef.createComponent(ui);
        if (component == null) {
            throw new IllegalStateException("Cannot create component for parameter with key \""
                    + jobParameterDef.getKey() + "\"");
        }

        final UIWidget<C> widget = window.add(jobParameterDef.getName(), component);
        if (widget == null) {
            throw new IllegalStateException("Cannot create widget for parameter with key \""
                    + jobParameterDef.getKey() + "\"");
        }

        widget.setVisible(jobParameterDef.isVisible());

        final Observable<Serializable> observable = widget.getComponent().getObservable();

        observable.subscribe(o -> setValidationMessage(jobParameterDef.validate(o), jobParameterDef, widget, ui));
        return new ParameterAndWidget<>(jobParameterDef, widget);
    }


    private static void setValidationMessage(List<String> validate, JobParameterDef jobParameterDef,
                                                                      UIWidget<?> widget, UI<?> ui) {
        if (!jobParameterDef.isVisible()) {
            if (!validate.isEmpty()) {
                ui.showMessage(jobParameterDef.getName() + ": " + JobsUIUtils.getMessagesAsString(validate));
            }
        } else {
            widget.setValidationMessages(validate);
        }
    }

}
