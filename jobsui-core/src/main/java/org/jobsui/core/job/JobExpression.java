package org.jobsui.core.job;

import org.jobsui.core.ObservableProducer;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by enrico on 3/8/17.
 */
public interface JobExpression extends JobDependency, ObservableProducer {

    String getName();

    void onDependenciesChange(Map<String, Serializable> values);

    void evaluate(Map<String, Serializable> values);

}
