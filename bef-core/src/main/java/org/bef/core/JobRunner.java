package org.bef.core;

import org.bef.core.ui.*;
import org.bef.core.utils.BEFUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.FuncN;

import java.util.*;

/**
 * Created by enrico on 4/29/16.
 */
public class JobRunner {

    public <T> JobFuture<T> run(final UI ui, final Job<T> job) throws UnsupportedComponentException {
        final UIWindow window = ui.createWindow(job.getName());

        final Map<JobParameterDef, UIWidget> widgets = createWidgets(ui, job, window);

        observeDependencies(job, widgets);

        final Map<String, Object> values = observeValues(ui, job, window, widgets);

        window.setValid(false);

        notifyInitialValue(widgets);

        if (window.show()) {
            return job.run(values);
        }

        return null;
    }

    private <T> Map<JobParameterDef, UIWidget> createWidgets(final UI ui, Job<T> job, UIWindow window) throws UnsupportedComponentException {
        final Map<JobParameterDef, UIWidget> widgetsMap = new LinkedHashMap<>();

        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            final UIComponent component = jobParameterDef.createComponent(ui);
            if (component == null) {
                throw new IllegalStateException("Cannot create component for parameter with key \""
                        + jobParameterDef.getKey() + "\"");
            }

            final UIWidget widget = window.add(jobParameterDef.getName(), component);
            if (widget == null) {
                throw new IllegalStateException("Cannot create widget for parameter with key \""
                        + jobParameterDef.getKey() + "\"");
            }

            widget.setVisible(jobParameterDef.isVisible());

            widgetsMap.put(jobParameterDef, widget);

            final Observable observable = widget.getComponent().getObservable();

            observable.subscribe(new Action1() {
                @Override
                public void call(Object o) {
                    setValidationMessage(jobParameterDef.validate(o), jobParameterDef, widget, ui);
                }
            });
        }
        return widgetsMap;
    }

    private void notifyInitialValue(Map<JobParameterDef, UIWidget> widgetsMap) {
        for (Map.Entry<JobParameterDef, UIWidget> entry : widgetsMap.entrySet()) {
            final Object value = entry.getValue().getComponent().getValue();
            if (entry.getKey().validate(value).isEmpty()) {
                entry.getValue().getComponent().notifySubscribers();
            }
        }
    }

    private <T> Map<String, Object> observeValues(final UI ui, final Job<T> job, final UIWindow window, final Map<JobParameterDef, UIWidget> widgetsMap) {
        final Map<String,Object> parameters = new HashMap<>();

        List<Observable<?>> observables = new ArrayList<>();

        for (UIWidget widget : widgetsMap.values()) {
            observables.add(widget.getComponent().getObservable());
        }

        Observable<Boolean> combined = Observable.combineLatest(observables, new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                parameters.clear();

                int i = 0;

                for (final Map.Entry<JobParameterDef, UIWidget> entry : widgetsMap.entrySet()) {
                    final Object value = args[i++];
                    final JobParameterDef<Object> parameterDef = (JobParameterDef<Object>) entry.getKey();
                    final List<String> validate = parameterDef.validate(value);

                    setValidationMessage(validate, parameterDef, entry.getValue(), ui);

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
                    window.showValidationMessage(BEFUtils.getMessagesAsString(validate));
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

    private <T> void observeDependencies(Job<T> job, final Map<JobParameterDef, UIWidget> widgetsMap) {
        for (final JobParameterDef jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<?>> observables = getDependenciesObservables(widgetsMap, dependencies);

                final Observable<Map<String, Object>> observable = combineDependeciesObservables(dependencies, observables);

                observable.subscribe(new Action1<Map<String,Object>>() {
                    @Override
                    public void call(Map<String,Object> objects) {
                        // all dependencies are valid
                        if (objects.size() == dependencies.size()) {
                            final UIWidget widget = widgetsMap.get(jobParameterDef);
                            jobParameterDef.onDependenciesChange(widget, objects);
                        }
                    }
                });
            }
        }
    }

    private Observable<Map<String, Object>> combineDependeciesObservables(final List<JobParameterDef> dependencies, List<Observable<?>> observables) {
        return Observable.combineLatest(observables, new FuncN<Map<String,Object>>() {
            @Override
            public Map<String,Object> call(Object... args) {
                Map<String,Object> result = new HashMap<>();

                int i = 0;
                for (JobParameterDef dependency : dependencies) {
                    final Object arg = args[i++];
                    final List validate = dependency.validate(arg);
                    if (!validate.isEmpty()) {
                        break;
                    }
                    result.put(dependency.getKey(), arg);
                }
                return result;
            }
        });
    }

    private List<Observable<?>> getDependenciesObservables(Map<JobParameterDef, UIWidget> widgetsMap, List<JobParameterDef> dependencies) {
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

    private void setValidationMessage(List<String> validate, JobParameterDef jobParameterDef,
                                      UIWidget widget, UI ui) {
        if (!jobParameterDef.isVisible()) {
            if (!validate.isEmpty()) {
                ui.showMessage(jobParameterDef.getName() + ": " + BEFUtils.getMessagesAsString(validate));
            }
        } else {
            widget.setValidationMessages(validate);
        }
    }
}
