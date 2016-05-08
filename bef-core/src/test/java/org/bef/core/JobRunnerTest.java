package org.bef.core;

import groovy.lang.GroovyShell;
import org.bef.core.groovy.JobParameterDefGroovy;
import org.bef.core.ui.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 4/30/16.
 */
public class JobRunnerTest {
    private JobRunner runner;
    private UI ui;
    private FakeUIWindow window;

    @Before
    public void init() {
        runner = new JobRunner();
        ui = mock(UI.class);
        window = new FakeUIWindow();
        when(ui.createWindow(anyString())).thenReturn(window);
    }

    @Test public void assert_that_simplejob_is_valid_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>() {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");
            }
        };

        jobRunnerWrapper.start(createSimpleJob());

        assertThat(window.isValid(), is(true));
    }

    @Test public void assert_that_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>() {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");
            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createSimpleJob());

        assertThat("Enrico Benedetti", equalTo(jobFuture.get()));
    }

    @Test public void assert_that_simplejob_is_not_valid_when_run_with_invalid_parameters() throws Exception {
        FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>() {
            @Override
            protected void interact() {
            }
        };

        jobRunnerWrapper.start(createSimpleJob());

        assertThat(window.isValid(), is(false));
    }

    @Test public void assert_that_groovy_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>() {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");

            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createGroovySimpleJob());

        assertThat("Enrico Benedetti", equalTo(jobFuture.get()));
    }

    @Test public void assert_that_complexjob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUIChoice<String,?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>() {
            @Override
            protected void interact() {
                uiChoiceVersion.setItems(Arrays.asList("1.0", "2.0"));
                uiChoiceVersion.setSelectedItem("1.0");
                uiChoiceDb.setSelectedItem("Dev-1.0");
            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createComplexJob());

        assertThat("1.0 Dev-1.0", equalTo(jobFuture.get()));
        assertThat(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"), equalTo(uiChoiceDb.getItems()));
        assertThat(Collections.singletonList("1.0 Dev-1.0"), equalTo(uiChoiceUser.getItems()));
    }

    private abstract class JobRunnerWrapper<T> {

        public JobFuture<T> start(Job<T> job) throws Exception {

            final Future<JobFuture<T>> future = runJob(job);

            window.waitUntilStarted();

            interact();

            window.exit();

            return future.get();
        }

        protected abstract void interact();
    }

    private Job<String> createSimpleJob() {
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

        return new Job<String>() {
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

    }

    private Job<String> createGroovySimpleJob() {
        GroovyShell shell = new GroovyShell();
        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

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

        return new Job<String>() {
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
    }

    private Job<String> createComplexJob() {
        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> version = new JobParameterDefAbstract<String>(
                "version",
                "Version",
                String.class,
                new NotEmptyStringValidator()) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return ui.create(UIChoice.class);
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
                return ui.create(UIChoice.class);
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
                String version = (String) values.get("version");
                if (version == null) {
                    ((UIChoice)component).setItems(Collections.<String>emptyList());
                } else if (version.equals("1.0")) {
                    ((UIChoice)component).setItems(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"));
                } else {
                    ((UIChoice)component).setItems(Arrays.asList("Dev-2.0", "Cons-2.0", "Dev"));
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
                return ui.create(UIChoice.class);
            }

            @Override
            public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
                String version = (String) values.get("version");
                String db = (String) values.get("db");
                if (version == null || db == null) {
                    ((UIChoice)component).setItems(Collections.<String>emptyList());
                } else {
                    ((UIChoice)component).setItems(Arrays.asList(version + " " + db));
                }
            }
        };
        parameterDefs.add(user);
        user.addDependency(db);
        user.addDependency(version);

        return new Job<String>() {
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

    }

    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    public <T> Future<JobFuture<T>> runJob(final Job<T> job) {
        return pool.submit(new Callable<JobFuture<T>>() {
            @Override
            public JobFuture<T> call() throws Exception {
                return runner.run(ui, job);
            }
        });
    }
}