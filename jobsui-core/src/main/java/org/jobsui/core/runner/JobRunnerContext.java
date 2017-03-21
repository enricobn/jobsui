package org.jobsui.core.runner;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobExpression;
import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.ui.*;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.functions.Action1;
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
    private final UI<C> ui;
    private final List<JobDependency> sortedJobDependencies;

    public JobRunnerContext(Job<T> job, UI<C> ui, UIWindow<C> window) throws Exception {
        this.job = job;
        this.ui = ui;
        widgets = new WidgetsMap<>();

        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            widgets.add(createWidget(ui, window, jobParameterDef));
        }
        sortedJobDependencies = job.getSortedDependencies();
    }

    public void notifyInitialValue() {
        Map<String,Serializable> values = new HashMap<>();
        for (JobDependency jobDependency : sortedJobDependencies) {
            if (jobDependency instanceof JobExpression) {
                JobExpression jobExpression = (JobExpression) jobDependency;
                if (jobExpression.getDependencies().isEmpty()) {
                    Serializable value = jobExpression.evaluate(values);
                    values.put(jobDependency.getKey(), value);
                    jobExpression.notifySubscribers(value);
                }
            } else if (jobDependency instanceof JobParameterDef) {
                JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                UIComponent<C> component = widgets.get(jobParameterDef).getComponent();
                Serializable value = component.getValue();
                if (isValid(jobParameterDef, values, value)) {
//                    component.setValue(value);
                    values.put(jobDependency.getKey(), value);
                    component.notifySubscribers();
                }
            } else {
                throw new IllegalStateException("Unexpected type " + jobDependency.getClass());
            }
        }

//        for (JobExpression jobExpression : job.getExpressions()) {
//            if (jobExpression.getDependencies().isEmpty()) {
//                jobExpression.evaluate(Collections.emptyMap());
//            }
//        }
//        widgets.getWidgets().forEach(JobRunnerContext::notifyInitialValue);
    }

    public void observeDependencies() {
        for (final JobDependency jobDependency : sortedJobDependencies) {
            final List<String> dependencies = jobDependency.getDependencies();
            if (!dependencies.isEmpty()) {
                Collection<Observable<Serializable>> observables = getDependenciesObservables(dependencies).values();

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
                                JavaFXUI.showErrorStatic("Error on onDependenciesChange for parameter " + jobParameterDef.getName(), e);
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

        //            private boolean isValid(JobParameterDef jobParameterDef, Serializable value) {
//                final List<String> validate = jobParameterDef.validate(value);
//                return validate.isEmpty();
//            }
        return Observable.combineLatest(observables, args -> {
            int i = 0;

            JobValidation jobValidation = new JobValidation();

            Map<String, Serializable> values = new HashMap<>();

            for (JobDependency jobDependency : sortedJobDependencies) {
                Serializable value = (Serializable) args[i++];
                if (jobDependency instanceof JobParameterDef) {
                    JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                    if (!isValid(jobParameterDef, values, value)) {
                        jobValidation.invalidate();
                        break;
                    }
                    values.put(jobParameterDef.getKey(), value);
                } else if (jobDependency instanceof JobExpression) {
                    values.put(jobDependency.getKey(), value);
                } else {
                    throw new IllegalStateException("Unknown type " + jobDependency.getClass());
                }
            }

            if (!jobValidation.isValid()) {
                return jobValidation;
            }

            jobValidation.setMessages(job.validate(values));

            return jobValidation;
        });
    }

    /**
     * Creates an Observable that emits a map with all valid values, when all values are set or
     * a value is changed.
     */
    public Observable<Map<String,Serializable>> valuesChangeObserver() {
        List<Observable<Serializable>> observables = widgets.getWidgets().stream()
                .map(widget -> widget.getWidget().getComponent().getObservable())
                .collect(Collectors.toList());
        observables.addAll(job.getExpressions().stream().map(JobExpression::getObservable).collect(Collectors.toList()));

        return Observable.combineLatest(observables, args -> {
            int i = 0;

            Map<String, Serializable> values = new HashMap<>();

            for (final ParameterAndWidget<C> entry : widgets.getWidgets()) {
                Serializable value = (Serializable) args[i++];
                values.put(entry.getJobParameterDef().getKey(), value);
            }

            for (JobExpression jobExpression : job.getExpressions()) {
                Serializable value = (Serializable) args[i++];
                values.put(jobExpression.getKey(), value);
            }

            for (final ParameterAndWidget<C> entry : widgets.getWidgets()) {
                JobParameterDef jobParameterDef = entry.getJobParameterDef();
                Serializable value = values.get(jobParameterDef.getKey());

                Map<String, Serializable> dependenciesValues = getDependenciesValues(values, jobParameterDef);

                if (!isValid(jobParameterDef, dependenciesValues, value)) {
                    values.remove(jobParameterDef.getKey());
                }
            }

            return values;
        });
    }

    private static boolean isValid(JobParameterDef parameterDef, Map<String,Serializable> dependenciesValues, Serializable value) {
        final List<String> validate = parameterDef.validate(dependenciesValues, value);

        return validate.isEmpty();
    }

    private static Map<String, Serializable> getDependenciesValues(Map<String, Serializable> values, JobParameterDef jobParameterDef) {
        Map<String,Serializable> dependenciesValues = new HashMap<>();
        for (String dependency : jobParameterDef.getDependencies()) {
            if (values.containsKey(dependency)) {
                Serializable dependencyValue = values.get(dependency);
                dependenciesValues.put(dependency, dependencyValue);
            }
        }
        return dependenciesValues;
    }


//    private static <C> void notifyInitialValue(ParameterAndWidget<C> entry) {
//        final Serializable value = entry.getWidget().getComponent().getValue();
//        if (entry.getJobParameterDef().validate(, value).isEmpty()) {
//            entry.getWidget().getComponent().notifySubscribers();
//        }
//    }

    private Observable<Map<String, Serializable>> combineDependenciesObservables(final List<String> dependencies,
                                                                                        Collection<Observable<Serializable>> observables) {
        //            private boolean addValidatedValue(Map<String, Serializable> result, JobParameterDef dependency,
//                                              Serializable arg) {
//                final List<String> validate = dependency.validate(arg);
//                if (!validate.isEmpty()) {
//                    return true;
//                }
//                result.put(dependency.getKey(), arg);
//                return false;
//            }
        return Observable.combineLatest(observables, args -> {
            Map<String, Serializable> result = new HashMap<>();

            int i = 0;
            for (String dependency : dependencies) {
                final Serializable arg = (Serializable) args[i++];
//                    JobParameterDef jobParameterDef = job.getParameter(dependency);
//                    if (jobParameterDef != null) {
//                        if (addValidatedValue(result, jobParameterDef, arg)) {
//                            break;
//                        }
//                    } else {
                    result.put(dependency, arg);
//                    }
            }
            return result;
        });
    }

    public void setComponentValidationMessage() {
        Map<JobDependency, Observable<Serializable>> observables = getDependenciesObservables(
                sortedJobDependencies.stream().map(JobDependency::getKey).collect(Collectors.toList())
        );

        Map<String, Serializable> values = new HashMap<>();

        Map<String, Serializable> validValues = new HashMap<>();

        for (Map.Entry<JobDependency, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                values.put(entry.getKey().getKey(), value);
                if (entry.getKey() instanceof JobParameterDef) {
                    JobParameterDef jobParameterDef = (JobParameterDef) entry.getKey();
                    UIWidget<C> widget = widgets.get(jobParameterDef);

                    // I set the validation message only if all dependencies are valid
                    if (getDependenciesValues(validValues, jobParameterDef).size() == jobParameterDef.getDependencies().size()) {
                        List<String> validate = jobParameterDef.validate(validValues, value);
                        if (validate.isEmpty()) {
                            validValues.put(entry.getKey().getKey(), value);
                        } else {
                            validValues.remove(entry.getKey().getKey());
                        }

                        setValidationMessage(validate, jobParameterDef, widget, ui);
                    } else {
                        setValidationMessage(Collections.emptyList(), jobParameterDef, widget, ui);
                    }
                }
            });
        }
    }

    private Map<JobDependency, Observable<Serializable>> getDependenciesObservables(List<String> dependencies) {
        Map<JobDependency, Observable<Serializable>> observables = new LinkedHashMap<>();
        for (String dependency : dependencies) {
            JobParameterDef jobParameterDef = job.getParameter(dependency);
            if (jobParameterDef != null) {
                final UIWidget widget = widgets.get(jobParameterDef);
                if (widget == null) {
                    throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                            dependency + "\".");
                }
                observables.put(jobParameterDef, widget.getComponent().getObservable());
            } else {
                JobExpression jobExpression = job.getExpression(dependency);
                observables.put(jobExpression, jobExpression.getObservable());
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

//        final Observable<Serializable> observable = widget.getComponent().getObservable();
//
//        observable.subscribe(o -> setValidationMessage(jobParameterDef.validate(, o), jobParameterDef, widget, ui));
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
