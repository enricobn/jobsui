package org.jobsui.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public class NotNullValidator implements ParameterValidator<Object> {
    @Override
    public List<String> validate(Object value) {
        if (value == null) {
            return Collections.singletonList("Value is mandatory.");
        }
        return Collections.emptyList();
    }
}
