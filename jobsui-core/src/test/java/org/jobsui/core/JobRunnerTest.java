package org.jobsui.core;

import groovy.lang.GroovyShell;
import org.jobsui.core.groovy.JobExpressionGroovy;
import org.jobsui.core.groovy.JobParameterDefGroovySimple;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.*;
import org.jobsui.core.runner.JobResult;
import org.jobsui.core.runner.JobResultImpl;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.ui.*;
import org.jobsui.core.xml.ProjectXML;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * Created by enrico on 4/30/16.
 */
public class JobRunnerTest {
    private JobUIRunner runner;
    private UI ui;
    private FakeUIWindow window;
    private FakeUIButton<?> runButton;
    private FakeUIButton<?> bookmarkButton;
    private static Job<String> simpleWithInternalCallFSJob;
    private static Job<String> simpleJob;
    private static Job<String> simpleFSJob;
    private static Job<String> groovySimpleJob;
    private static Job<String> complexJob;

    @BeforeClass
    public static void initStatic() throws Exception {
        simpleWithInternalCallFSJob = getJob("src/test/resources/simplejob", "simpleWithInternalCall");
        simpleFSJob = getJob("src/test/resources/simplejob", "simple");
        simpleJob = createSimpleJob();
        groovySimpleJob = createGroovySimpleJob();
        complexJob = createComplexJob();
    }

    @AfterClass
    public static void teardownStatic() throws Exception {
        simpleWithInternalCallFSJob = null;
        simpleFSJob = null;
        simpleJob = null;
        groovySimpleJob = null;
        complexJob = null;
    }

    @Before
    public void init() throws Exception {
        ui = mock(UI.class);
        runner = new JobUIRunner(ui);
        window = new FakeUIWindow();
        when(ui.createWindow(anyString())).thenReturn(window);
        runButton = spy(new FakeUIButton<>());
        bookmarkButton = spy(new FakeUIButton<>());
        when(ui.create(UIButton.class)).thenReturn(runButton, bookmarkButton);
        doAnswer(invocation -> {
            String message = invocation.getArgumentAt(0, String.class);
            Exception exception = invocation.getArgumentAt(1, Exception.class);
            System.err.println(message);
            exception.printStackTrace();
            return null;
        }).when(ui).showError(anyString(), any(Throwable.class));
    }

    @After
    public void tearDown() throws Exception {
        verify(runButton).setTitle("Run");
        verify(bookmarkButton).setTitle("Bookmark");
    }

