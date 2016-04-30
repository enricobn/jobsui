package org.bef.core;

import org.bef.core.ui.UI;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UIWindow;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public class JobRunner {

    public JobFuture run(UI ui, final Job job) {
        final UIWindow window = ui.createWindow();
        final UIContainer uiContainer = window.addContainer();

        final Map<JobParameterDef, Observable<Object>> observableMap = new LinkedHashMap<>();

        for (JobParameterDef jobParameterDef : job.getParameterDefs()) {
            final Observable<Object> observable = jobParameterDef.addToUI(uiContainer);
            observableMap.put(jobParameterDef, observable);
        }

//        final UIButton runButton = uiContainer.addButton("Run");
//        runButton.setEnabled(false);

        final List<JobParameter> parameters = new ArrayList<JobParameter>();

        Observable<Boolean> combined = Observable.combineLatest(observableMap.values(), new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                parameters.clear();

                int i = 0;

                for (final Map.Entry<JobParameterDef, Observable<Object>> entry : observableMap.entrySet()) {
                    final Object value = args[i++];
                    // TODO where must I put the validation?
                    if (!entry.getKey().validate(value).isEmpty()) {
                        break;
                    }
                    parameters.add(new JobParameter() {
                        @Override
                        public String getKey() {
                            return entry.getKey().getKey();
                        }

                        @Override
                        public Object getValue() {
                            return value;
                        }
                    });
                }

                return parameters.size() == args.length;
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
