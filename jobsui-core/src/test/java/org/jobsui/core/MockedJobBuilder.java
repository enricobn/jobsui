package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobExpression;
import org.jobsui.core.job.JobParameterDef;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Created by enrico on 4/2/17.
 */
public class MockedJobBuilder<T> {
    private final Map<String, JobParameterDef> parameters = new LinkedHashMap<>();
    private final Map<String, JobExpression> expressions = new LinkedHashMap<>();
    private final Job<T> job = mock(Job.class);
    private boolean adding = false;
    private Function<Map<String, Serializable>, T> onRun;

    public <COMP extends UIComponent> MockedParameter<COMP> addParameter(String key, Class<COMP> componentTYpe) {
        if (adding) {
            throw new IllegalStateException("Cannot add another parameter before creating the previous. Forgotten a create()?.");
        }
        adding = true;
        final JobParameterDef jobParameterDef = mock(JobParameterDef.class, key);
        when(jobParameterDef.getKey()).thenReturn(key);
        when(jobParameterDef.getName()).thenReturn(key);
        parameters.put(key, jobParameterDef);
        return new MockedParameter<>(componentTYpe, jobParameterDef);
    }

    public void addExpression(String key, Function<Map<String,Serializable>, Serializable> evaluate, String... dependencies) {
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

        when(jobExpression.getDependencies()).thenReturn(Arrays.asList(dependencies));

        expressions.put(key, jobExpression);
    }

    public void onRun(Function<Map<String,Serializable>, T> onRun) {
        this.onRun = onRun;
    }

    public Job<T> build() {
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
        private final JobParameterDef jobParameterDef;
        private boolean visible = true;
        private BiConsumer<COMP,Map<String,Serializable>> onDependenciesChange;
        private Consumer<COMP> onInit;

        public MockedParameter(Class<COMP> componentTYpe, JobParameterDef jobParameterDef) {
            this.componentTYpe = componentTYpe;
            this.jobParameterDef = jobParameterDef;
        }

        public MockedParameter<COMP> withName(String name) {
            when(jobParameterDef.getName()).thenReturn(name);
            return this;
        }

        public MockedParameter<COMP> dependsOn(String... dependencies) {
            when(jobParameterDef.getDependencies()).thenReturn(Arrays.asList(dependencies));
            return this;
        }

        public MockedParameter<COMP> onDependenciesChange(BiConsumer<COMP,Map<String,Serializable>> onDependenciesChange) {
            this.onDependenciesChange = onDependenciesChange;
            return this;
        }

        public MockedParameter<COMP> onInit(Consumer<COMP> onInit) {
            this.onInit = onInit;
            return this;
        }

        public MockedParameter<COMP> invisible() {
            this.visible = false;
            return this;
        }

        public COMP build() {
            final COMP uiComponent;

            if (componentTYpe.equals(UIValue.class)) {
                uiComponent = (COMP) new FakeUiValue();
            } else if (componentTYpe.equals(UIChoice.class)) {
                uiComponent = (COMP) new FakeUIChoice();
            } else {
                throw new IllegalArgumentException();
            }

            try {
                when(jobParameterDef.createComponent(any(UI.class))).thenReturn((UIComponent<Object>)uiComponent);
            } catch (UnsupportedComponentException e) {
                throw new RuntimeException(e);
            }

            when(jobParameterDef.isVisible()).thenReturn(visible);
            when(jobParameterDef.validate(anyMapOf(String.class, Serializable.class), isNull(Serializable.class))).thenReturn(Collections.singletonList("Error"));
            when(jobParameterDef.validate(anyMapOf(String.class, Serializable.class), isNotNull(Serializable.class))).thenReturn(Collections.emptyList());

            if (onInit != null) {
                onInit.accept(uiComponent);
            }

            if (onDependenciesChange != null) {
                doAnswer(invocation -> {
                    Map<String,Serializable> values = (Map<String, Serializable>) invocation.getArguments()[1];
//                    uiChoiceInv.setValue(values.get("name"));
                    onDependenciesChange.accept(uiComponent, values);
                    return null;
                }).when(jobParameterDef).onDependenciesChange(any(UIWidget.class), anyMapOf(String.class, Serializable.class));
            }

            adding = false;

            return uiComponent;
        }
    }

}