    @Test public void assert_that_simplejob_is_valid_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            }
        };

        jobRunnerWrapper.start(simpleJob);

        assertThat(jobRunnerWrapper.isValid(), is(true));
    }

    @Test public void assert_that_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            }
        };

        String result = jobRunnerWrapper.start(simpleJob);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_simplejob_is_not_valid_when_run_with_invalid_parameters() throws Exception {
        FakeUiValue<?> uiValueName = new FakeUiValue<>();
        FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
            }
        };

        jobRunnerWrapper.start(simpleJob);

        assertThat(jobRunnerWrapper.isValid(), is(false));
    }

    @Test public void assert_that_groovy_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");

            }
        };

        String result = jobRunnerWrapper.start(groovySimpleJob);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_loaded_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");

            }
        };

        String result = jobRunnerWrapper.start(simpleFSJob);

        assertThat(result, equalTo("(John,Doe)"));
    }

    @Test public void assert_that_complexjob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUIChoice<?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiChoiceVersion.setItems(Arrays.asList("1.0", "2.0"));
                uiChoiceVersion.setValue("1.0");
                uiChoiceDb.setValue("Dev-1.0");
            }
        };

        String result = jobRunnerWrapper.start(complexJob);

        assertThat(result, equalTo("1.0 Dev-1.0"));
        assertThat(uiChoiceDb.getItems(), equalTo(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev")));
        assertThat(uiChoiceUser.getItems(), equalTo(Collections.singletonList("1.0 Dev-1.0")));
    }

    @Test public void assert_that_the_default_value_of_a_parameter_triggers_validation() throws Exception {
        final FakeUIChoice<?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiChoiceVersion.setItems(Collections.singletonList("1.0"));
                uiChoiceDb.setItems(Collections.singletonList("Dev-1.0"));
                uiChoiceUser.setItems(Collections.singletonList("John"));
            }
        };

        jobRunnerWrapper.start(complexJob);

        assertThat(jobRunnerWrapper.isValid(), is(true));
    }

    @Test public void assert_that_not_valid_parameter_invokes_set_validation_on_widget() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueName.setValue(null);
            }
        };

        jobRunnerWrapper.start(simpleFSJob);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(window.getWidget("Name"), times(2)).setValidationMessages(captor.capture());

        final List<String> messages1 = captor.getAllValues().get(0);
        // the first time is true since, in the script, there's a set to a default value
        assertThat(messages1.isEmpty(), is(true));

        final List<String> messages2 = captor.getAllValues().get(1);
        assertThat(messages2.size(), is(1));
        assertThat(messages2.get(0), containsString("is null"));
    }

    @Test public void verify_that_validation_does_NOT_occur_if_dependencies_are_NOT_valid() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                // I want to ignore all validations at startup
                Mockito.reset(job.getParameter("inv"));
                uiValueSurname.setValue(null);
            }
        };

        jobRunnerWrapper.start(job);

        final JobParameterDef inv = job.getParameter("inv");
        verify(inv, never()).validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class));
        verify(inv, never()).validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class));
    }

    @Test public void verify_that_onDepependencyChange_occurs_if_dependencies_are_valid() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
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
        verify(inv).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));
        verify(inv, atLeast(1)).validate(anyMapOf(String.class, Serializable.class),
                isNull(Serializable.class));
    }

    @Test public void assert_that_a_message_is_shown_when_job_is_not_valid() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        final FakeUIChoice uiChoiceInv = new FakeUIChoice();
        when(this.ui.create(UIChoice.class)).thenReturn(uiChoiceInv);

        final Job<String> job = getMockedSimpleJob(uiValueName, uiValueSurname, uiChoiceInv);
        when(job.validate(anyMapOf(String.class, Serializable.class))).thenReturn(Collections.singletonList("Error"));

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
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
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");
            }
        };

        final Job<String> job = getJob("src/test/resources/external", "concat");

        String result = jobRunnerWrapper.start(job);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_ext_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");
            }
        };

        String result = jobRunnerWrapper.start(simpleWithInternalCallFSJob);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_int_call_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue(" Doe");
            }
        };

        String result = jobRunnerWrapper.start(simpleWithInternalCallFSJob);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_expression_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton) {
            @Override
            protected void interact() {
                uiValueFirst.setValue("John");
                uiValueSecond.setValue("Doe");
            }
        };

        final Job<String> job = createGroovySimpleJobWithExpression();

        String result = jobRunnerWrapper.start(job);

        assertThat(result, equalTo("Mr. John Doe"));
    }

    private static Job<String> getMockedSimpleJob(FakeUiValue<?> uiValueName, FakeUiValue<?> uiValueSurname,
                                           FakeUIChoice uiChoiceInv) throws UnsupportedComponentException {
        final Job<String> job = mock(Job.class);

        final Map<String, JobParameterDef> parameters = new LinkedHashMap<>();
        final JobParameterDef name = mock(JobParameterDef.class, "name");
        parameters.put("name", name);
        final JobParameterDef surname = mock(JobParameterDef.class, "surname");
        parameters.put("surname", surname);
        final JobParameterDef inv = mock(JobParameterDef.class, "inv");
        parameters.put("inv", inv);

        List<JobParameterDef> parametersList = new ArrayList<>(parameters.values());
        when(job.getParameterDefs()).thenReturn(parametersList);

        when(name.createComponent(any(UI.class))).thenReturn((UIComponent<Object>) uiValueName);
        when(name.getKey()).thenReturn("name");
        when(name.getName()).thenReturn("Name");
        when(name.isVisible()).thenReturn(true);
        when(name.isOptional()).thenReturn(false);
        when(name.validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class))).thenReturn(Collections.singletonList("Error"));
        when(name.validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class))).thenReturn(Collections.emptyList());

        when(surname.createComponent(any(UI.class))).thenReturn((UIComponent<Object>) uiValueSurname);
        when(surname.getKey()).thenReturn("surname");
        when(surname.getName()).thenReturn("Surname");
        when(surname.isVisible()).thenReturn(true);
        when(surname.isOptional()).thenReturn(false);
        when(surname.validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class))).thenReturn(Collections.singletonList("Error"));
        when(surname.validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class))).thenReturn(Collections.emptyList());

        when(inv.createComponent(any(UI.class))).thenReturn(uiChoiceInv);
        when(inv.getKey()).thenReturn("inv");
        when(inv.getName()).thenReturn("Inv");
        when(inv.isVisible()).thenReturn(false);
        when(inv.isOptional()).thenReturn(false);
        when(inv.getDependencies()).thenReturn(Collections.singletonList("name"));
