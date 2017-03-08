package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 3/6/17.
 */
public class Bookmark {
    private final String name;
    private final Map<String, Serializable> values = new HashMap<>();

    public Bookmark(Job<?> job, String name, JobValues values) {
        this.name = name;
        for (JobParameterDef<? extends Serializable> parameterDef : job.getParameterDefs()) {
            if (!parameterDef.isCalculated()) {
                this.values.put(parameterDef.getKey(), values.getValue(parameterDef));
            }
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getValues() {
        return values;
    }
}
