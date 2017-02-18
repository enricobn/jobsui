package org.jobsui.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job<T> {

    String getName();

    List<JobParameterDef<? extends Serializable>> getParameterDefs();

    JobFuture<T> run(Map<String,Object> values);

    List<String> validate(Map<String,Object> values);

    JobParameterDef<?> getParameter(String key);
}
