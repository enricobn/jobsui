package org.jobsui.core;

import java.io.Serializable;

/**
 * Created by enrico on 2/24/17.
 */
public interface JobValues {

    <V extends Serializable> void setValue(ParameterDef<V> parameterDef, V value);

    <V extends Serializable> V getValue(ParameterDef<V> parameterDef);

    void clear();
}
