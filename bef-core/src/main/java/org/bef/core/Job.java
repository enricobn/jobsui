package org.bef.core;

import java.util.List;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job {

    List<JobParameterDef> getParameterDefs();

    JobFuture run(List<JobParameter> parameters);

    List<String> validate(List<JobParameter> parameters);

}
