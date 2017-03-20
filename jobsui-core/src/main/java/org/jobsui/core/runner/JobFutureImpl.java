package org.jobsui.core.runner;

/**
 * Created by enrico on 3/19/17.
 */
public class JobFutureImpl<T> implements JobFuture<T> {
    private T value;
    private Exception exception;

    public JobFutureImpl(T value) {
        this.value = value;
    }

    public JobFutureImpl(Exception exception) {
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
