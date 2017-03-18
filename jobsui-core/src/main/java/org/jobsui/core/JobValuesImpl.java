package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public class JobValuesImpl implements JobValues {
    private final Map<ParameterDef, Serializable> values = new HashMap<>();

    @Override
    public void setValue(ParameterDef parameterDef, Serializable value) {
        values.put(parameterDef, value);
    }

    @Override
    public Serializable getValue(ParameterDef parameterDef) {
        return values.get(parameterDef);
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
