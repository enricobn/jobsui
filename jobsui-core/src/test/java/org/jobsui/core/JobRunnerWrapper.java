package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.runner.JobRunner;
import org.jobsui.core.ui.FakeUIButton;
import org.jobsui.core.ui.FakeUIWindow;
import org.jobsui.core.ui.UI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by enrico on 5/10/16.
 */
abstract class JobRunnerWrapper<T extends Serializable, C> {
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final FakeUIWindow window;
    private final JobRunner<C> runner;
    private final FakeUIButton<?> runButton;

    JobRunnerWrapper(JobRunner<C> runner, FakeUIWindow window, FakeUIButton<?> runButton) {
        this.runner = runner;
        this.window = window;
        this.runButton = runButton;
    }

    T start(Job<T> job) throws Exception {

        final Future<T> future = runJob(job);

        window.waitUntilStarted();

        interact();

        runButton.click();

        window.exit();

        return future.get();
    }

    private Future<T> runJob(final Job<T> job) {
        return pool.submit(() -> {
            try {
                return runner.run(job);
//                return job.run(values);
//                return runner.run(ui, job);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }

    protected abstract void interact();

    public boolean isValid() {
        return runner.isValid();
    }
}
