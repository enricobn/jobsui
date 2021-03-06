package org.jobsui.core.job;

import java.io.Serializable;
import java.util.*;

/**
 * Created by enrico on 4/30/16.
 */
public abstract class JobParameterAbstract implements JobParameter {
    private final String key;
    private final String name;
//    private final StringConverter<T> converter;
    private final JobParameterValidator validator;
    private final List<String> dependencies = new ArrayList<>();
    private final boolean optional;
    private final boolean visible;

    public JobParameterAbstract(String key, String name, JobParameterValidator validator, boolean optional, boolean visible) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(name);
        this.key = key;
        this.name = name;
//        this.converter = converter;
        this.validator = validator;
        this.optional = optional;
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
        return optional;
    }

    @Override
    public Serializable getDefaultValue() {
        return null;
    }

    @Override
    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public List<String> validate(Map<String, Serializable> values, Serializable value) {
        if (!isOptional()) {
            final List<String> validate = new NotNullValidator().validate(values, value);
            if (!validate.isEmpty()) {
                return validate;
            }
        }
        if (validator == null) {
            return Collections.emptyList();
        }
        return validator.validate(values, value);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void addDependency(String key) {
        dependencies.add(key);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobParameterAbstract that = (JobParameterAbstract) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
