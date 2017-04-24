package org.jobsui.core.runner;

import org.jobsui.core.job.ParameterValidator;
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
public class JobUIRunnerContext<T extends Serializable, C> {
    private static final Logger LOGGER = Logger.getLogger(JobUIRunnerContext.class.getName());
    private final UI<C> ui;
    private final Job<T> job;
    private final Map<String,UIWidget<C>> widgets;
    private final List<JobDependency> sortedJobDependencies;
    private final DependenciesObservables dependenciesObservables;

    private JobUIRunnerContext(UI<C> ui, Job<T> job, Map<String, UIWidget<C>> widgets,
                              List<JobDependency> sortedJobDependencies,
                              DependenciesObservables dependenciesObservables) {
        this.ui = ui;
        this.job = job;
        this.widgets = widgets;
        this.sortedJobDependencies = sortedJobDependencies;
        this.dependenciesObservables = dependenciesObservables;
    }

    public static <T extends Serializable, C> JobUIRunnerContext<T,C> of(Job<T> job, UI<C> ui, UIWindow<C> window) throws Exception {
        LOGGER.fine("Creating Job runner context");

        Map<String,UIWidget<C>> widgets = new HashMap<>();
        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            UIWidget<C> widget = createWidget(ui, window, jobParameterDef);
            widget.setDisable(!jobParameterDef.getDependencies().isEmpty());
            widgets.put(jobParameterDef.getKey(), widget);
        }

        List<JobDependency> sortedDependencies = job.getSortedDependencies();

        List<String> sortedDependenciesKeys = sortedDependencies.stream()
                .map(JobDependency::getKey)
                .collect(Collectors.toList());
        DependenciesObservables dependenciesObservables = getDependenciesObservables(job, widgets, sortedDependenciesKeys);

        LOGGER.fine("Created Job runner context");

