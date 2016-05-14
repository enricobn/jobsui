package org.bef.core;

import org.bef.core.ui.FakeUIWindow;
import org.bef.core.ui.UI;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by enrico on 5/10/16.
 */
public abstract class JobRunnerWrapper<T> {
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final FakeUIWindow window;
    private final JobRunner runner;
    private final UI ui;

    protected JobRunnerWrapper(JobRunner runner, UI ui, FakeUIWindow window) {
        this.runner = runner;
        this.ui = ui;
        this.window = window;
    }

    public JobFuture<T> start(Job<T> job) throws Exception {

        final Future<JobFuture<T>> future = runJob(job);

        window.waitUntilStarted();

        interact();

        window.exit();

        return future.get();
    }

    private <T1> Future<JobFuture<T1>> runJob(final Job<T1> job) {
        return pool.submit(new Callable<JobFuture<T1>>() {
            @Override
            public JobFuture<T1> call() throws Exception {
                try {
                    return runner.run(ui, job);
                } catch (Throwable th) {
                    th.printStackTrace();
                    return null;
                }
            }
        });
    }

    protected abstract void interact();
}
