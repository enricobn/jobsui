package org.jobsui.core.runner;

import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.*;
import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by enrico on 4/29/16.
 */
public class JobUIRunner<C> implements JobRunner {
    private final UI<C> ui;
    private boolean valid = false;

    public JobUIRunner(UI<C> ui) {
        this.ui = ui;
    }

    @Override
    public <T extends Serializable> T run(Project project, final Job<T> job) throws Exception {
        valid = false;

        final UIWindow<C> window = ui.createWindow(job.getName());

        final JobValues values = new JobValuesImpl();

        List<Exception> exceptions = new ArrayList<>();

        AtomicReference<T> atomicResult = new AtomicReference<>(null);

        window.show(project, job, () -> {

            JobUIRunnerContext<T,C> context;

            try {
                 context = JobUIRunnerContext.of(job, ui, window);
            } catch (Exception e) {
                throw new RuntimeException((e));
//                exceptions.add(e);
//                return;
            }

            for (JobParameter jobParameter : job.getParameterDefs()) {
                UIWidget<C> widget = context.getWidget(jobParameter);
                window.add(widget);
            }

            observeDependencies(ui, context);

            UIButton<C> runButton;
            UIButton<C> saveBookmarkButton;
//            UIButton<C> closeButton;
            try {
                runButton = ui.create(UIButton.class);
                saveBookmarkButton = ui.create(UIButton.class);
//                closeButton = ui.create(UIButton.class);
            } catch (UnsupportedComponentException e) {
                // TODO
                throw new RuntimeException(e);
            }

            runButton.setEnabled(false);
            runButton.setTitle("Run");

            runButton.getObservable().subscribe(serializableVoid -> {
                try {
                    JobResult<T> result = job.run(values);
                    if (result.getException() != null) {
                        ui.showError("Error running job.", result.getException());
                    } else {
                        atomicResult.set(result.get());
                    }
                } catch (Exception e) {
                    ui.showError("Error running job.", e);
                }
            });

            saveBookmarkButton.setEnabled(false);
            saveBookmarkButton.setTitle("Bookmark");

            saveBookmarkButton.getObservable().subscribe(serializableVoid -> {
                Optional<String> name = ui.askString("Bookmark's name");
                name.ifPresent(n -> {
                    JobsUIPreferences preferences = ui.getPreferences();

                    boolean ok = true;
                    if (preferences.existsBookmark(project, job, n)) {
                        ok = ui.askOKCancel("A bookmark with the same name exists. Do you want to override it?");
                    }

                    if (ok) {
                        try {
                            Bookmark bookmark = new Bookmark(project, job, n, values);
                            preferences.saveBookmark(project, job, bookmark);
                            window.refreshBookmarks(project, job);
                        } catch (Exception e) {
                            ui.showError("Error saving bookmark.", e);
                        }
                    }
                });
            });

//            closeButton.setTitle("Close");
//            closeButton.getObservable().subscribe(serializableVoid -> {
//                ui.gotoStart();
//            });

            Observable<JobValidation> validationObserver = context.jobValidationObserver();

            validationObserver.subscribe(v -> {
                valid = v.isValid();
                runButton.setEnabled(v.isValid());
                saveBookmarkButton.setEnabled(v.isValid());
                window.showValidationMessage(String.join(", ", v.getMessages()));
            });

            Observable<Map<String, Serializable>> valuesChangeObserver = context.valuesChangeObserver();

            valuesChangeObserver.subscribe(map -> {
                values.clear();
                for (JobDependency jobDependency : job.getUnsortedDependencies()) {
                    setValue(values, map, jobDependency);
                }
            });

            setComponentValidationMessage(context);

            notifyInitialValue(context);

            window.addButton(runButton);
            window.addButton(saveBookmarkButton);
            window.setOnOpenBookmark(bookmark -> {
                try {
                    for (JobDependency jobDependency : job.getSortedDependencies()) {
                        if (jobDependency instanceof JobParameter) {
                            JobParameter jobParameter = (JobParameter) jobDependency;
                            Serializable value = bookmark.getValues().get(jobParameter.getKey());

                            List<String> validate = jobParameter.validate(bookmark.getValues(), value);

                            if (validate.isEmpty()) {
                                context.getWidget(jobParameter).getComponent().setValue(value);
                            } else {
                                ui.showMessage(String.format("Value '%s' for parameter '%s' is not valid:\n%s",
                                        value, jobParameter.getName(), String.join(",", validate)));
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
//            window.add(closeButton);
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
        return atomicResult.get();
    }

    private static void setValidationMessage(List<String> validate, JobParameter jobParameter, UIWidget<?> widget,
                                             UI<?> ui) {
        if (!jobParameter.isVisible()) {
            if (!validate.isEmpty()) {
                ui.showMessage(jobParameter.getName() + ": " + JobsUIUtils.getMessagesAsString(validate));
            }
        } else {
            widget.setValidationMessages(validate);
        }
    }

    private static <T extends Serializable, C> void setComponentValidationMessage(JobUIRunnerContext<T, C> context) {
        Map<String, Observable<Serializable>> observables = context.getDependenciesObservables().getMap();

        Map<String, Serializable> validValues = new HashMap<>();

        for (Map.Entry<String, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                JobDependency jobDependency = context.getJob().getJobDependency(entry.getKey());
                if (jobDependency instanceof JobParameter) {
                    JobParameter jobParameter = (JobParameter) jobDependency;
                    UIWidget<C> widget = context.getWidget(jobParameter);

                    // I set the validation message only if all dependencies are valid
                    if (JobUIRunnerContext.getDependenciesValues(validValues, jobParameter).size() == jobParameter.getDependencies().size()) {
                        List<String> validate = jobParameter.validate(validValues, value);
                        if (validate.isEmpty()) {
                            validValues.put(jobDependency.getKey(), value);
                        } else {
                            validValues.remove(jobDependency.getKey());
                        }
                        setValidationMessage(validate, jobParameter, widget, context.getUi());
                    } else {
                        setValidationMessage(Collections.emptyList(), jobParameter, widget, context.getUi());
                    }
                }
            });
        }
    }

    private static <T extends Serializable, C> void observeDependencies(UI<C> ui, JobUIRunnerContext<T, C> context) {
        Map<String, Serializable> validValues = new HashMap<>();

        context.valueChangeObserver().subscribe(changedValue -> {
            validValues.clear();
            validValues.putAll(changedValue.validValues);
        });

        for (final JobDependency jobDependency : context.getSortedJobDependencies()) {
            final List<String> dependencies = jobDependency.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = context.getDependenciesObservables(dependencies).getList();

                final Observable<Map<String, Serializable>> observable =
                        context.combineDependenciesObservables(dependencies, observables, validValues);

                observable.subscribe(objects -> {
                    // all dependencies are valid
                    if (objects.size() == dependencies.size()) {
                        if (jobDependency instanceof JobParameter) {
                            JobParameter jobParameter = (JobParameter) jobDependency;
                            final UIWidget widget = context.getWidget(jobParameter);
                            widget.setDisable(false);
                            context.reEnableDependants(validValues, jobDependency);
                            try {
                                jobParameter.onDependenciesChange(widget, objects);
                            } catch (Exception e) {
                                ui.showError("Error on onDependenciesChange for parameter " + jobParameter.getName(), e);
                                widget.setValidationMessages(Collections.singletonList(e.getMessage()));
                                widget.getComponent().setValue(null);
                                widget.setDisable(true);
                                context.disableDependants(jobDependency);
                            }
                        } else if (jobDependency instanceof JobExpression) {
                            JobExpression jobExpression = (JobExpression) jobDependency;
                            //jobExpression.onDependenciesChange(objects);
                            Serializable value = jobExpression.evaluate(objects);
                            jobExpression.notifySubscribers(value);
                            context.reEnableDependants(validValues, jobDependency);
                        } else {
                            throw new IllegalStateException("Unknown type " + jobDependency.getClass());
                        }
                    } else {
                        if (jobDependency instanceof JobParameter) {
                            JobParameter jobParameter = (JobParameter) jobDependency;
                            final UIWidget widget = context.getWidget(jobParameter);
                            widget.setDisable(true);
                        }
                        context.disableDependants(jobDependency);
                    }
                });
            }
        }
    }

    private static <T extends Serializable, C> void notifyInitialValue(JobUIRunnerContext<T, C> context) {
        Map<String,Serializable> values = new HashMap<>();
        for (JobDependency jobDependency : context.getSortedJobDependencies()) {
            if (jobDependency instanceof JobExpression) {
                JobExpression jobExpression = (JobExpression) jobDependency;
                if (jobExpression.getDependencies().isEmpty()) {
                    Serializable value = jobExpression.evaluate(values);
                    values.put(jobDependency.getKey(), value);
                    jobExpression.notifySubscribers(value);
                }
            } else if (jobDependency instanceof JobParameter) {
                JobParameter jobParameter = (JobParameter) jobDependency;
                UIComponent<C> component = context.getWidget(jobParameter).getComponent();
                Serializable value = component.getValue();
                if (JobUIRunnerContext.isValid(jobParameter, values, value)) {
//                    component.setValue(value);
                    values.put(jobDependency.getKey(), value);
                    component.notifySubscribers();
                }
            } else {
                throw new IllegalStateException("Unexpected type " + jobDependency.getClass());
            }
        }
    }

    private static void setValue(JobValues values, Map<String, Serializable> map, JobDependency jobDependency) {
        values.setValue(jobDependency, map.get(jobDependency.getKey()));
    }

    public boolean isValid() {
        return valid;
    }


}
