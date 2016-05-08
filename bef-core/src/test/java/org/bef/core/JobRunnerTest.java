package org.bef.core;

import groovy.lang.GroovyShell;
import org.bef.core.groovy.JobParameterDefGroovy;
import org.bef.core.ui.*;
import org.bef.core.utils.Tuple2;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 4/30/16.
 */
public class JobRunnerTest {
    private JobRunner runner;
    private UI ui;
    private UIWindow window;

    @Before
    public void init() {
        runner = new JobRunner();
        ui = mock(UI.class);
        window = mock(UIWindow.class);
        when(window.show()).thenReturn(true);
        when(ui.createWindow(anyString())).thenReturn(window);
    }

    @Test
    public void runSimple() throws UnsupportedComponentException {
        FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefAbstract<String>(
                "name",
                "Name",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String, ?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
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
                final UIValue<String, ?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
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
            public JobFuture<String> run(final Map<String, Object> parameters) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return parameters.get("name") + " " + parameters.get("surname");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> parameters) {
                return Collections.emptyList();
            }
        };

        final JobFuture<String> future = runner.run(ui, job);

        uiValueName.setValue("Enrico");
        uiValueSurname.setValue("Benedetti");

        assertEquals("Enrico Benedetti", future.get());
    }

    @Test
    public void runSimpleGroovy() throws UnsupportedComponentException {
        FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        GroovyShell shell = new GroovyShell();

        final JobParameterDefAbstract<String> name = new JobParameterDefGroovy<String>(
                shell,
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
                shell,
                "surname",
                "Surname",
                String.class,
                "def uiValue = ui.create(org.bef.core.ui.UIValue.class);\n" +
                        "uiValue.setConverter(new org.bef.core.ui.StringConverterString());\n" +
                        "return uiValue;",
                null,
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
            public JobFuture<String> run(final Map<String, Object> parameters) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return parameters.get("name") + " " + parameters.get("surname");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> parameters) {
                return Collections.emptyList();
            }
        };

        final JobFuture<String> future = runner.run(ui, job);

        uiValueName.setValue("Enrico");
        uiValueSurname.setValue("Benedetti");

        assertEquals("Enrico Benedetti", future.get());

    }

    @Test
    public void runComplex() throws UnsupportedComponentException {
        final FakeUIChoice<String,?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceUser = new FakeUIChoice<>();

        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> version = new JobParameterDefAbstract<String>(
                "version",
                "Version",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return uiChoiceVersion;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
            }
        };
        parameterDefs.add(version);

        final JobParameterDefAbstract<String> db = new JobParameterDefAbstract<String>(
                "db",
                "DB",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return uiChoiceDb;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
                String version = (String) values.get("version");
                if (version == null) {
                    uiChoiceDb.setItems(Collections.<String>emptyList());
                } else if (version.equals("1.0")) {
                    uiChoiceDb.setItems(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"));
                } else {
                    uiChoiceDb.setItems(Arrays.asList("Dev-2.0", "Cons-2.0", "Dev"));
                }
            }
        };
        parameterDefs.add(db);
        db.addDependency(version);

        final JobParameterDefAbstract<String> user = new JobParameterDefAbstract<String>(
                "user",
                "User",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return uiChoiceUser;
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
                String version = (String) values.get("version");
                String db = (String) values.get("db");
                if (version == null || db == null) {
                    uiChoiceUser.setItems(Collections.<String>emptyList());
                } else {
                    uiChoiceUser.setItems(Arrays.asList(version + " " + db));
                }

            }
        };
        parameterDefs.add(user);
        user.addDependency(db);
        user.addDependency(version);

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
            public JobFuture<String> run(final Map<String, Object> parameters) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return (String) parameters.get("user");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> parameters) {
                return Collections.emptyList();
            }
        };

        final JobFuture<String> future = runner.run(ui, job);

        uiChoiceVersion.setItems(Arrays.asList("1.0", "2.0"));

        uiChoiceVersion.setSelectedItem("1.0");
        uiChoiceDb.setSelectedItem("Dev-1.0");

        assertEquals("1.0 Dev-1.0", future.get());
        assertEquals(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"), uiChoiceDb.getItems());
        assertEquals(Arrays.asList("1.0 Dev-1.0"), uiChoiceUser.getItems());
    }

}