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
abstract class JobRunnerWrapper<T extends Serializable> {
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final FakeUIWindow window;
    private final JobRunner runner;
    private final UI<T> ui;
    private final FakeUIButton<?> runButton;

    JobRunnerWrapper(JobRunner runner, UI<T> ui, FakeUIWindow window, FakeUIButton<?> runButton) {
        this.runner = runner;
        this.ui = ui;
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

    private <T1 extends Serializable> Future<T1> runJob(final Job<T1> job) {
        return pool.submit(() -> {
            try {
                return runner.run(ui, job);
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
