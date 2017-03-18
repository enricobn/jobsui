package org.jobsui.core.ui;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by enrico on 4/29/16.
 */
public class StringConverterString implements StringConverter<Serializable> {
    @Override
    public String toString(Serializable value) {
        return Objects.toString(value);
    }

    @Override
    public String fromString(String value) {
        return value;
    }
}
