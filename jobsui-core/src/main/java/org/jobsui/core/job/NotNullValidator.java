package org.jobsui.core.job;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/30/16.
 */
public class NotNullValidator implements JobParameterValidator {
    @Override
    public List<String> validate(Map<String, Serializable> values, Serializable value) {
        if (value == null) {
            return Collections.singletonList("Value is mandatory.");
        }
        return Collections.emptyList();
    }
}
