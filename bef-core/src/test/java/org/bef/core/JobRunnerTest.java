package org.bef.core;

import org.bef.core.groovy.JobParameterDefGroovy;
import org.bef.core.ui.*;
import org.bef.core.ui.swing.SwingUI;
import org.junit.Ignore;
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
    @Ignore
    public void run() throws UnsupportedComponentException {
        JobRunner runner = new JobRunner();

        UI ui = new SwingUI();

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefAbstract<String>(
                "name",
                "Name",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String,?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                uiValue.setDefaultValue("Enrico");
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefAbstract<String>(
                "surname",
                "Surname",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String,?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
                System.out.println("name=" + values.get("name"));
            }
        };
        surname.addDependency(name);
        parameterDefs.add(surname);


        Job<String> job = new Job<String>() {
            @Override
            public String getName() {
                return "Test";
            }

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

    @Test
    public void runGroovy() throws UnsupportedComponentException {
        JobRunner runner = new JobRunner();

        UI ui = new SwingUI();

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefGroovy<String>(
                "name",
                "Name",
                String.class,
                "import org.bef.core.ui.UIValue;\n" +
                "import org.bef.core.ui.StringConverterString;\n" +
                "\n" +
                "def uiValue = ui.create(UIValue.class);\n" +
                "uiValue.setConverter(new StringConverterString());\n" +
                "uiValue.setDefaultValue(\"Enrico\");\n" +
                "return uiValue;",
                null,
                null) {
            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefGroovy<String>(
                "surname",
                "Surname",
                String.class,
                "def uiValue = ui.create(org.bef.core.ui.UIValue.class);\n" +
                "uiValue.setConverter(new org.bef.core.ui.StringConverterString());\n" +
                "return uiValue;",
                "System.out.println(\"name=\" + values.get(\"name\"));",
                null) {
        };
        surname.addDependency(name);
        parameterDefs.add(surname);

        Job<String> job = new Job<String>() {
            @Override
            public String getName() {
                return "Test";
            }

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