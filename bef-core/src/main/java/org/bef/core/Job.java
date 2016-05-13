package org.bef.core;

import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job<T> {

    String getName();

    List<JobParameterDef<?>> getParameterDefs();

    JobFuture<T> run(Map<String,Object> parameters);

    List<String> validate(Map<String,Object> parameters);

    JobParameterDef getParameter(String key);
}
