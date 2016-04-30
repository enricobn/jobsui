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

        final Map<JobParameterDef<?>, Observable<?>> observableMap = new LinkedHashMap<>();

        for (JobParameterDef<?> jobParameterDef : job.getParameterDefs()) {
            final Observable<?> observable = jobParameterDef.addToUI(uiContainer);
            observableMap.put(jobParameterDef, observable);
        }

        final Map<String,Object> parameters = new HashMap<>();

        Observable<Boolean> combined = Observable.combineLatest(observableMap.values(), new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                parameters.clear();

                int i = 0;

                for (final Map.Entry<JobParameterDef<?>, Observable<?>> entry : observableMap.entrySet()) {
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
