package org.jobsui.core;

import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.*;
import org.jobsui.core.runner.JobResult;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.ui.*;
import org.jobsui.core.xml.ProjectXML;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.listeners.InvocationListener;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

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
    private static Map<String, Project> projects;
    private static Map<JobType, CachedJob> jobs;
    private JobUIRunner runner;
    private UI<?> ui;
    private FakeUIWindow window;
    private FakeUIButton<?> runButton;
    private FakeUIButton<?> bookmarkButton;

    private static class CachedJob {
        private final Supplier<Job<String>> supplier;
        private Job<String> job = null;

        private CachedJob(Supplier<Job<String>> supplier) {
            this.supplier = supplier;
        }

        public Job<String> get() {
            if (job == null) {
                job = supplier.get();
            }
            return job;
        }
    }

    private enum JobType {
        simpleWithInternalCallFSJob,
        simpleJob,
        simpleFSJob,
        complexJob,
        simpleJobWithExpression
    }

    @BeforeClass
    public static void initStatic() throws Exception {
        projects = new HashMap<>();
        jobs = new HashMap<>();
        jobs.put(JobType.simpleWithInternalCallFSJob, new CachedJob(() -> getJob("src/test/resources/simplejob", "simpleWithInternalCall")));
        jobs.put(JobType.simpleFSJob, new CachedJob(() -> getJob("src/test/resources/simplejob", "simple")));
        jobs.put(JobType.simpleJobWithExpression, new CachedJob(() -> getJob("src/test/resources/simplejob", "simpleWithExpression")));
        jobs.put(JobType.simpleJob, new CachedJob(JobRunnerTest::createSimpleJob));
        jobs.put(JobType.complexJob, new CachedJob(JobRunnerTest::createComplexJob));
    }

    @AfterClass
    public static void teardownStatic() throws Exception {
        jobs = null;
        projects = null;
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

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                    uiValueName.setValue("John");
                    uiValueSurname.setValue("Doe");
                });

        boolean validate = jobRunnerWrapper.interactAndValidate(jobs.get(JobType.simpleJob).get());

        assertThat(validate, is(true));
    }

    @Test public void assert_that_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                    uiValueName.setValue("John");
                    uiValueSurname.setValue("Doe");
                });

        String result = jobRunnerWrapper.run(jobs.get(JobType.simpleJob).get());

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_simplejob_is_not_valid_when_run_with_invalid_parameters() throws Exception {
        FakeUiValue<?> uiValueName = new FakeUiValue<>();
        FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton);

        boolean validate = jobRunnerWrapper.interactAndValidate(jobs.get(JobType.simpleJob).get());

        assertThat(validate, is(false));
    }

    @Test public void assert_that_simplejob_is_not_valid_when_run_with_invalid_parameters_on_interact() throws Exception {
        FakeUiValue<?> uiValueName = new FakeUiValue<>();
        FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                    uiValueName.setValue("John");
                    uiValueSurname.setValue("Doe");
                    uiValueSurname.setValue(null);
                });

        boolean validate = jobRunnerWrapper.interactAndValidate(jobs.get(JobType.simpleJob).get());

        assertThat(validate, is(false));
    }

    @Test public void assert_that_groovy_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                    uiValueName.setValue("John");
                    uiValueSurname.setValue("Doe");
                });

        String result = jobRunnerWrapper.run(jobs.get(JobType.simpleJob).get());

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_loaded_simplejob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                uiValueName.setValue("John");
                uiValueSurname.setValue("Doe");
            });

        String result = jobRunnerWrapper.run(jobs.get(JobType.simpleFSJob).get());

        assertThat(result, equalTo("(John,Doe)"));
    }

    @Test public void assert_that_complexjob_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUIChoice<?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                uiChoiceVersion.setItems(Arrays.asList("1.0", "2.0"));
                uiChoiceVersion.setValue("1.0");
                uiChoiceDb.setValue("Dev-1.0");
            });

        String result = jobRunnerWrapper.run(jobs.get(JobType.complexJob).get());

        assertThat(result, equalTo("1.0 Dev-1.0"));
        assertThat(uiChoiceDb.getItems(), equalTo(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev")));
        assertThat(uiChoiceUser.getItems(), equalTo(Collections.singletonList("1.0 Dev-1.0")));
    }

    @Test public void assert_that_the_default_value_of_a_parameter_triggers_validation() throws Exception {
        final FakeUIChoice<?> uiChoiceVersion = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceDb = new FakeUIChoice<>();
        final FakeUIChoice<?> uiChoiceUser = new FakeUIChoice<>();
        when(ui.create(UIChoice.class)).thenReturn(uiChoiceVersion, uiChoiceDb, uiChoiceUser);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> {
                uiChoiceVersion.setItems(Collections.singletonList("1.0"));
                uiChoiceDb.setItems(Collections.singletonList("Dev-1.0"));
                uiChoiceUser.setItems(Collections.singletonList("John"));
            });

        boolean validate = jobRunnerWrapper.interactAndValidate(jobs.get(JobType.complexJob).get());

        assertThat(validate, is(true));
    }

    @Test public void assert_that_not_valid_parameter_invokes_set_validation_on_widget() throws Exception {
        final FakeUiValue<?> uiValueName = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSurname = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueName, uiValueSurname);
        when(ui.create(UIChoice.class)).thenReturn(new FakeUIChoice());

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<String,Object>(runner, window, runButton,
                () -> uiValueName.setValue(null));

        jobRunnerWrapper.run(jobs.get(JobType.simpleFSJob).get());

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
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        builder.addParameter("name", UIValue.class).build();
        UIValue surnameComponent = builder.addParameter("surname", UIValue.class).build();
        builder.addParameter("inv", UIChoice.class)
                .dependsOn("name")
                .build();

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    // I want to ignore all validations at startup
                    Mockito.reset(job.getParameter("inv"));
                    surnameComponent.setValue(null);
                });

        jobRunnerWrapper.run(job);

        final JobParameterDef invParameter = job.getParameter("inv");
        verify(invParameter, never()).validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class));
        verify(invParameter, never()).validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class));
    }

    @Test public void verify_that_onDepependencyChange_occurs_if_dependencies_are_valid() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue nameComponent = builder.addParameter("name", UIValue.class).build();
        UIValue surnameComponent = builder.addParameter("surname", UIValue.class).build();
        builder.addParameter("inv", UIChoice.class)
                .dependsOn("name")
                .build();

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    nameComponent.setValue("John");
                    surnameComponent.setValue("Doe");
                });

        jobRunnerWrapper.run(job);

        final JobParameterDef invParameter = job.getParameter("inv");
        verify(invParameter).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));
        verify(invParameter, atLeast(1)).validate(anyMapOf(String.class, Serializable.class),
                isNull(Serializable.class));
    }

    @Test public void assert_that_a_message_is_shown_when_job_is_not_valid() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue nameComponent = builder.addParameter("name", UIValue.class).build();
        UIValue surnameComponent = builder.addParameter("surname", UIValue.class).build();
        builder.addParameter("inv", UIChoice.class)
                .dependsOn("name")
                .invisible()
                .onInit(component -> component.setItems(Arrays.asList("Hello", "wold")))
                .onDependenciesChange((component, values) -> component.setValue("Hello"))
                .build();

        final Job<String> job = builder.build();

        when(job.validate(anyMapOf(String.class, Serializable.class))).thenReturn(Collections.singletonList("Error"));

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    nameComponent.setValue("John");
                    surnameComponent.setValue("Doe");
                });

        jobRunnerWrapper.run(job);

        assertEquals(Collections.singletonList("Error"), window.getValidationMessages());
    }

    @Test public void assert_that_groovy_external_concat_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    uiValueFirst.setValue("John");
                    uiValueSecond.setValue(" Doe");
                });

        final Job<String> job = getJob("src/test/resources/external", "concat");

        String result = jobRunnerWrapper.run(job);

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_ext_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    uiValueFirst.setValue("John");
                    uiValueSecond.setValue(" Doe");
                });

        String result = jobRunnerWrapper.run(jobs.get(JobType.simpleWithInternalCallFSJob).get());

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_int_call_job_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();
        final FakeUIChoice<?> uiValueInv = new FakeUIChoice<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);
        when(ui.create(UIChoice.class)).thenReturn(uiValueInv);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    uiValueFirst.setValue("John");
                    uiValueSecond.setValue(" Doe");
                });

        String result = jobRunnerWrapper.run(jobs.get(JobType.simpleWithInternalCallFSJob).get());

        assertThat(result, equalTo("John Doe"));
    }

    @Test public void assert_that_groovy_simple_with_expression_returns_the_correct_value_when_run_with_valid_parameters() throws Exception {
        final FakeUiValue<?> uiValueFirst = new FakeUiValue<>();
        final FakeUiValue<?> uiValueSecond = new FakeUiValue<>();

        when(ui.create(UIValue.class)).thenReturn(uiValueFirst, uiValueSecond);

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    uiValueFirst.setValue("John");
                    uiValueSecond.setValue("Doe");
                });

        final Job<String> job = jobs.get(JobType.simpleJobWithExpression).get();

        String result = jobRunnerWrapper.run(job);

        assertThat(result, equalTo("Mr. John Doe"));
    }

    @Test public void verify_that_onDependenciesChange_does_NOT_occur_if_dependencies_are_NOT_valid() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue nameComponent = builder.addParameter("name", UIValue.class).build();
        UIValue surnameComponent = builder.addParameter("surname", UIValue.class).build();
        builder.addParameter("inv", UIChoice.class)
                .dependsOn("name")
                .invisible()
