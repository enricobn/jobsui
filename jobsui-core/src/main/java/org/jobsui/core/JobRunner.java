package org.jobsui.core;

import org.jobsui.core.ui.*;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.functions.FuncN;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/29/16.
 */
class JobRunner {
    private boolean valid = false;

    private static class ParameterAndWidget<T extends Serializable, C> {
        private final JobParameterDef<T> jobParameterDef;
        private final UIWidget<T, C> widget;

        private ParameterAndWidget(JobParameterDef<T> jobParameterDef, UIWidget<T, C> widget) {
            this.jobParameterDef = jobParameterDef;
            this.widget = widget;
        }

        UIWidget<T, C> getWidget() {
            return widget;
        }

        JobParameterDef<T> getJobParameterDef() {
            return jobParameterDef;
        }
    }

    private static class WidgetsMap<C> {
        private final Collection<ParameterAndWidget<Serializable, C>> widgets;

        private WidgetsMap(Collection<ParameterAndWidget<Serializable, C>> widgets) {
            this.widgets = widgets;
        }

        UIWidget<?, C> get(JobParameterDef<?> jobParameterDef) {
            for (ParameterAndWidget<?, C> widget : widgets) {
                if (jobParameterDef.equals(widget.getJobParameterDef())) {
                    return widget.getWidget();
                }
            }
            return null;
        }

        Collection<ParameterAndWidget<Serializable, C>> getWidgets() {
            return widgets;
        }
    }

    public void setValues(JobValues values) {
        // TODO
    }

