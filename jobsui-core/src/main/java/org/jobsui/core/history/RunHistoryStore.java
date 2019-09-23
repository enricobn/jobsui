package org.jobsui.core.history;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.jobstore.JobStore;

import java.util.Optional;

public interface RunHistoryStore extends JobStore<RunHistory> {

    Optional<RunHistory> getLast(Project project, Job job);

}
