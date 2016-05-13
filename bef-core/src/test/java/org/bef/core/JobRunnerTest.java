package org.bef.core;

import groovy.lang.GroovyShell;
import org.bef.core.groovy.JobParameterDefGroovy;
import org.bef.core.groovy.JobParser;
import org.bef.core.ui.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
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

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");
            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createSimpleJob());

        assertThat(jobFuture.get(), equalTo("Enrico Benedetti"));
    }

    @Test public void assert_that_simplejob_is_not_valid_when_run_with_invalid_parameters() throws Exception {
        FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
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

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");

            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createGroovySimpleJob());

        assertThat(jobFuture.get(), equalTo("Enrico Benedetti"));
    }

    @Test public void assert_that_groovy_loaded_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue("Enrico");
                uiValueSurname.setValue("Benedetti");

            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<String> job = project.getJob("simple");

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(job);

        assertThat(jobFuture.get(), equalTo("(Enrico,Benedetti)"));
    }

    @Test public void assert_that_complexjob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUIChoice<String,?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiChoiceVersion.setItems(Arrays.asList("1.0", "2.0"));
                uiChoiceVersion.setValue("1.0");
                uiChoiceDb.setValue("Dev-1.0");
            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createComplexJob());

        assertThat(jobFuture.get(), equalTo("1.0 Dev-1.0"));
        assertThat(uiChoiceDb.getItems(), equalTo(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev")));
        assertThat(uiChoiceUser.getItems(), equalTo(Collections.singletonList("1.0 Dev-1.0")));
    }

    @Test public void assert_that_the_default_value_of_a_parameter_triggers_validation() throws Exception {
        final FakeUIChoice<String,?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<String,?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiChoiceVersion.setItems(Collections.singletonList("1.0"));
                uiChoiceDb.setItems(Collections.singletonList("Dev-1.0"));
                uiChoiceUser.setItems(Collections.singletonList("Enrico"));
            }
        };

        jobRunnerWrapper.start(createComplexJob());

        assertThat(window.isValid(), is(true));
    }

    private Job<String> createSimpleJob() {
        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefAbstract<String>(
                "name",
                "Name",
                new NotEmptyStringValidator(), true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String, ?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                uiValue.setDefaultValue("Enrico");
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefAbstract<String>(
                "surname",
                "Surname",
                new NotEmptyStringValidator(), true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String, ?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
            }
        };
        surname.addDependency(name);
        parameterDefs.add(surname);

        return new JobAbstract<String>() {
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
                "import org.bef.core.ui.UIValue;\n" +
                        "import org.bef.core.ui.StringConverterString;\n" +
                        "\n" +
                        "def uiValue = ui.create(UIValue.class);\n" +
                        "uiValue.setConverter(new StringConverterString());\n" +
                        "uiValue.setDefaultValue(\"Enrico\");\n" +
                        "return uiValue;",
                null,
                null,
                true) {
            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefGroovy<String>(
                shell,
                "surname",
                "Surname",
                "def uiValue = ui.create(org.bef.core.ui.UIValue.class);\n" +
                        "uiValue.setConverter(new org.bef.core.ui.StringConverterString());\n" +
                        "return uiValue;",
                null,
                null,
                true) {
        };
        surname.addDependency(name);
        parameterDefs.add(surname);

        return new JobAbstract<String>() {
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
                new NotEmptyStringValidator(),
                true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return ui.create(UIChoice.class);
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
            }
        };
        parameterDefs.add(version);

        final JobParameterDefAbstract<String> db = new JobParameterDefAbstract<String>(
                "db",
                "DB",
                new NotEmptyStringValidator(),
                true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return ui.create(UIChoice.class);
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
                String version = (String) values.get("version");
                if (version == null) {
                    ((UIChoice)widget.getComponent()).setItems(Collections.<String>emptyList());
                } else if (version.equals("1.0")) {
                    ((UIChoice)widget.getComponent()).setItems(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"));
                } else {
                    ((UIChoice)widget.getComponent()).setItems(Arrays.asList("Dev-2.0", "Cons-2.0", "Dev"));
                }
            }
        };
        parameterDefs.add(db);
        db.addDependency(version);

        final JobParameterDefAbstract<String> user = new JobParameterDefAbstract<String>(
                "user",
                "User",
                new NotEmptyStringValidator(),
                true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                return ui.create(UIChoice.class);
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
                String version = (String) values.get("version");
                String db = (String) values.get("db");
                if (version == null || db == null) {
                    ((UIChoice)widget.getComponent()).setItems(Collections.<String>emptyList());
                } else {
                    ((UIChoice)widget.getComponent()).setItems(Collections.singletonList(version + " " + db));
                }
            }
        };
        parameterDefs.add(user);
        user.addDependency(db);
        user.addDependency(version);

        return new JobAbstract<String>() {
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
}