    public <T extends Serializable, C> T run(final UI<C> ui, final Job<T> job) throws UnsupportedComponentException {
        valid = false;

        final UIWindow<C> window = ui.createWindow(job.getName());

        final JobValues values = new JobValuesImpl();

        List<UnsupportedComponentException> exceptions = new ArrayList<>();

        AtomicReference<T> result = new AtomicReference<T>(null);

        window.show(() -> {
            final WidgetsMap<C> widgets;
            try {
                widgets = createWidgets(ui, job, window);
            } catch (UnsupportedComponentException e) {
                exceptions.add(e);
                return;
            }

            observeDependencies(job, widgets);

            UIButton<C> runButton;
            try {
                runButton = ui.create(UIButton.class);
            } catch (UnsupportedComponentException e) {
                // TODO
                throw new RuntimeException(e);
            }

            runButton.setEnabled(false);

            runButton.setTitle("Run");

            runButton.getObservable().subscribe(serializableVoid -> {
                try {
                    JobFuture<T> resultFuture = job.run(values);
                    result.set(resultFuture.get());
                } catch (Exception e) {
                    ui.showError("Error running job.", e);
                }
            });

//            Observable<Map<String, Object>> mapObservable = observeValues(ui, job, window, runButton, widgets);
//
//            mapObservable.subscribe(map -> {
//                values.clear();
//                for (JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
//                    setValue(values, map, jobParameterDef);
//                }
//            });

//            window.setValid(false);

            Observable<JobValidation> validationObserver = validationObserver(job, widgets);

            validationObserver.subscribe(v -> {
                valid = v.isValid();
                runButton.setEnabled(v.isValid());
                window.showValidationMessage(String.join(", ", v.getMessages()));
            });

            Observable<Map<String, Serializable>> valuesChangeObserver = valuesChangeObserver(widgets);

            valuesChangeObserver.subscribe(map -> {
                values.clear();
                for (JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
                    setValue(values, map, jobParameterDef);
                }
            });

            notifyInitialValue(widgets);

            window.add(runButton);

        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
        return result.get();
    }

    private static <T extends Serializable> void setValue(JobValues values, Map<String, Serializable> map, JobParameterDef<T> jobParameterDef) {
        values.setValue(jobParameterDef, (T)map.get(jobParameterDef.getKey()));
    }

//    public <T extends Serializable, C> JobFuture<T> run(final UI<C> ui, final Job<T> job) throws UnsupportedComponentException {
//        final UIWindow<C> window = ui.createWindow(job.getName());
//
//        final Map<String, Object> values = new HashMap<>();
//
//        List<UnsupportedComponentException> exceptions = new ArrayList<>();
//
//        if (window.show(() -> {
//            final WidgetsMap<C> widgets;
//            try {
//                widgets = createWidgets(ui, job, window);
//            } catch (UnsupportedComponentException e) {
//                exceptions.add(e);
//                return;
//            }
//
//            observeDependencies(job, widgets);
//
//            Observable<Map<String, Object>> mapObservable = observeValues(ui, job, window, widgets);
//            mapObservable.subscribe(map -> {
//                values.clear();
//                values.putAll(map);
//            });
//
//            window.setValid(false);
//
//            notifyInitialValue(widgets);
//        })) {
//            if (!exceptions.isEmpty()) {
//                throw exceptions.get(0);
//            }
//            return job.run(values);
//        }
//        return null;
//    }

    private <T extends Serializable, C> WidgetsMap<C> createWidgets(final UI<C> ui, Job<T> job, UIWindow<C> window)
    throws UnsupportedComponentException {
        Collection<ParameterAndWidget<Serializable, C>> result = new ArrayList<>();

        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            createWidget(ui, window, result, jobParameterDef);
        }
        return new WidgetsMap<>(result);
    }

    private <T extends Serializable, C> void createWidget(final UI<C> ui, UIWindow<C> window,
                                                          Collection<ParameterAndWidget<Serializable, C>> result,
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

        result.add(new ParameterAndWidget(jobParameterDef, widget));

        final Observable<T> observable = widget.getComponent().getObservable();

        observable.subscribe(o -> {
            setValidationMessage(jobParameterDef.validate(o), jobParameterDef, widget, ui);
        });
    }

    private <C> void notifyInitialValue(WidgetsMap<C> widgets) {
        widgets.getWidgets().forEach(this::notifyInitialValue);
    }

    private <T extends Serializable, C> void notifyInitialValue(ParameterAndWidget<T, C> entry) {
        final T value = entry.getWidget().getComponent().getValue();
        if (entry.getJobParameterDef().validate(value).isEmpty()) {
            entry.getWidget().getComponent().notifySubscribers();
        }
    }

    private static class JobValidation {
        private boolean valid = true;
        private List<String> messages = new ArrayList<>();

        void invalidate() {
            valid = false;
        }

        void setMessages(List<String> messages) {
            this.messages = messages;
        }

        boolean isValid() {
            return valid && messages.isEmpty();
        }

        List<String> getMessages() {
            return Collections.unmodifiableList(messages);
        }
    }

    /**
     * Creates an Observable that emits a JobValidation, with the validation status of the job, when all values are set or
     * a value is changed.
     */
    private <T, C> Observable<JobValidation> validationObserver(final Job<T> job,
                                                          final WidgetsMap<C> widgetsMap) {
        List<Observable<?>> observables = widgetsMap.getWidgets().stream()
                .map(widget -> widget.getWidget().getComponent().getObservable())
                .collect(Collectors.toList());

        return Observable.combineLatest(observables, new FuncN<JobValidation>() {
            @Override
            public JobValidation call(Object... args) {
                int i = 0;

                JobValidation jobValidation = new JobValidation();

                Map<String, Serializable> values = new HashMap<>();

                for (final ParameterAndWidget<Serializable, C> entry : widgetsMap.getWidgets()) {
                    Serializable value = (Serializable) args[i++];
                    if (!isValid(entry, value)) {
                        jobValidation.invalidate();
                        break;
                    }
                    values.put(entry.jobParameterDef.getKey(), value);
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
    private <C> Observable<Map<String,Serializable>> valuesChangeObserver(final WidgetsMap<C> widgetsMap) {
        List<Observable<?>> observables = widgetsMap.getWidgets().stream()
                .map(widget -> widget.getWidget().getComponent().getObservable())
                .collect(Collectors.toList());

        return Observable.combineLatest(observables, new FuncN<Map<String,Serializable>>() {
            @Override
            public Map<String,Serializable> call(Object... args) {
                int i = 0;

                Map<String, Serializable> values = new HashMap<>();

                for (final ParameterAndWidget<Serializable, C> entry : widgetsMap.getWidgets()) {
                    Serializable value = (Serializable) args[i++];
                    if (isValid(entry, value)) {
                        values.put(entry.jobParameterDef.getKey(), value);
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


//    private <T, C> Observable<Map<String, Object>> observeValues(final UI<?> ui, final Job<T> job, final UIWindow<?> window,
//                                                                 final UIButton<C> runButton,
//                                                                 final WidgetsMap<C> widgetsMap) {
//        final List<Subscriber<? super Map<String, Object>>> subscribers = new ArrayList<>();
//        Observable<Map<String, Object>> result = Observable.create(subscribers::add);
//
//        final Map<String,Object> values = new HashMap<>();
//
//        List<Observable<?>> observables = widgetsMap.getWidgets().stream()
//                .map(widget -> widget.getWidget().getComponent().getObservable())
//                .collect(Collectors.toList());
//
//        Observable<Boolean> combined = Observable.combineLatest(observables, new FuncN<Boolean>() {
//            @Override
//            public Boolean call(Object... args) {
//                values.clear();
//                subscribers.forEach(subscriber -> subscriber.onNext(values));
//
//                int i = 0;
//
//                for (final ParameterAndWidget<Serializable, C> entry : widgetsMap.getWidgets()) {
//                    if (addValidValue(entry, (Serializable) args[i++])) break;
//                }
//
//                if (values.size() != args.length) {
//                    return false;
//                }
//
//                final List<String> validate = job.validate(values);
//                if (!validate.isEmpty()) {
//                    window.showValidationMessage(JobsUIUtils.getMessagesAsString(validate));
//                } else {
//                    window.showValidationMessage(null);
//                }
//                return validate.isEmpty();
//            }
//
//            private boolean addValidValue(ParameterAndWidget<Serializable, C> entry, Serializable value) {
//                final JobParameterDef<Serializable> parameterDef = entry.getJobParameterDef();
//                final List<String> validate = parameterDef.validate(value);
//
//                setValidationMessage(validate, parameterDef, entry.getWidget(), ui);
//
//                if (!validate.isEmpty()) {
//                    return true;
//                }
//                values.put(parameterDef.getKey(), value);
//                subscribers.forEach(subscriber -> subscriber.onNext(values));
//                return false;
//            }
//        });
//
////        combined.subscribe(window::setValid);
////        combined.subscribe(runButton::setEnabled);
//        combined.subscribe(v -> {
//            valid = v;
//            runButton.setEnabled(v);
//        });
//        return result;
//    }

    public boolean isValid() {
        return valid;
    }

    private <T extends Serializable, C> void observeDependencies(Job<T> job, final WidgetsMap<C> widgets) {
        for (final JobParameterDef<? extends Serializable> jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef<? extends Serializable>> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<Serializable>> observables = getDependenciesObservables(widgets, dependencies);

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

            private <T extends Serializable> boolean addValidatedValue(Map<String, Serializable> result, JobParameterDef<T> dependency, T arg) {
                final List<String> validate = dependency.validate(arg);
                if (!validate.isEmpty()) {
                    return true;
                }
                result.put(dependency.getKey(), arg);
                return false;
            }
        });
    }

    private <C> List<Observable<Serializable>> getDependenciesObservables(WidgetsMap<C> widgetsMap,
                                                           List<JobParameterDef<? extends Serializable>> dependencies) {
        List<Observable<Serializable>> observables = new ArrayList<>();
        for (JobParameterDef dependency : dependencies) {
            final UIWidget widget = widgetsMap.get(dependency);
            if (widget == null) {
                throw new IllegalStateException("Cannot find widget for dependency with key \"" +
                        dependency.getKey() + "\".");
            }
            observables.add(widget.getComponent().getObservable());
        }
        return observables;
    }

    private <T extends Serializable> void setValidationMessage(List<String> validate, JobParameterDef<T> jobParameterDef,
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
