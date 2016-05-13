package org.bef.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public abstract class JobParameterDefAbstract<T> implements JobParameterDef<T> {
    private final String key;
    private final String name;
//    private final StringConverter<T> converter;
    private final ParameterValidator<T> validator;
    private final List<JobParameterDef<?>> dependencies = new ArrayList<>();
    private final boolean visible;

    public JobParameterDefAbstract(String key, String name, ParameterValidator<T> validator, boolean visible) {
        this.key = key;
        this.name = name;
//        this.converter = converter;
        this.validator = validator;
        this.visible = visible;
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
    public boolean isOptional() {
        return false;
    }

    @Override
    public T getDefaultValue() {
        return null;
    }

    @Override
    public List<JobParameterDef<?>> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public List<String> validate(T value) {
        return validator.validate(value);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void addDependency(JobParameterDef<?> parameterDef) {
        dependencies.add(parameterDef);
    }

    @Override
    public String toString() {
        return getName();
    }
}
