package org.jobsui.core;

import org.jobsui.core.ui.FakeUIWindow;
import org.jobsui.core.ui.UI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by enrico on 5/10/16.
 */
abstract class JobRunnerWrapper<T extends Serializable> {
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final FakeUIWindow window;
    private final JobRunner runner;
    private final UI<T> ui;

    JobRunnerWrapper(JobRunner runner, UI<T> ui, FakeUIWindow window) {
        this.runner = runner;
        this.ui = ui;
        this.window = window;
    }

    JobFuture<T> start(Job<T> job) throws Exception {

        final Future<JobFuture<T>> future = runJob(job);

        window.waitUntilStarted();

        interact();

        window.exit();

        return future.get();
    }

    private <T1 extends Serializable> Future<JobFuture<T1>> runJob(final Job<T1> job) {
        return pool.submit(() -> {
            try {
                return runner.run(ui, job);
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        });
    }

    protected abstract void interact();
}
