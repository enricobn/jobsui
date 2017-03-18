package org.jobsui.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public class NotEmptyStringValidator implements ParameterValidator {

    @Override
    public List<String> validate(Serializable value) {

        if (value == null) {
            return Collections.singletonList("Value is null.");
        }

        if (value.toString().isEmpty()) {
            return Collections.singletonList("Value is empty.");
        }

        return Collections.emptyList();
    }
}
