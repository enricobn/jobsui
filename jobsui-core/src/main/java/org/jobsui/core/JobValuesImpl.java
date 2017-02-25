package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public class JobValuesImpl implements JobValues {
    private final Map<ParameterDef<? extends Serializable>, Object> values = new HashMap<>();

    @Override
    public <V extends Serializable> void setValue(ParameterDef<V> parameterDef, V value) {
        values.put(parameterDef, value);
    }

    @Override
    public <V extends Serializable> V getValue(ParameterDef<V> parameterDef) {
        return (V) values.get(parameterDef);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        values.entrySet().forEach(e -> stringBuilder.append(e.getKey().getName())
                .append("=")
                .append(e.getValue())
                .append("\n")
        );
        return stringBuilder.toString();
    }
}