//        doAnswer(invocation -> {
//            Map<String,Serializable> values = (Map<String, Serializable>) invocation.getArguments()[1];
//            uiChoiceInv.setValue(values.get("name"));
//            return null;
//        }).when(inv).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));

        when(job.getParameter(anyString())).thenAnswer(invocation ->
                parameters.get(invocation.getArguments()[0].toString())
        );
        try {
            when(job.getSortedDependencies()).thenReturn(Arrays.asList(name, surname, inv));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(job.getUnsortedDependencies()).thenReturn(Arrays.asList(inv, name, surname));
        when(job.run(anyMapOf(String.class, Serializable.class))).thenReturn(new JobResultImpl<>((String)null));
        when(job.run(any(JobValues.class))).thenReturn(new JobResultImpl<>((String)null));
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

    private static <T> Job<T> getJob(String file, String job) throws Exception {
        ProjectXML projectXML = JobParser.getParser(file).parse();
        Project project = new ProjectGroovyBuilder().build(file, projectXML);
        Job<T> result = project.getJob(job);
        if (result == null) {
            throw new Exception("Cannot find job with id \"" + job + "\". Ids:" + project.getJobsIds());
        }
        return result;
    }

    private static Job<String> createSimpleJob() {
        final List<JobParameterDef> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract name = new JobParameterDefAbstract(
                "name",
                "Name",
                new NotEmptyStringValidator(), false, true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<?> uiValue = (UIValue<?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                uiValue.setDefaultValue("John");
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract surname = new JobParameterDefAbstract(
                "surname",
                "Surname",
                new NotEmptyStringValidator(), false, true) {
            @Override
            public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
                final UIValue<?> uiValue = (UIValue<?>) ui.create(UIValue.class);
                uiValue.setConverter(new StringConverterString());
                return uiValue;
            }

            @Override
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
            }
        };
        surname.addDependency(name.getKey());
        parameterDefs.add(surname);

        return new JobAbstract<String>() {

            @Override
            public String getId() {
                return "JobRunnerTest.simpleJob";
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public List<JobParameterDef> getParameterDefs() {
                return parameterDefs;
            }

            @Override
            public List<JobExpression> getExpressions() {
                return Collections.emptyList();
            }

            @Override
            public JobResult<String> run(final Map<String, Serializable> values) {
                return new JobResult<String>() {
                    @Override
                    public String get() {
                        return values.get("name") + " " + values.get("surname");
                    }

                    @Override
                    public Exception getException() {
                        return null;
                    }
                };
            }

//            @Override
//            public JobFuture<String> run(JobValues values) {
//                return () -> values.getValue(name) + " " + values.getValue(surname);
//            }

            @Override
            public List<String> validate(Map<String, Serializable> values) {
                return Collections.emptyList();
            }
        };

    }

    private static Job<String> createGroovySimpleJob() {
        GroovyShell shell = new GroovyShell();
        final List<JobParameterDef> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract name = new JobParameterDefGroovySimple(
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
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract surname = new JobParameterDefGroovySimple(
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
        surname.addDependency(name.getKey());
        parameterDefs.add(surname);

        return new JobAbstract<String>() {
            @Override
            public String getId() {
                return "JobRunnerTest.groovySimpleJob";
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public List<JobParameterDef> getParameterDefs() {
                return parameterDefs;
            }

            @Override
            public List<JobExpression> getExpressions() {
                return Collections.emptyList();
            }

            @Override
            public JobResult<String> run(final Map<String, Serializable> values) {
                return new JobResult<String>() {
                    @Override
                    public String get() {
                        return values.get("name") + " " + values.get("surname");
                    }

                    @Override
                    public Exception getException() {
                        return null;
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Serializable> values) {
                return Collections.emptyList();
            }
        };
    }

    private static Job<String> createGroovySimpleJobWithExpression() {
        GroovyShell shell = new GroovyShell();
        final List<JobParameterDef> parameterDefs = new ArrayList<>();
        final List<JobExpression> expressions = new ArrayList<>();

        final JobParameterDefAbstract name = new JobParameterDefGroovySimple(
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
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
            }
        };
        parameterDefs.add(name);

        final JobParameterDefAbstract surname = new JobParameterDefGroovySimple(
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
        parameterDefs.add(surname);

        final JobExpressionGroovy completeName = new JobExpressionGroovy(
                shell,
                "completeName",
                "Complete name",
                "return name + ' ' + surname;"
        );
        completeName.addDependency("name");
        completeName.addDependency("surname");
        expressions.add(completeName);

        final JobExpressionGroovy prefixed = new JobExpressionGroovy(
                shell,
                "prefixed",
                "Name prefix",
                "return 'Mr. ' + completeName;"
        );
        prefixed.addDependency("completeName");
        expressions.add(prefixed);

        return new JobAbstract<String>() {
            @Override
            public String getId() {
                return "JobRunnerTest.groovySimpleJobWithParameters";
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public List<JobParameterDef> getParameterDefs() {
                return parameterDefs;
            }

            @Override
            public List<JobExpression> getExpressions() {
                return expressions;
            }

            @Override
            public JobResult<String> run(final Map<String, Serializable> values) {
                return new JobResult<String>() {
                    @Override
                    public String get() {
                        return (String)values.get("prefixed");
                    }

                    @Override
                    public Exception getException() {
                        return null;
                    }
                };
            }

            @Override
            public List<String> validate(Map<String, Serializable> values) {
                return Collections.emptyList();
            }
        };
    }

    private static Job<String> createComplexJob() {
        final List<JobParameterDef> parameterDefs = new ArrayList<>();

        final JobParameterDefAbstract version = new JobParameterDefAbstract(
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
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
            }
        };
        parameterDefs.add(version);

        final JobParameterDefAbstract db = new JobParameterDefAbstract(
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
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
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
        db.addDependency(version.getKey());

        final JobParameterDefAbstract user = new JobParameterDefAbstract(
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
            public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
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
        user.addDependency(db.getKey());
        user.addDependency(version.getKey());

        return new JobAbstract<String>() {
            @Override
            public String getId() {
                return "JobRunnerTest.complexJob";
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public List<JobParameterDef> getParameterDefs() {
                return parameterDefs;
            }

            @Override
            public List<JobExpression> getExpressions() {
                return Collections.emptyList();
            }

            @Override
            public JobResult<String> run(final Map<String, Serializable> values) {
                return new JobResult<String>() {
                    @Override
                    public String get() {
                        return (String) values.get("user");
                    }

                    @Override
                    public Exception getException() {
                        return null;
                    }
                };
            }

//            @Override
//            public JobFuture<String> run(JobValues values) {
//                return () -> values.getValue(user);
//            }

            @Override
            public List<String> validate(Map<String, Serializable> values) {
                return Collections.emptyList();
            }
        };

    }
}