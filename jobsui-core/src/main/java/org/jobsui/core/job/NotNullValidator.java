package org.jobsui.core.job;

import org.jobsui.core.job.ParameterValidator;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/30/16.
 */
public class NotNullValidator implements ParameterValidator {
    @Override
    public List<String> validate(Map<String, Serializable> values, Serializable value) {
        if (value == null) {
            return Collections.singletonList("Value is mandatory.");
        }
        return Collections.emptyList();
    }
}