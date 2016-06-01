package org.jobsui.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public class NotEmptyStringValidator implements ParameterValidator<String> {
    @Override
    public List<String> validate(String value) {
        if (value == null) {
            return Collections.singletonList("Value is null.");
        }

        if (value.isEmpty()) {
            return Collections.singletonList("Value is empty.");
        }

        return Collections.emptyList();
    }
}
