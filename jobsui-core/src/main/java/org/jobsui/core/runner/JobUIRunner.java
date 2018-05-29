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
    private Bookmark activeBookmark;

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
                 context = JobUIRunnerContext.of(project, job, ui, window);
            } catch (Exception e) {
                throw new RuntimeException((e));
//                exceptions.add(e);
//                return;
            }

            if (!job.getWizardSteps().isEmpty()) {
                WizardState wizardState;
                try {
                    wizardState = new WizardState(job);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                UIButton<C> nextButton = ui.createButton();
                nextButton.setTitle("Next");

                UIButton<C> previousButton = ui.createButton();
                previousButton.setTitle("Back");
                previousButton.setEnabled(false);

                nextButton.getObservable().subscribe(serializable -> {
                    wizardState.next(context, window);
                    nextButton.setEnabled(wizardState.hasNext());
                    previousButton.setEnabled(wizardState.hasPrevious());
                });

                previousButton.getObservable().subscribe(serializable -> {
                    wizardState.previous(context, window);
                    nextButton.setEnabled(wizardState.hasNext());
                    previousButton.setEnabled(wizardState.hasPrevious());
                });

                window.addButton(previousButton);
                window.addButton(nextButton);

                wizardState.updateWindow(context, window);

            } else {
                for (JobParameter jobParameter : job.getParameterDefs()) {
                    UIWidget<C> widget = context.getWidget(jobParameter);
                    window.add(widget);
                }
            }

            try {
                observeDependencies(ui, context, project);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            UIButton<C> runButton = createRunButton(context, values, atomicResult);

            UIButton<C> saveAsButton = createSaveAsButton(project, job, window, values);
            UIButton<C> saveButton = createSaveButton(project, job, window, values);
            saveButton.setEnabled(false);

            Observable<JobsUIValidationResult> validationObserver = context.jobValidationObserver();

            validationObserver.subscribe(v -> {
                valid = v.isValid();
                runButton.setEnabled(v.isValid());
                saveAsButton.setEnabled(v.isValid());
                saveButton.setEnabled(v.isValid() && activeBookmark != null);
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

            window.addButton(saveButton);

            window.addButton(saveAsButton);

            window.setOnOpenBookmark(bookmark -> {
                try {
                    for (JobDependency jobDependency : job.getSortedDependencies()) {
                        if (jobDependency instanceof JobParameter) {
                            JobParameter jobParameter = (JobParameter) jobDependency;
                            Serializable value = bookmark.getValues().get(jobParameter.getKey());

                            List<String> validate = jobParameter.validate(bookmark.getValues(), value);

                            if (validate.isEmpty()) {
                                try {
                                    context.getWidget(jobParameter).getComponent().setValue(value);
                                } catch (Exception e) {
                                    throw new RuntimeException("Error setting value for parameter with key '" + jobDependency.getKey() + "'.", e);
                                }
                            } else {
                                ui.showMessage(String.format("Value '%s' for parameter '%s' is not valid:\n%s",
                                        value, jobParameter.getName(), String.join(",", validate)));
                                break;
                            }
                            this.activeBookmark = bookmark;
                            window.setTitle(bookmark.getName());
                            saveButton.setEnabled(true);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            window.setOnDeleteBookmark(bookmark -> {
                if (activeBookmark != null && bookmark.getKey().equals(activeBookmark.getKey())) {
                    activeBookmark = null;
                    window.setTitle(null);
                    saveButton.setEnabled(false);
                }
            });

//            window.add(closeButton);
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
        return atomicResult.get();
    }

    private <T extends Serializable> UIButton<C> createSaveAsButton(Project project, Job<T> job, UIWindow<C> window, JobValues values) {
        UIButton<C> button = ui.createButton();
        button.setEnabled(false);
        button.setTitle("Save as");

        button.getObservable().subscribe(serializableVoid -> {
            Optional<String> name = ui.askString("Name");
            name.ifPresent(n -> {
                JobsUIPreferences preferences = ui.getPreferences();

                boolean ok = true;
                if (preferences.existsBookmark(project, job, n)) {
                    ok = ui.askOKCancel("A bookmark with the same name exists. Do you want to override it?");
                }

                if (ok) {
                    try {
                        Bookmark bookmark = new Bookmark(project, job, UUID.randomUUID().toString(), n, values);
                        preferences.saveBookmark(project, job, bookmark);
                        this.activeBookmark = bookmark;
                        window.refreshBookmarks(project, job, activeBookmark);
                        window.setTitle(bookmark.getName());
                    } catch (Exception e) {
                        ui.showError("Error saving bookmark.", e);
                    }
                }
            });
        });
        return button;
    }

    private <T extends Serializable> UIButton<C> createSaveButton(Project project, Job<T> job, UIWindow<C> window, JobValues values) {
        UIButton<C> button = ui.createButton();
        button.setEnabled(false);
        button.setTitle("Save");

        button.getObservable().subscribe(serializableVoid -> {
                JobsUIPreferences preferences = ui.getPreferences();

                try {
                    activeBookmark.getValues().clear();
                    activeBookmark.getValues().putAll(values.getMap(job));
                    preferences.saveBookmark(project, job, activeBookmark);
                    window.refreshBookmarks(project, job, activeBookmark);
                } catch (Exception e) {
                    ui.showError("Error saving bookmark.", e);
                }
            });
        return button;
    }


    private <T extends Serializable> UIButton<C> createRunButton(JobUIRunnerContext<T, C> context, JobValues values, AtomicReference<T> atomicResult) {
        UIButton<C> runButton = ui.createButton();
        runButton.setEnabled(false);
        runButton.setTitle("Run");

        runButton.getObservable().subscribe(serializableVoid -> {
            try {
                JobResult<T> result = context.getJob().run(context.transformValues(values.getMap(context.getJob())));
                if (result.getException() != null) {
                    ui.showError("Error running job.", result.getException());
                } else {
                    atomicResult.set(result.get());
                }
            } catch (Exception e) {
                ui.showError("Error running job.", e);
            }
        });
        return runButton;
    }

    private void setValidationMessage(List<String> validate, JobParameter jobParameter, UIWidget<?> widget) {
        if (!jobParameter.isVisible()) {
            if (!validate.isEmpty()) {
                ui.showMessage(jobParameter.getName() + ": " + JobsUIUtils.getMessagesAsString(validate));
            }
        } else {
            widget.setValidationMessages(validate);
        }
    }

    private <T extends Serializable> void setComponentValidationMessage(JobUIRunnerContext<T, C> context) {
        Map<String, Observable<Serializable>> observables = context.getDependenciesObservables().getMap();

        Map<String, Serializable> validValues = new HashMap<>();

        for (Map.Entry<String, Observable<Serializable>> entry : observables.entrySet()) {
            entry.getValue().subscribe(value -> {
                JobDependency jobDependency = context.getJob().getJobDependency(entry.getKey());
                if (jobDependency instanceof JobParameter) {
                    JobParameter jobParameter = (JobParameter) jobDependency;
                    UIWidget<C> widget = context.getWidget(jobParameter);

                    // I set the validation message only if all dependencies are valid
                    Map<String, Serializable> dependenciesValues = JobUIRunnerContext.getDependenciesValues(validValues,
                            jobParameter);
                    if (dependenciesValues.size() == jobParameter.getDependencies().size()) {
                        Map<String, Serializable> transformedValues = context.transformValues(validValues);
                        List<String> validate = jobParameter.validate(transformedValues, value);
                        if (validate.isEmpty()) {
                            validValues.put(jobDependency.getKey(), value);
                        } else {
                            validValues.remove(jobDependency.getKey());
                        }
                        setValidationMessage(validate, jobParameter, widget);
                    } else {
                        setValidationMessage(Collections.emptyList(), jobParameter, widget);
                    }
                }
            });
        }
    }

    private static <T extends Serializable, C> void observeDependencies(UI<C> ui, JobUIRunnerContext<T, C> context,
                                                                        Project project) throws Exception {
        Map<String, Serializable> validValues = new HashMap<>();

        context.valueChangeObserver().subscribe(changedValue -> {
            validValues.clear();
            validValues.putAll(changedValue.validValues);
        });

        for (final JobDependency jobDependency : context.getSortedJobDependencies()) {
            final List<String> dependencies = jobDependency.getSortedDependencies(context);
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = context.getDependenciesObservables(dependencies).getList();

                final Observable<Map<String, Serializable>> observable =
                        context.combineDependenciesObservables(dependencies, observables, validValues);

                observable.subscribe(values -> {
                    // all dependencies are valid
                    if (values.size() == dependencies.size()) {
                        Map<String, Serializable> transormedValues = context.transformValues(values);

                        if (jobDependency instanceof JobParameter) {
                            JobParameter jobParameter = (JobParameter) jobDependency;
                            final UIWidget widget = context.getWidget(jobParameter);
                            widget.setDisable(false);
                            context.reEnableDependants(validValues, jobDependency);
                            try {
                                jobParameter.onDependenciesChange(widget, transormedValues);

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
                            Serializable value = jobExpression.evaluate(transormedValues);
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
                Serializable value = context.transformValue(component.getValue());
                if (JobUIRunnerContext.isValid(jobParameter, values, value)) {
//                    component.setValue(value);
                    values.put(jobDependency.getKey(), context.transformValue(value));
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