//                .onInitThen(component -> component.setItems(Arrays.asList("Hello", "world")))
//                .onDependenciesChangeThen(component -> component.setValue("Hello"))
                .build();

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    nameComponent.setValue(null);
                    surnameComponent.setValue(null);
                });

        jobRunnerWrapper.run(job);

        final JobParameterDef inv = job.getParameter("inv");
        verify(inv, never()).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));
    }

    @Test public void assert_that_a_job_gets_expressions_with_dependencies() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue nameComponent = builder.addParameter("name", UIValue.class).build();
        builder.addExpression("mr", values -> "Mr. " + values.get("name"), "name");

        builder.onRun(values -> (String)values.get("mr"));

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> nameComponent.setValue("Jones"));

        assertThat(jobRunnerWrapper.run(job), is("Mr. Jones"));
    }

    /**
     * This test is for testing a bug that was in JobRunnerContext.jobValidationObserver when mixing parameters
     * and expressions due to ordering.
     */
    @Test public void assert_that_mixing_expressions_and_parameters_does_not_fool_dependencies() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue pathComponent = builder.addParameter("path", UIValue.class).build();

        builder.addExpression("config", values -> values.get("path") + "/config", "path");

        builder.addParameter("file", UIChoice.class)
                .dependsOn("config")
                .onDependenciesChange((component, values) -> {
                        // it's needed since it triggers setValue(null) to component, so validation observer is triggered
                        component.setItems(Collections.emptyList());
                })
                .build();

        builder.onRun(values -> (String)values.get("file"));

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> pathComponent.setValue("home"));
        assertThat(jobRunnerWrapper.interactAndValidate(job), is(false));

    }

    /**
     * This test is for testing a bug that was in JobRunnerContext.valuesChangeObserver when mixing parameters
     * and expressions due to ordering.
     */
    @Test public void assert_that_mixing_expressions_and_parameters() throws Exception {
        MockedJobBuilder<String> builder = new MockedJobBuilder<>();

        UIValue pathComponent = builder.addParameter("path", UIValue.class).build();

        builder.addExpression("config", values -> values.get("path") + "/config", "path");

        UIChoice fileComponent = builder.addParameter("file", UIChoice.class)
                .dependsOn("config")
                .onDependenciesChange((component, values) -> {
                    component.setItems(Arrays.asList("one.xml", "two.xml"));
                })
                .build();

        builder.onRun(values -> (String)values.get("config"));

        final Job<String> job = builder.build();

        JobRunnerWrapper<String,?> jobRunnerWrapper = new JobRunnerWrapper<>(runner, window, runButton,
                () -> {
                    pathComponent.setValue("home");
                    fileComponent.setValue("one.xml");
                }
        );

        String result = jobRunnerWrapper.run(job);

        assertThat(runner.isValid(), is(true));

        assertThat(result, is("home/config"));
    }

    private MockSettings printInvocation(String name, final CharSequence methodName) {
        return Mockito.withSettings().name(name).invocationListeners((InvocationListener) methodInvocationReport -> {
            if (methodInvocationReport.getInvocation().toString().contains(methodName)) {
                new RuntimeException().printStackTrace();
            }
        });
    }

    private static <T> Job<T> getJob(String file, String job) {
        Job<T> result;
        try {
            Project project = projects.get(file);
            if (project == null) {
                ProjectXML projectXML = JobParser.getParser(file).parse();
                project = new ProjectGroovyBuilder().build(projectXML);
                projects.put(file, project);
            }
            result = project.getJob(job);
            if (result == null) {
                throw new Exception("Cannot find job with id \"" + job + "\". Ids:" + project.getJobsIds());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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