        return new JobUIRunnerContext<>(ui, job, widgets, sortedDependencies, dependenciesObservables);
    }

    public Job<T> getJob() {
        return job;
    }

    public UI<C> getUi() {
        return ui;
    }

    public List<JobDependency> getSortedJobDependencies() {
        return sortedJobDependencies;
    }

    public DependenciesObservables getDependenciesObservables() {
        return dependenciesObservables;
    }

    public static <T extends Serializable, C> void notifyInitialValue(JobUIRunnerContext<T, C> jobUIRunnerContext) {
        Map<String,Serializable> values = new HashMap<>();
        for (JobDependency jobDependency : jobUIRunnerContext.getSortedJobDependencies()) {
            if (jobDependency instanceof JobExpression) {
                JobExpression jobExpression = (JobExpression) jobDependency;
                if (jobExpression.getDependencies().isEmpty()) {
                    Serializable value = jobExpression.evaluate(values);
                    values.put(jobDependency.getKey(), value);
                    jobExpression.notifySubscribers(value);
                }
            } else if (jobDependency instanceof JobParameterDef) {
                JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                UIComponent<C> component = jobUIRunnerContext.getWidget(jobParameterDef).getComponent();
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
    }

    public static <T extends Serializable, C> void observeDependencies(JobUIRunnerContext<T, C> jobUIRunnerContext) {
        Map<String, Serializable> validValues = new HashMap<>();

        jobUIRunnerContext.valueChangeObserver().subscribe(changedValue -> {
            validValues.clear();
            validValues.putAll(changedValue.validValues);
        });

        for (final JobDependency jobDependency : jobUIRunnerContext.getSortedJobDependencies()) {
            final List<String> dependencies = jobDependency.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = jobUIRunnerContext.getDependenciesObservables(dependencies).getList();

                final Observable<Map<String, Serializable>> observable = jobUIRunnerContext.combineDependenciesObservables(dependencies,
                        observables, validValues);

                observable.subscribe(objects -> {
                    // all dependencies are valid
                    if (objects.size() == dependencies.size()) {
                        if (jobDependency instanceof JobParameterDef) {
                            JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                            final UIWidget widget = jobUIRunnerContext.getWidget(jobParameterDef);
                            widget.setDisable(false);
                            jobUIRunnerContext.reEnableDependants(validValues, jobDependency);
                            try {
                                jobParameterDef.onDependenciesChange(widget, objects);
                            } catch (Exception e) {
                                JavaFXUI.showErrorStatic("Error on onDependenciesChange for parameter " + jobParameterDef.getName(), e);
                                widget.setValidationMessages(Collections.singletonList(e.getMessage()));
                                widget.getComponent().setValue(null);
                                widget.setDisable(true);
                                jobUIRunnerContext.disableDependants(jobDependency);
                            }
                        } else if (jobDependency instanceof JobExpression) {
                            JobExpression jobExpression = (JobExpression) jobDependency;
                            //jobExpression.onDependenciesChange(objects);
                            Serializable value = jobExpression.evaluate(objects);
                            jobExpression.notifySubscribers(value);
                            jobUIRunnerContext.reEnableDependants(validValues, jobDependency);
                        } else {
                            throw new IllegalStateException("Unknown type " + jobDependency.getClass());
                        }
                    } else {
                        if (jobDependency instanceof JobParameterDef) {
                            JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                            final UIWidget widget = jobUIRunnerContext.getWidget(jobParameterDef);
                            widget.setDisable(true);
                        }
                        jobUIRunnerContext.disableDependants(jobDependency);
                    }
                });
            }
        }
    }

    private void disableDependants(JobDependency jobDependency) {
        for (JobDependency dependant : getDependants(jobDependency)) {
            if (dependant instanceof JobParameterDef) {
                getWidget((JobParameterDef) dependant).setDisable(true);
            }
            disableDependants(dependant);
        }
    }

    private void reEnableDependants(Map<String,Serializable> validValues, JobDependency jobDependency) {
        for (JobDependency dependant : getDependants(jobDependency)) {
            Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, dependant);
            if (dependenciesValues.size() == dependant.getDependencies().size()) {
                if (dependant instanceof JobParameterDef) {
                    getWidget((JobParameterDef) dependant).setDisable(false);
                }
                reEnableDependants(validValues, dependant);
            }
        }
    }

    private Collection<JobDependency> getDependants(JobDependency jobDependency) {
        Collection<JobDependency> result = new ArrayList<>();
        for (JobDependency dependency : getSortedJobDependencies()) {
            if (dependency.getDependencies().contains(jobDependency.getKey())) {
                result.add(dependency);
            }
        }
        return result;
    }

    /**
     * Creates an Observable that emits a JobValidation, with the validation status of the job, when all values are set or
     * a value is changed.
     */
    public Observable<JobValidation> jobValidationObserver() {
        List<Observable<Serializable>> observables = getDependenciesObservables().getList();

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
        List<Observable<Serializable>> observables = dependenciesObservables.getList();

        return Observable.combineLatest(observables, args -> {
            int i = 0;

            Map<String, Serializable> values = new HashMap<>();

            for (JobDependency jobDependency : sortedJobDependencies) {
                Serializable value = (Serializable) args[i++];
                values.put(jobDependency.getKey(), value);
            }

            for (final Map.Entry<String,UIWidget<C>> entry : widgets.entrySet()) {
                JobParameterDef jobParameterDef = job.getParameter(entry.getKey());
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
        Map<String, Observable<Serializable>> observables = dependenciesObservables.getMap();

        List<Subscriber<? super ChangedValue>> subscribers = new ArrayList<>();

        Map<String, Serializable> values = new HashMap<>();
        Map<String, Serializable> unmodifiableValues = Collections.unmodifiableMap(values);
        Map<String, Serializable> validValues = new HashMap<>();
        Map<String, Serializable> unmodifiableValidValues = Collections.unmodifiableMap(validValues);

        Observable<ChangedValue> mapObservable = Observable.create(subscribers::add);

        for (Map.Entry<String, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                String key = entry.getKey();
                JobDependency jobDependency = job.getJobDependency(key);
                values.put(key, value);
                validValues.put(key, value);

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

                ChangedValue changedValue = new ChangedValue(jobDependency, unmodifiableValues, unmodifiableValidValues,
                        validation);

                subscribers.forEach(s -> s.onNext(changedValue));
            });
        }

        return mapObservable;
    }

    public void setComponentValidationMessage() {
        Map<String, Observable<Serializable>> observables = dependenciesObservables.getMap();

        Map<String, Serializable> validValues = new HashMap<>();

        for (Map.Entry<String, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                JobDependency jobDependency = job.getJobDependency(entry.getKey());
                if (jobDependency instanceof JobParameterDef) {
                    JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
                    UIWidget<C> widget = getWidget(jobParameterDef);

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

    public UIWidget<C> getWidget(JobParameterDef jobParameterDef) {
        return widgets.get(jobParameterDef.getKey());
    }

//    private List<Observable<Serializable>> getObservables() {
//        List<Observable<Serializable>> result = new ArrayList<>();
//        for (JobDependency jobDependency : sortedJobDependencies) {
//            if (jobDependency instanceof JobExpression) {
//                JobExpression jobExpression = (JobExpression) jobDependency;
//                result.add(jobExpression.getObservable());
//            } else if (jobDependency instanceof JobParameterDef) {
//                JobParameterDef jobParameterDef = (JobParameterDef) jobDependency;
//                UIComponent<C> component = widgets.get(jobParameterDef).getComponent();
//                result.add(component.getObservable());
//            } else {
//                throw new IllegalStateException("Unexpected type " + jobDependency.getClass());
//            }
//        }
//        return result;
//    }

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

    private DependenciesObservables getDependenciesObservables(List<String> dependencies) {
        return getDependenciesObservables(job, widgets, dependencies);
    }

    private static <T extends Serializable, C> DependenciesObservables getDependenciesObservables(
            Job<T> job,
            Map<String,UIWidget<C>> widgets,
            List<String> dependencies)
    {
        DependenciesObservablesImpl result = new DependenciesObservablesImpl();

        for (String dependency : dependencies) {
            JobParameterDef jobParameterDef = job.getParameter(dependency);
            if (jobParameterDef != null) {
                final UIWidget widget = widgets.get(jobParameterDef.getKey());
                if (widget == null) {
                    throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                            dependency + "\".");
                }
                result.add(jobParameterDef, widget.getComponent().getObservable());
            } else {
                JobExpression jobExpression = job.getExpression(dependency);
                result.add(jobExpression, jobExpression.getObservable());
            }
        }
        return result;
    }

    public interface DependenciesObservables {

        Map<String, Observable<Serializable>> getMap();

        List<Observable<Serializable>> getList();

    }

    private static class DependenciesObservablesImpl implements DependenciesObservables {
        private final Map<String,Observable<Serializable>> map = new LinkedHashMap<>();
        private final Map<String,Observable<Serializable>> unmodifiableMap = Collections.unmodifiableMap(map);


        private void add(JobDependency jobDependency, Observable<Serializable> observable) {
            map.put(jobDependency.getKey(), observable);
        }

        public Map<String, Observable<Serializable>> getMap() {
            return unmodifiableMap;
        }

        @Override
        public List<Observable<Serializable>> getList() {
            return new ArrayList<>(map.values());
        }
    }

    private static <C> UIWidget<C> createWidget(final UI<C> ui, UIWindow<C> window,
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
        return widget;
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
