package org.jobsui.ui;

import org.jobsui.core.job.*;
import org.jobsui.core.runner.JobResultImpl;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.ui.*;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by enrico on 4/2/17.
 */
class MockedJobBuilder<T> {
    private final Map<String, JobParameter> parameters = new LinkedHashMap<>();
    private final Map<String, JobExpression> expressions = new LinkedHashMap<>();
    private final Job<T> job = mock(Job.class);
    private boolean adding = false;
    private Function<Map<String, Serializable>, T> onRun;

    <COMP extends UIComponent> MockedParameter<COMP> addParameter(String key, Class<COMP> componentTYpe) {
        if (adding) {
            throw new IllegalStateException("Cannot add another parameter before creating the previous. Forgotten a create()?.");
        }
        adding = true;
        final JobParameter jobParameter = mock(JobParameter.class, key);
        when(jobParameter.getKey()).thenReturn(key);
        when(jobParameter.getName()).thenReturn(key);
        parameters.put(key, jobParameter);
        return new MockedParameter<>(componentTYpe, jobParameter);
    }

    void addExpression(String key, Function<Map<String, Serializable>, Serializable> evaluate, String... dependencies) {
        if (adding) {
            throw new IllegalStateException("Cannot add another parameter before creating the previous. Forgotten a create()?.");
        }

        final JobExpression jobExpression = mock(JobExpression.class, key);
        when(jobExpression.getKey()).thenReturn(key);

        List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();

        Observable<Serializable> observable = Observable.create(subscribers::add);

        when(jobExpression.getObservable()).thenReturn(observable);

        when(jobExpression.evaluate(anyMapOf(String.class, Serializable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Serializable> values = (Map<String, Serializable>) invocation.getArguments()[0];
                    return evaluate.apply(values);
                });

        doAnswer(invocation -> {
            Serializable value = (Serializable) invocation.getArguments()[0];
            for (Subscriber<? super Serializable> subscriber : subscribers) {
                subscriber.onNext(value);
            }
            return null;
        }).when(jobExpression).notifySubscribers(any(Serializable.class));

        List<String> dependenciesList = Arrays.asList(dependencies);
        when(jobExpression.getDependencies()).thenReturn(dependenciesList);
        try {
            when(jobExpression.getSortedDependencies(any(JobDependencyProvider.class)))
                    .thenReturn(dependenciesList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        expressions.put(key, jobExpression);
    }

    void onRun(Function<Map<String, Serializable>, T> onRun) {
        this.onRun = onRun;
    }

    Job<T> build() {
        if (adding) {
            throw new IllegalStateException("Cannot create job while creating a parameter. Forgotten a create()?.");
        }

        when(job.getParameterDefs()).thenReturn(new ArrayList<>(parameters.values()));
        when(job.getParameter(anyString())).thenAnswer(invocation ->
                parameters.get(invocation.getArguments()[0].toString())
        );

        when(job.getExpressions()).thenReturn(new ArrayList<>(expressions.values()));
        when(job.getExpression(anyString())).thenAnswer(invocation ->
                expressions.get(invocation.getArguments()[0].toString())
        );

        when(job.getJobDependency(anyString())).thenAnswer(invocation -> {
            String key = (String) invocation.getArguments()[0];
            JobDependency jobDependency = parameters.get(key);
            if (jobDependency == null) {
                jobDependency = expressions.get(key);
            }
            return jobDependency;
        });

        List<JobDependency> jobDependencies = new ArrayList<>(parameters.values());
        jobDependencies.addAll(expressions.values());

        List<JobDependency> sortedDependencies;
        try {
            sortedDependencies = JobDependency.sort(jobDependencies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            when(job.getSortedDependencies()).thenReturn(sortedDependencies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(job.getUnsortedDependencies()).thenReturn(jobDependencies);

        if (onRun == null) {
            when(job.run(anyMapOf(String.class, Serializable.class))).thenReturn(new JobResultImpl<>((T) null));
            when(job.run(any(JobValues.class))).thenReturn(new JobResultImpl<>((T) null));
        } else {
            when(job.run(anyMapOf(String.class, Serializable.class)))
                    .thenAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        Map<String,Serializable> values = (Map<String, Serializable>) invocation.getArguments()[0];
                        return new JobResultImpl<>(onRun.apply(values));
                    });
            when(job.run(any(JobValues.class)))
                    .thenAnswer(invocation -> {
                        JobValues values = (JobValues) invocation.getArguments()[0];
                        return new JobResultImpl<>(onRun.apply(values.getMap(job)));
                    });
        }
        return job;
    }

    public class MockedParameter<COMP extends UIComponent> {
        private final Class<COMP> componentTYpe;
        private final JobParameter jobParameter;
        private boolean visible = true;
        private BiConsumer<COMP,Map<String,Serializable>> onDependenciesChange;
        private Consumer<COMP> onInit;

        MockedParameter(Class<COMP> componentTYpe, JobParameter jobParameter) {
            this.componentTYpe = componentTYpe;
            this.jobParameter = jobParameter;
        }

        public MockedParameter<COMP> withName(String name) {
            when(jobParameter.getName()).thenReturn(name);
            return this;
        }

        MockedParameter<COMP> dependsOn(String... dependencies) {
            List<String> dependenciesList = Arrays.asList(dependencies);
            when(jobParameter.getDependencies()).thenReturn(dependenciesList);
            try {
                when(jobParameter.getSortedDependencies(any(JobDependencyProvider.class)))
                        .thenReturn(dependenciesList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        MockedParameter<COMP> onDependenciesChange(BiConsumer<COMP, Map<String, Serializable>> onDependenciesChange) {
            this.onDependenciesChange = onDependenciesChange;
            return this;
        }

        MockedParameter<COMP> onInit(Consumer<COMP> onInit) {
            this.onInit = onInit;
            return this;
        }

        MockedParameter<COMP> invisible() {
            this.visible = false;
            return this;
        }

        COMP build() {
            final COMP uiComponent;

            if (componentTYpe.equals(UIValue.class)) {
                uiComponent = (COMP) new FakeUiValue();
            } else if (componentTYpe.equals(UIChoice.class)) {
                uiComponent = (COMP) new FakeUIChoice();
            } else {
                throw new IllegalArgumentException();
            }

            try {
                when(jobParameter.createComponent(any(UI.class))).thenReturn(uiComponent);
            } catch (UnsupportedComponentException e) {
                throw new RuntimeException(e);
            }

            when(jobParameter.isVisible()).thenReturn(visible);
            when(jobParameter.validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class))).thenReturn(Collections.singletonList("Error"));
            when(jobParameter.validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class))).thenReturn(Collections.emptyList());

            if (onInit != null) {
                onInit.accept(uiComponent);
            }

            if (onDependenciesChange != null) {
                doAnswer(invocation -> {
                    Map<String,Serializable> values = (Map<String, Serializable>) invocation.getArguments()[1];
//                    uiChoiceInv.setValue(values.get("name"));
                    onDependenciesChange.accept(uiComponent, values);
                    return null;
                }).when(jobParameter).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));
            }

            adding = false;

            return uiComponent;
        }
    }

}
