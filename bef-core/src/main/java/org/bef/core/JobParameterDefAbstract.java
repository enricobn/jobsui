package org.bef.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public abstract class JobParameterDefAbstract<T> implements JobParameterDef<T> {
    private final String key;
//    private final String name;
    private final Class<T> type;
//    private final StringConverter<T> converter;
    private final ParameterValidator<T> validator;
    private final List<JobParameterDef<?>> dependencies = new ArrayList<>();

    public JobParameterDefAbstract(String key, Class<T> type, ParameterValidator<T> validator) {
        this.key = key;
        this.type = type;
//        this.converter = converter;
        this.validator = validator;
    }

    @Override
    public String getKey() {
        return key;
    }

//    @Override
//    public String getName() {
//        return name;
//    }

    @Override
    public Class<T> getType() {
        return type;
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

    public void addDependency(JobParameterDef<?> parameterDef) {
        dependencies.add(parameterDef);
    }
}
