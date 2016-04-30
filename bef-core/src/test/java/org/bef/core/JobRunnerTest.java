package org.bef.core;

import org.bef.core.ui.StringConverterString;
import org.bef.core.ui.UI;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Test;

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

        Job<String> job = new Job<String>() {
            @Override
            public List<JobParameterDef<?>> getParameterDefs() {
                List<JobParameterDef<?>> parameterDefs = new ArrayList<>();
                parameterDefs.add(new JobParameterDefSimple<>("name", "Name", String.class, new StringConverterString(),
                        new NotEmptyStringValidator()));
                parameterDefs.add(new JobParameterDefSimple<>("surname", "Surname", String.class, new StringConverterString(),
                        new NotEmptyStringValidator()));
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