package org.bef.core;

import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public interface ParameterValidator<T> {

    List<String> validate(T value);
}
