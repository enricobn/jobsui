package org.jobsui.core.runner;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobResult<T> {

    T get();

    Exception getException();

}
