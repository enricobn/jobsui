package org.jobsui.core.runner;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.bookmark.SavedLink;
import org.jobsui.core.job.*;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UnsupportedComponentException;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by enrico on 3/11/17.
 */
public class JobUIRunnerContext<T extends Serializable, C> implements JobDependencyProvider {
    private static final Logger LOGGER = Logger.getLogger(JobUIRunnerContext.class.getName());
    private final UI<C> ui;
    private final Project project;
    private final Job<T> job;
    private final Map<String,UIWidget<C>> widgets;
    private final List<JobDependency> sortedJobDependencies;
    private final DependenciesObservables dependenciesObservables;

    private JobUIRunnerContext(UI<C> ui, Project project, Job<T> job, Map<String, UIWidget<C>> widgets,
                              List<JobDependency> sortedJobDependencies,
                              DependenciesObservables dependenciesObservables) {
        this.ui = ui;
        this.project = project;
        this.job = job;
        this.widgets = widgets;
        this.sortedJobDependencies = sortedJobDependencies;
        this.dependenciesObservables = dependenciesObservables;
    }

    public static <T extends Serializable, C> JobUIRunnerContext<T,C> of(Project project, Job<T> job, UI<C> ui) throws Exception {
        LOGGER.fine("Creating Job runner context");

        Map<String,UIWidget<C>> widgets = new HashMap<>();
        for (final JobParameter jobParameter : job.getParameterDefs()) {
            UIWidget<C> widget = createWidget(ui, jobParameter);
            widget.setDisable(!jobParameter.getDependencies().isEmpty());
            widgets.put(jobParameter.getKey(), widget);
        }

        List<JobDependency> sortedDependencies = job.getSortedDependencies();

        List<String> sortedDependenciesKeys = sortedDependencies.stream()
                .map(JobDependency::getKey)
                .collect(Collectors.toList());
        DependenciesObservables dependenciesObservables = getDependenciesObservables(job, widgets, sortedDependenciesKeys);

        LOGGER.fine("Created Job runner context");

        return new JobUIRunnerContext<>(ui, project, job, widgets, sortedDependencies, dependenciesObservables);
    }

    public Job<T> getJob() {
        return job;
    }

    public UI<C> getUi() {
        return ui;
    }

    List<JobDependency> getSortedJobDependencies() {
        return sortedJobDependencies;
    }

    DependenciesObservables getDependenciesObservables() {
        return dependenciesObservables;
    }

    void disableDependants(JobDependency jobDependency) {
        for (JobDependency dependant : getDependants(jobDependency)) {
            if (dependant instanceof JobParameter) {
                getWidget((JobParameter) dependant).setDisable(true);
            }
            disableDependants(dependant);
        }
    }

