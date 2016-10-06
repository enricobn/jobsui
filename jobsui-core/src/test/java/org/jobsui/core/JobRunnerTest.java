package org.jobsui.core;

import groovy.lang.GroovyShell;
import org.jobsui.core.groovy.JobParameterDefGroovySimple;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.ui.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
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
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createSimpleJob());

        assertThat(jobFuture.get(), equalTo("John Doe"));
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
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");

            }
        };

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(createGroovySimpleJob());

        assertThat(jobFuture.get(), equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_loaded_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");

            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<String> job = project.getJob("simple");

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(job);

        assertThat(jobFuture.get(), equalTo("(John,Doe)"));
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
                uiChoiceUser.setItems(Collections.singletonList("John"));
            }
        };

        jobRunnerWrapper.start(createComplexJob());

        assertThat(window.isValid(), is(true));
    }

    @Test public void assert_that_not_valid_parameter_invokes_set_validation_on_widget() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue(null);
            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<String> job = project.getJob("simple");

        jobRunnerWrapper.start(job);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(window.getWidget("Name"), times(2)).setValidationMessages(captor.capture());

        final List<String> messages1 = captor.getAllValues().get(0);
        // the first time is true since, in the script, there's a set to a default value
        assertThat(messages1.isEmpty(), is(true));

        final List<String> messages2 = captor.getAllValues().get(1);
        assertThat(messages2.isEmpty(), is(false));
    }

    @Test public void verify_that_validation_does_NOT_occur_if_dependencies_are_NOT_valid() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, JobRunnerTest.this.ui, window) {
            @Override
            protected void interact() {
                // I want to ignore all validations at startup
                Mockito.reset(job.getParameter("inv"));
                uiValueSurname.setValue(null);
            }
        };

        jobRunnerWrapper.start(job);

        final JobParameterDef inv = job.getParameter("inv");
        verify(inv, never()).validate(isNull());
        verify(inv, never()).validate(isNotNull());
    }

    @Test public void verify_that_onDepependencyChange_occurs_if_dependencies_are_valid() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, JobRunnerTest.this.ui, window) {
            @Override
            protected void interact() {
                // I want to ignore all validations at startup
                Mockito.reset(job.getParameter("inv"));
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            }
        };

        jobRunnerWrapper.start(job);

        final JobParameterDef inv = job.getParameter("inv");
        verify(inv).onDependenciesChange(any(UIWidget.class), anyMap());
        verify(inv).validate(isNull());
    }

    @Test public void assert_that_a_message_is_shown_when_job_is_not_valid() throws Exception {
        final FakeUiValue<String, ?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);
        when(job.validate(anyMap())).thenReturn(Collections.singletonList("Error"));

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, JobRunnerTest.this.ui, window) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            }
        };

        jobRunnerWrapper.start(job);

        assertEquals(Collections.singletonList("Error"), window.getValidationMessages());
    }

    @Test public void assert_that_groovy_external_concat_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSecond = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");

            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/external"));
        final Job<String> job = project.getJob("concat");

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(job);

        assertThat(jobFuture.get(), equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_ext_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<String, ?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");
            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<String> job = project.getJob("simpleWithExternalCall");

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(job);

        assertThat(jobFuture.get(), equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_int_call_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<String, ?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<String, ?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<String, ?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String> jobRunnerWrapper = new JobRunnerWrapper<String>(runner, ui, window) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");
            }
        };

        JobParser parser = new JobParser();
        Project project = parser.loadProject(new File("src/test/resources/simplejob"));
        final Job<String> job = project.getJob("simpleWithInternalCall");

        final JobFuture<String> jobFuture = jobRunnerWrapper.start(job);

        assertThat(jobFuture.get(), equalTo("John Doe"));
    }

    private Job<String> getMockedSimpleJob(FakeUiValue<String, ?> uiValueName, FakeUiValue<String, ?> uiValueSurname, FakeUIChoice uiChoiceInv) throws UnsupportedComponentException {
        final Job<String> job = mock(Job.class);

        final Map<String, JobParameterDef<?>> parameters = new LinkedHashMap<>();
        final JobParameterDef name = mock(JobParameterDef.class, "name");
        parameters.put("name", name);
        final JobParameterDef surname = mock(JobParameterDef.class, "surname");
        parameters.put("surname", surname);
        final JobParameterDef inv = mock(JobParameterDef.class, "inv");
        parameters.put("inv", inv);

        List<JobParameterDef<?>> parametersList = new ArrayList<>(parameters.values());
        when(job.getParameterDefs()).thenReturn(parametersList);

        when(name.createComponent(any(UI.class))).thenReturn(uiValueName);
        when(name.getKey()).thenReturn("name");
        when(name.getName()).thenReturn("Name");
        when(name.isVisible()).thenReturn(true);
        when(name.isOptional()).thenReturn(false);
        when(name.validate(isNull())).thenReturn(Collections.singletonList("Error"));
        when(name.validate(isNotNull())).thenReturn(Collections.emptyList());

        when(surname.createComponent(any(UI.class))).thenReturn(uiValueSurname);
        when(surname.getKey()).thenReturn("surname");
        when(surname.getName()).thenReturn("Surname");
        when(surname.isVisible()).thenReturn(true);
        when(surname.isOptional()).thenReturn(false);
        when(surname.validate(isNull())).thenReturn(Collections.singletonList("Error"));
        when(surname.validate(isNotNull())).thenReturn(Collections.emptyList());

        when(inv.createComponent(any(UI.class))).thenReturn(uiChoiceInv);
        when(inv.getKey()).thenReturn("inv");
        when(inv.getName()).thenReturn("Inv");
        when(inv.isVisible()).thenReturn(false);
        when(inv.isOptional()).thenReturn(false);
        when(inv.getDependencies()).thenReturn(
                Collections.<JobParameterDef<?>>singletonList(name));

        when(job.getParameter(anyString())).thenAnswer(new Answer<JobParameterDef>() {
            @Override
            public JobParameterDef answer(InvocationOnMock invocation) throws Throwable {
                final JobParameterDef jobParameterDef = parameters.get(invocation.getArguments()[0]);
                return jobParameterDef;
            }
        });
        return job;
    }

    private MockSettings printInvocation(String name, final CharSequence methodName) {
        return Mockito.withSettings().name(name).invocationListeners(new InvocationListener() {
            @Override
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
                if (methodInvocationReport.getInvocation().toString().contains(methodName)) {
                    new RuntimeException().printStackTrace();
                }
            }
        });
    }


    private Job<String> createSimpleJob() {
        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefAbstract<String>(
                "name",
                "Name",
                new NotEmptyStringValidator(), false, true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<String, ?> uiValue = (UIValue<String, ?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                uiValue.setDefaultValue("John");
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
                new NotEmptyStringValidator(), false, true) {
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
            public JobFuture<String> run(final Map<String, Object> values) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return values.get("name") + " " + values.get("surname");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> values) {
                return Collections.emptyList();
            }
        };

    }

    private Job<String> createGroovySimpleJob() {
        GroovyShell shell = new GroovyShell();
        final List<JobParameterDef<?>> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract<String> name = new JobParameterDefGroovySimple<String>(
                null,
                shell,
                "name",
                "Name",
                "def uiValue = ui.create(UIValue.class);\n" +
                "uiValue.setConverter(new StringConverterString());\n" +
                "uiValue.setDefaultValue(\"John\");\n" +
                "return uiValue;",
                null,
                null,
                false,
                true) {
            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract<String> surname = new JobParameterDefGroovySimple<String>(
                null,
                shell,
                "surname",
                "Surname",
                "def uiValue = ui.create(UIValue.class);\n" +
                        "uiValue.setConverter(new StringConverterString());\n" +
                        "return uiValue;",
                null,
                null,
                false,
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
            public JobFuture<String> run(final Map<String, Object> values) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return values.get("name") + " " + values.get("surname");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> values) {
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
                false,
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
                false,
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
                false,
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
            public JobFuture<String> run(final Map<String, Object> values) {
                return new JobFuture<String>() {
                    @Override
                    public String get() {
                        return (String) values.get("user");
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Object> values) {
                return Collections.emptyList();
            }
        };

    }
}