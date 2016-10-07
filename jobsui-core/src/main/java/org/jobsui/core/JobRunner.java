package org.jobsui.core;

import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.FuncN;

import java.util.*;

/**
 * Created by enrico on 4/29/16.
 */
class JobRunner {

    private static class ParameterAndWidget<T, C> {
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
        private final Collection<ParameterAndWidget<?, C>> widgets;

        private WidgetsMap(Collection<ParameterAndWidget<?, C>> widgets) {
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

        Collection<ParameterAndWidget<?, C>> getWidgets() {
            return widgets;
        }
    }

    public <T, C> JobFuture<T> run(final UI<C> ui, final Job<T> job) throws UnsupportedComponentException {
        final UIWindow<C> window = ui.createWindow(job.getName());

        final WidgetsMap<C> widgets = createWidgets(ui, job, window);

        observeDependencies(job, widgets);

        final Map<String, Object> values = observeValues(ui, job, window, widgets);

        window.setValid(false);

        notifyInitialValue(widgets);

        if (window.show()) {
            return job.run(values);
        }

        return null;
    }

    private <T, C> WidgetsMap<C> createWidgets(final UI<C> ui, Job<T> job, UIWindow<C> window)
    throws UnsupportedComponentException {
        Collection<ParameterAndWidget<?, C>> result = new ArrayList<>();

        for (final JobParameterDef<?> jobParameterDef : job.getParameterDefs()) {
            crateWidget(ui, window, result, jobParameterDef);
        }
        return new WidgetsMap<>(result);
    }

    private <T, C> void crateWidget(final UI<C> ui, UIWindow<C> window, Collection<ParameterAndWidget<?, C>> result,
                                    final JobParameterDef<T> jobParameterDef) throws UnsupportedComponentException {
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

        result.add(new ParameterAndWidget<>(jobParameterDef, widget));

        final Observable<T> observable = widget.getComponent().getObservable();

        observable.subscribe(new Action1<T>() {
            @Override
            public void call(T o) {
                setValidationMessage(jobParameterDef.validate(o), jobParameterDef, widget, ui);
            }
        });
    }

    private <C> void notifyInitialValue(WidgetsMap<C> widgets) {
        for (ParameterAndWidget<?, C> entry : widgets.getWidgets()) {
            notifyInitialValue(entry);
        }
    }

    private <T, C> void notifyInitialValue(ParameterAndWidget<T, C> entry) {
        final T value = entry.getWidget().getComponent().getValue();
        if (entry.getJobParameterDef().validate(value).isEmpty()) {
            entry.getWidget().getComponent().notifySubscribers();
        }
    }

    private <T, C> Map<String, Object> observeValues(final UI<?> ui, final Job<T> job, final UIWindow<?> window,
                                                  final WidgetsMap<C> widgetsMap) {
        final Map<String,Object> parameters = new HashMap<>();

        List<Observable<?>> observables = new ArrayList<>();

        for (ParameterAndWidget<?, C> widget : widgetsMap.getWidgets()) {
            observables.add(widget.getWidget().getComponent().getObservable());
        }

        Observable<Boolean> combined = Observable.combineLatest(observables, new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                parameters.clear();

                int i = 0;

                for (final ParameterAndWidget<?, C> entry : widgetsMap.getWidgets()) {
                    final Object value = args[i++];
                    final JobParameterDef parameterDef = entry.getJobParameterDef();
                    final List<String> validate = parameterDef.validate(value);

                    setValidationMessage(validate, parameterDef, entry.getWidget(), ui);

                    if (!validate.isEmpty()) {
                        break;
                    }
                    parameters.put(parameterDef.getKey(), value);
                }

                if (parameters.size() != args.length) {
                    return false;
                }

                final List<String> validate = job.validate(parameters);
                if (!validate.isEmpty()) {
                    window.showValidationMessage(JobsUIUtils.getMessagesAsString(validate));
                }
                return validate.isEmpty();
            }
        });

        combined.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean valid) {
                window.setValid(valid);
            }
        });
        return parameters;
    }

    private <T, C> void observeDependencies(Job<T> job, final WidgetsMap<C> widgets) {
        for (final JobParameterDef<?> jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef<?>> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<?>> observables = getDependenciesObservables(widgets, dependencies);

                final Observable<Map<String, Object>> observable = combineDependeciesObservables(dependencies, observables);

                observable.subscribe(new Action1<Map<String,Object>>() {
                    @Override
                    public void call(Map<String,Object> objects) {
                        // all dependencies are valid
                        if (objects.size() == dependencies.size()) {
                            final UIWidget widget = widgets.get(jobParameterDef);
                            jobParameterDef.onDependenciesChange(widget, objects);
                        }
                    }
                });
            }
        }
    }

    private Observable<Map<String, Object>> combineDependeciesObservables(final List<JobParameterDef<?>> dependencies,
                                                                          List<Observable<?>> observables) {
        return Observable.combineLatest(observables, new FuncN<Map<String,Object>>() {
            @Override
            public Map<String,Object> call(Object... args) {
                Map<String, Object> result = new HashMap<>();

                int i = 0;
                for (JobParameterDef dependency : dependencies) {
                    final Object arg = args[i++];
                    if (addValidatedValue(result, dependency, arg)) break;
                }
                return result;
            }

            private <T> boolean addValidatedValue(Map<String, Object> result, JobParameterDef<T> dependency, T arg) {
                final List<String> validate = dependency.validate(arg);
                if (!validate.isEmpty()) {
                    return true;
                }
                result.put(dependency.getKey(), arg);
                return false;
            }
        });
    }

    private <C> List<Observable<?>> getDependenciesObservables(WidgetsMap<C> widgetsMap,
                                                           List<JobParameterDef<?>> dependencies) {
        List<Observable<?>> observables = new ArrayList<>();
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

    private <T> void setValidationMessage(List<String> validate, JobParameterDef<T> jobParameterDef,
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