    void reEnableDependants(Map<String, Serializable> validValues, JobDependency jobDependency) {
        for (JobDependency dependant : getDependants(jobDependency)) {
            Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, dependant);
            if (dependenciesValues.size() == dependant.getDependencies().size()) {
                if (dependant instanceof JobParameter) {
                    getWidget((JobParameter) dependant).setDisable(false);
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
    Observable<JobsUIValidationResult> jobValidationObserver() {
        List<Observable<Serializable>> observables = getDependenciesObservables().getList();

        return Observable.combineLatest(observables, args -> {
            int i = 0;

            JobsUIValidationResultImpl jobValidation = new JobsUIValidationResultImpl();

            Map<String, Serializable> values = new HashMap<>();

            for (JobDependency jobDependency : sortedJobDependencies) {
                Serializable value = (Serializable) args[i++];
                if (jobDependency instanceof JobParameterValidator) {
                    JobParameterValidator jobParameterValidator = (JobParameterValidator) jobDependency;
                    if (!isValid(jobParameterValidator, values, value)) {
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
    Observable<Map<String,Serializable>> valuesChangeObserver() {
        List<Observable<Serializable>> observables = dependenciesObservables.getList();

        return Observable.combineLatest(observables, args -> {
            int i = 0;

            Map<String, Serializable> values = new HashMap<>();

            for (JobDependency jobDependency : sortedJobDependencies) {
                Serializable value = (Serializable) args[i++];
                values.put(jobDependency.getKey(), value);
            }

            for (final Map.Entry<String,UIWidget<C>> entry : widgets.entrySet()) {
                JobParameter jobParameter = job.getParameter(entry.getKey());
                Serializable value = values.get(jobParameter.getKey());

                Map<String, Serializable> dependenciesValues = getDependenciesValues(values, jobParameter);

                if (!isValid(jobParameter, dependenciesValues, value)) {
                    values.remove(jobParameter.getKey());
                }
            }

            return values;
        });
    }

    static boolean isValid(JobParameterValidator jobParameterValidator, Map<String, Serializable> dependenciesValues, Serializable value) {
        final List<String> validate = jobParameterValidator.validate(dependenciesValues, value);

        return validate.isEmpty();
    }

    static Map<String, Serializable> getDependenciesValues(Map<String, Serializable> values,
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

    Observable<Map<String, Serializable>> combineDependenciesObservables(
            List<String> dependencies,
            Collection<Observable<Serializable>> observables,
            Map<String, Serializable> validValues)
    {
        return Observable.combineLatest(observables, args -> {
            Map<String, Serializable> result = new HashMap<>();

            int i = 0;
            for (String dependency : dependencies) {
                final Serializable arg = (Serializable) args[i++];
                JobParameter jobParameter = job.getParameter(dependency);
                if (jobParameter != null) {
                    Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, jobParameter);
                    if (dependenciesValues.size() == jobParameter.getDependencies().size() &&
                            jobParameter.validate(dependenciesValues, arg).isEmpty()) {
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

    Observable<ChangedValue> valueChangeObserver() {
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
                if (jobDependency instanceof JobParameterValidator) {
                    JobParameterValidator jobParameterValidator = (JobParameterValidator) jobDependency;
                    Map<String, Serializable> dependenciesValues = getDependenciesValues(validValues, jobDependency);
                    if (dependenciesValues.size() == jobDependency.getDependencies().size()) {
                        validation = jobParameterValidator.validate(dependenciesValues, value);
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

    UIWidget<C> getWidget(JobParameter jobParameter) {
        return widgets.get(jobParameter.getKey());
    }

    DependenciesObservables getDependenciesObservables(List<String> dependencies) {
        return getDependenciesObservables(job, widgets, dependencies);
    }

    private static <T extends Serializable, C> DependenciesObservables getDependenciesObservables(
            Job<T> job,
            Map<String,UIWidget<C>> widgets,
            List<String> dependencies)
    {
        DependenciesObservablesImpl result = new DependenciesObservablesImpl();

        for (String dependency : dependencies) {
            JobParameter jobParameter = job.getParameter(dependency);
            if (jobParameter != null) {
                final UIWidget widget = widgets.get(jobParameter.getKey());
                if (widget == null) {
                    throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                            dependency + "\".");
                }
                result.add(jobParameter, widget.getComponent().getObservable());
            } else {
                JobExpression jobExpression = job.getExpression(dependency);
                result.add(jobExpression, jobExpression.getObservable());
            }
        }
        return result;
    }

    public JobDependency getJobDependency(String key) {
        return job.getJobDependency(key);
    }

    public Project getProject() {
        return project;
    }

    Serializable transformValue(Serializable value) {
        Serializable transformedValues;
        if (value instanceof SavedLink) {
            SavedLink savedLink = (SavedLink) value;
            Job<Object> job = project.getJob(savedLink.getJobId());
            Map<String, Bookmark> bookmarks = ui.getPreferences().getBookmarksStore()
                    .getBookmarks(project, job);
            Bookmark bookmark = bookmarks.get(savedLink.getId());
            transformedValues = (Serializable) bookmark.getValues();
        } else {
            transformedValues = value;
        }
        return transformedValues;
    }

    Map<String, Serializable> transformValues(Map<String, Serializable> values) {
        Map<String, Serializable> transformedValues = new HashMap<>(values.size());
        values.forEach((key, value) -> transformedValues.put(key, transformValue(value)));
        return transformedValues;
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

    private static <C> UIWidget<C> createWidget(final UI<C> ui,
                                                final JobParameter jobParameter)
    throws UnsupportedComponentException {
        final UIComponent<C> component = jobParameter.createComponent(ui);
        if (component == null) {
            throw new IllegalStateException("Cannot create component for parameter with key \""
                    + jobParameter.getKey() + "\"");
        }

        final UIWidget<C> widget = ui.createWidget(jobParameter.getName(), component);
        if (widget == null) {
            throw new IllegalStateException("Cannot create widget for parameter with key \""
                    + jobParameter.getKey() + "\"");
        }

        widget.setVisible(jobParameter.isVisible());

//        final Observable<Serializable> observable = widget.getComponent().getObservable();
//
//        observable.subscribe(o -> setValidationMessage(jobParameter.validate(, o), jobParameter, widget, ui));
        return widget;
    }

}
