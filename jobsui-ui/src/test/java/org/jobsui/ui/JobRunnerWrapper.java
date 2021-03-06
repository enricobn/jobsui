package org.jobsui.ui;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.utils.Tuple2;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by enrico on 5/10/16.
 */
public class JobRunnerWrapper<T extends Serializable, C> {
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final FakeUIWindow window;
    private final JobUIRunner<C> runner;
    private final FakeUIButton runButton;

    JobRunnerWrapper(JobUIRunner<C> runner, FakeUIWindow window, FakeUIButton runButton) {
        this.runner = runner;
        this.window = window;
        this.runButton = runButton;
    }

    public boolean interactAndValidate(Tuple2<Project,Job<T>> projectJob) {
        return interactAndValidate(projectJob, null);
    }

    public boolean interactAndValidate(Tuple2<Project,Job<T>> projectJob, Runnable interaction) {
        return interactAndValidate(projectJob.first, projectJob.second, interaction);
    }

    public boolean interactAndValidate(Project project, Job<T> job, Runnable interaction) {
        runJob(project, job);

        window.waitUntilStarted();

        try {
            interact(interaction);
        } finally {
            window.exit();
        }
        return runner.isValid();
    }

    T run(Tuple2<Project,Job<T>> projectJob) throws Exception {
        return run(projectJob, null);
    }

    T run(Tuple2<Project,Job<T>> projectJob, Runnable interaction) throws Exception {
        return run(projectJob.first, projectJob.second, interaction);
    }

    T run(Project project, Job<T> job, Runnable interaction) throws Exception {

        final Future<T> future = runJob(project, job);

        window.waitUntilStarted();

        try {
            interact(interaction);
            runButton.click();
        } finally {
            window.exit();
        }
        return future.get();
    }

    private Future<T> runJob(Project project, final Job<T> job) {
        return pool.submit(() -> {
            try {
                return runner.run(project, job);
//                return job.run(values);
//                return runner.run(ui, job);
            } catch (Exception e) {
                window.exit();
                e.printStackTrace();
                throw e;
            }
        });
    }

    private void interact(Runnable interaction) {
        if (interaction != null) {
            interaction.run();
        }
    }

}
