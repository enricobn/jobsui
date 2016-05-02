package org.bef.core;

import org.bef.core.ui.UI;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UIWindow;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.FuncN;

import java.util.*;

/**
 * Created by enrico on 4/29/16.
 */
public class JobRunner {

    public <T> JobFuture<T> run(UI ui, final Job<T> job) {
        final UIWindow window = ui.createWindow("Test");
        final UIContainer uiContainer = window.addContainer();

        final Map<JobParameterDef<?>, JobParameterDefUIComponent<?>> componentsMap = new LinkedHashMap<>();

        for (JobParameterDef<?> jobParameterDef : job.getParameterDefs()) {
            final JobParameterDefUIComponent<?> component = jobParameterDef.addToUI(uiContainer);
            componentsMap.put(jobParameterDef, component);
        }

        for (final JobParameterDef<?> jobParameterDef : job.getParameterDefs()) {
            final List<JobParameterDef<?>> dependencies = jobParameterDef.getDependencies();
            if (!dependencies.isEmpty()) {
                List<Observable<?>> observables = new ArrayList<>();
                for (JobParameterDef<?> dependency : dependencies) {
                    final JobParameterDefUIComponent<?> component = componentsMap.get(dependency);
                    observables.add(component.getObservable());
                }

                final Observable<Map<String,Object>> observable = Observable.combineLatest(observables, new FuncN<Map<String,Object>>() {
                    @Override
                    public Map<String,Object> call(Object... args) {
                        Map<String,Object> result = new HashMap<>();

                        int i = 0;
                        for (JobParameterDef<?> dependency : dependencies) {
                            result.put(dependency.getKey(), args[i++]);
                        }
                        return result;
                    }
                });

                observable.subscribe(new Action1<Map<String,Object>>() {
                    @Override
                    public void call(Map<String,Object> objects) {
                        // TODO only if are valid
                        componentsMap.get(jobParameterDef).onDependenciesChange(objects);
                    }
                });
            }
        }

        final Map<String,Object> parameters = new HashMap<>();

        List<Observable<?>> observables = new ArrayList<>();

        for (JobParameterDefUIComponent<?> jobParameterDefUIComponent : componentsMap.values()) {
            observables.add(jobParameterDefUIComponent.getObservable());
        }


        Observable<Boolean> combined = Observable.combineLatest(observables, new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                parameters.clear();

                int i = 0;

                for (final Map.Entry<JobParameterDef<?>, JobParameterDefUIComponent<?>> entry : componentsMap.entrySet()) {
                    final Object value = args[i++];
                    // TODO where must I put the validation messages?
                    final JobParameterDef<Object> parameterDef = (JobParameterDef<Object>) entry.getKey();
                    if (!parameterDef.validate(value).isEmpty()) {
                        break;
                    }
                    parameters.put(parameterDef.getKey(), value);
                }

                if (parameters.size() != args.length) {
                    return false;
                }

                // TODO where must I put the validation messages?
                return job.validate(parameters).isEmpty();
            }
        });

        combined.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean valid) {
                window.setValid(valid);
            }
        });

        if (window.show()) {
            return job.run(parameters);
        }

        return null;
    }
}
