package org.jobsui.core.runner;

/**
 * Created by enrico on 3/19/17.
 */
public class JobResultImpl<T> implements JobResult<T> {
    private T value;
    private Exception exception;

    public JobResultImpl(T value) {
        this.value = value;
    }

    public JobResultImpl(Exception exception) {
        this.exception = exception;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Exception getException() {
        return exception;
    }
}
