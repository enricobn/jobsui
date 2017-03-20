package org.jobsui.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jobsui.core.job.JobExpression;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by enrico on 3/19/17.
 */
public class JobExpressionGroovy implements JobDependencyGroovy,JobExpression {
    private static final String IMPORTS =
            "import org.jobsui.core.*;\n" +
            "import org.jobsui.core.ui.*;\n";
    private final String key;
    private final String name;
    private final List<String> dependencies = new ArrayList<>();
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final Observable<Serializable> observable;
    private final Script evaluate;
    private final Binding shellBinding;

    public JobExpressionGroovy(GroovyShell shell, String key, String name, String evaluateScript) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(name);
        Objects.requireNonNull(dependencies);
        this.key = key;
        this.name = name;
        this.observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        try {
            this.evaluate = shell.parse(IMPORTS + evaluateScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing evaluate for expression with key \"" + key + "\".", e);
        }
        shellBinding = shell.getContext();
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public void onDependenciesChange(Map<String, Serializable> values) {
        evaluate(values);
    }

    public void evaluate(Map<String, Serializable> values) {
        // I reset the bindings otherwise I get "global" or previous bindings
        evaluate.setBinding(new Binding(shellBinding.getVariables()));
        evaluate.setProperty("values", values);
        for (Map.Entry<String, Serializable> entry : values.entrySet()) {
            evaluate.setProperty(entry.getKey(), entry.getValue());
        }

        try {
            Serializable value = (Serializable) evaluate.run();
            for (Subscriber<? super Serializable> subscriber : subscribers) {
                subscriber.onNext(value);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error in evaluate script for expression whit key \"" +
                    getKey() + "\"", e);
        }
    }

    @Override
    public void addDependency(String key) {
        dependencies.add(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobExpressionGroovy that = (JobExpressionGroovy) o;

        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
