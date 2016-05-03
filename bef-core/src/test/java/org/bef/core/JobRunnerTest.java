package org.bef.core;

import org.bef.core.ui.StringConverterString;
import org.bef.core.ui.UI;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UIValue;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Test;
import rx.Observable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/30/16.
 */
public class JobRunnerTest {

    @Test
    public void run() {
        JobRunner runner = new JobRunner();

        UI ui = new SwingUI();

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();
        final JobParameterDefAbstract<String> name = new JobParameterDefAbstract<String>("name",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public JobParameterDefUIComponent<String> addToUI(UIContainer container) {
                final UIValue<String,?> uiValue = container.add("Name", new StringConverterString(), "Enrico");
                return new JobParameterDefUIComponent<String>() {
                    @Override
                    public Observable<String> getObservable() {
                        return uiValue.getObservable();
                    }

                    @Override
                    public void onDependenciesChange(Map<String,Object> values) {
                    }
                };
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefAbstract<String>("surname",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public JobParameterDefUIComponent<String> addToUI(UIContainer container) {
                final UIValue<String,?> uiValue = container.add("Surname", new StringConverterString(), null);
                return new JobParameterDefUIComponent<String>() {
                    @Override
                    public Observable<String> getObservable() {
                        return uiValue.getObservable();
                    }

                    @Override
                    public void onDependenciesChange(Map<String,Object> values) {
                        System.out.println("name=" + values.get("name"));
                    }
                };
            }
        };
        surname.addDependency(name);
        parameterDefs.add(surname);


        Job<String> job = new Job<String>() {
            @Override
            public List<JobParameterDef<?>> getParameterDefs() {
                return parameterDefs;
            }

            @Override
            public JobFuture<String> run(final Map<String,Object> parameters) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return parameters.get("name") + " " + parameters.get("surname");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String,Object> parameters) {
                return Collections.emptyList();
            }
        };

        final JobFuture<String> future = runner.run(ui, job);

        if (future == null) {
            System.out.println("Cancelled");
        } else {
            System.out.println(future.get());
        }
    }

}