package org.jobsui.core.runner;

import org.jobsui.core.ParameterValidator;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobExpression;
import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.ui.*;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by enrico on 3/11/17.
 */
public class JobRunnerContext<T extends Serializable, C> {
    private static final Logger LOGGER = Logger.getLogger(JobRunnerContext.class.getName());
    private final WidgetsMap<C> widgets;
    private final Job<T> job;
    private final UI<C> ui;
    private final List<JobDependency> sortedJobDependencies;

    public JobRunnerContext(Job<T> job, UI<C> ui, UIWindow<C> window) throws Exception {
        LOGGER.fine("Creating Job runner context");
        this.job = job;
        this.ui = ui;
        widgets = new WidgetsMap<>();

        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            widgets.add(createWidget(ui, window, jobParameterDef));
        }
        sortedJobDependencies = job.getSortedDependencies();
        LOGGER.fine("Created Job runner context");
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
        Map<String, Serializable> validValues = new HashMap<>();

        valueChangeObserver().subscribe(changedValue -> {
            validValues.clear();
            validValues.putAll(changedValue.validValues);
        });

        for (final JobDependency jobDependency : sortedJobDependencies) {
            final List<String> dependencies = jobDependency.getDependencies();
            if (!dependencies.isEmpty()) {
                Collection<Observable<Serializable>> observables = getDependenciesObservables(dependencies).values();

                final Observable<Map<String, Serializable>> observable = combineDependenciesObservables(dependencies,
                        observables, validValues);

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
    public Observable<JobValidation> jobValidationObserver() {
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
                if (jobDependency instanceof ParameterValidator) {
                    ParameterValidator parameterValidator = (ParameterValidator) jobDependency;
                    if (!isValid(parameterValidator, values, value)) {
                        jobValidation.invalidate();
                        break;
                    }
                    values.put(jobDependency.getKey(), value);
                } else {
                    values.put(jobDependency.getKey(), value);
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

    private static boolean isValid(ParameterValidator parameterValidator, Map<String,Serializable> dependenciesValues, Serializable value) {
        final List<String> validate = parameterValidator.validate(dependenciesValues, value);

        return validate.isEmpty();
    }

    private static Map<String, Serializable> getDependenciesValues(Map<String, Serializable> values,
                                                                   JobDependency jobDependency) {
        Map<String,Serializable> dependenciesValues = new HashMap<>();
        for (String dependency : jobDependency.getDependencies()) {
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
                                                                                 Collection<Observable<Serializable>> observables,
                                                                                 Map<String, Serializable> validValues) {
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
                JobParameterDef jobParameterDef = job.getParameter(dependency);
                if (jobParameterDef != null) {
                    Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, jobParameterDef);
                    if (dependenciesValues.size() == jobParameterDef.getDependencies().size() &&
                            jobParameterDef.validate(dependenciesValues, arg).isEmpty()) {
                        result.put(dependency, arg);
                    } else {
                        break;
                    }
                } else {
                    result.put(dependency, arg);
                }
            }
            return result;
        });
    }

    public Observable<ChangedValue> valueChangeObserver() {
        Map<JobDependency, Observable<Serializable>> observables = getDependenciesObservables(
                sortedJobDependencies.stream().map(JobDependency::getKey).collect(Collectors.toList())
        );

        List<Subscriber<? super ChangedValue>> subscribers = new ArrayList<>();

        Map<String, Serializable> values = new HashMap<>();
        Map<String, Serializable> unmodifiableValues = Collections.unmodifiableMap(values);
        Map<String, Serializable> validValues = new HashMap<>();
        Map<String, Serializable> unmodifiableValidValues = Collections.unmodifiableMap(validValues);

        Observable<ChangedValue> mapObservable = Observable.create(subscribers::add);

        for (Map.Entry<JobDependency, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                JobDependency jobDependency = entry.getKey();
                values.put(jobDependency.getKey(), value);
                validValues.put(jobDependency.getKey(), value);

                List<String> validation;
                if (jobDependency instanceof ParameterValidator) {
                    ParameterValidator parameterValidator = (ParameterValidator) jobDependency;
                    Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, jobDependency);
                    if (dependenciesValues.size() == jobDependency.getDependencies().size()) {
                        validation = parameterValidator.validate(dependenciesValues, value);
                        if (!validation.isEmpty()) {
                            validValues.remove(jobDependency.getKey());
                        }
                    } else {
                        validation = Collections.singletonList("Invalid dependencies.");
                        validValues.remove(jobDependency.getKey());
                    }
                } else {
                    validation = Collections.emptyList();
                }

                ChangedValue changedValue = new ChangedValue(entry.getKey(), unmodifiableValues, unmodifiableValidValues,
                        validation);

                subscribers.forEach(s -> s.onNext(changedValue));
            });
        }

        return mapObservable;
    }

    public void setComponentValidationMessage() {
        Map<JobDependency, Observable<Serializable>> observables = getDependenciesObservables(
                sortedJobDependencies.stream().map(JobDependency::getKey).collect(Collectors.toList())
        );

        Map<String, Serializable> validValues = new HashMap<>();

        for (Map.Entry<JobDependency, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                JobDependency jobDependency = entry.getKey();
                if (jobDependency instanceof JobParameterDef) {
                    JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                    UIWidget<C> widget = widgets.get(jobParameterDef);

                    // I set the validation message only if all dependencies are valid
                    if (getDependenciesValues(validValues, jobParameterDef).size() == jobParameterDef.getDependencies().size()) {
                        List<String> validate = jobParameterDef.validate(validValues, value);
                        if (validate.isEmpty()) {
                            validValues.put(jobDependency.getKey(), value);
                        } else {
                            validValues.remove(jobDependency.getKey());
                        }

                        setValidationMessage(validate, jobParameterDef, widget, ui);
                    } else {
                        setValidationMessage(Collections.emptyList(), jobParameterDef, widget, ui);
                    }
                }
            });
        }
    }

//    a try of using valueChangeObserver()
//    public void setComponentValidationMessage() {
//        Observable<ChangedValue> mapObservable = valueChangeObserver();
//
//        mapObservable.subscribe(changedValue -> {
//            if (changedValue.jobDependency instanceof JobParameterDef) {
//                JobParameterDef jobParameterDef = (JobParameterDef) changedValue.jobDependency;
//                UIWidget<C> widget = widgets.get(jobParameterDef);
//
//                Map<String, Serializable> dependenciesValues = getDependenciesValues(changedValue.validValues, jobParameterDef);
//
//                // I set the validation message only if all dependencies are valid
//                if (dependenciesValues.size() == jobParameterDef.getDependencies().size()) {
//                    setValidationMessage(changedValue.validation, jobParameterDef, widget, ui);
//                } else {
//                    setValidationMessage(Collections.emptyList(), jobParameterDef, widget, ui);
//                }
//            }
//        });
//    }

    public static class ChangedValue {
        public final JobDependency jobDependency;
        public final Map<String, Serializable> values;
        private final Map<String, Serializable> validValues;
        private final List<String> validation;

        private ChangedValue(JobDependency jobDependency, Map<String, Serializable> values, Map<String, Serializable> validValues,
                             List<String> validation) {
            this.jobDependency = jobDependency;
            this.values = values;
            this.validValues = validValues;
            this.validation = validation;
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
