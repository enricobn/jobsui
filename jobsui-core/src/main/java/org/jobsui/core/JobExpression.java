package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 3/8/17.
 */
public interface JobExpression extends ObservableProducer {

    String getKey();

    String getName();

    boolean isOptional();

    Serializable getDefaultValue();

    List<JobParameterDef> getDependencies();

}
