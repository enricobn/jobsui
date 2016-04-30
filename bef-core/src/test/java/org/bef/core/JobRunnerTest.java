package org.bef.core;

import org.bef.core.ui.StringConverterString;
import org.bef.core.ui.UI;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public class JobRunnerTest {

    @Test
    public void run() {
        JobRunner runner = new JobRunner();
        UI ui = new SwingUI();
        Job job = new Job() {
            @Override
            public List<JobParameterDef> getParameterDefs() {
                List<JobParameterDef> parameterDefs = new ArrayList<>();
                parameterDefs.add(new JobParameterDefSimple("name", "Name", String.class, new StringConverterString(),
                        new NotEmptyStringValidator()));
                parameterDefs.add(new JobParameterDefSimple("surname", "Surname", String.class, new StringConverterString(),
                        new NotEmptyStringValidator()));
                return parameterDefs;
            }

            @Override
            public JobFuture run(List<JobParameter> parameters) {
                for (JobParameter parameter : parameters) {
                    System.out.println(parameter.getKey() + " " + parameter.getValue());
                }
                return null;
            }

            @Override
            public List<String> validate(List<JobParameter> parameters) {
                return Collections.emptyList();
            }
        };
        runner.run(ui, job);
    }

}