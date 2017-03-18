package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public interface ParameterValidator {

    List<String> validate(Serializable value);

}
