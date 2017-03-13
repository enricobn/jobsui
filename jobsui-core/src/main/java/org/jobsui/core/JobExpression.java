package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 3/8/17.
 */
public interface JobExpression<T extends Serializable> extends ObservableProducer<T> {

    String getKey();

    String getName();

    boolean isOptional();

    T getDefaultValue();

    List<JobParameterDef<? extends Serializable>> getDependencies();

}
