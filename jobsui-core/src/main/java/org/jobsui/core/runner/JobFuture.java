package org.jobsui.core.runner;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobFuture<T> {

    T get();

    Exception getException();

}
