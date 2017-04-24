package org.jobsui.core.runner;

import org.jobsui.core.job.ParameterValidator;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobExpression;
import org.jobsui.core.job.JobParameterDef;
import org.jobsui.core.ui.*;
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

    public void disableDependants(JobDependency jobDependency) {
        for (JobDependency dependant : getDependants(jobDependency)) {
            if (dependant instanceof JobParameterDef) {
                getWidget((JobParameterDef) dependant).setDisable(true);
            }
            disableDependants(dependant);
        }
    }

    public void reEnableDependants(Map<String,Serializable> validValues, JobDependency jobDependency) {
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

    public Collection<JobDependency> getDependants(JobDependency jobDependency) {
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

    public static boolean isValid(ParameterValidator parameterValidator, Map<String,Serializable> dependenciesValues, Serializable value) {
        final List<String> validate = parameterValidator.validate(dependenciesValues, value);

        return validate.isEmpty();
    }

    public static Map<String, Serializable> getDependenciesValues(Map<String, Serializable> values,
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

    public Observable<Map<String, Serializable>> combineDependenciesObservables(
            List<String> dependencies,
            Collection<Observable<Serializable>> observables,
            Map<String, Serializable> validValues)
    {
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

    public UIWidget<C> getWidget(JobParameterDef jobParameterDef) {
        return widgets.get(jobParameterDef.getKey());
    }

    public DependenciesObservables getDependenciesObservables(List<String> dependencies) {
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

}
