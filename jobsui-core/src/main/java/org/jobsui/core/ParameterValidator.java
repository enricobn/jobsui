package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public interface ParameterValidator<T extends Serializable> {

    List<String> validate(T value);
}
