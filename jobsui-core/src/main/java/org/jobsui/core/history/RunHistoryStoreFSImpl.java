package org.jobsui.core.history;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.jobstore.JobStoreElementImpl;
import org.jobsui.core.jobstore.JobStoreFSImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RunHistoryStoreFSImpl extends JobStoreFSImpl<RunHistory> implements RunHistoryStore {
    static int MAX_ELEMENTS = 5;
    RunHistoryStoreFSImpl(File root) {
        super(root);
    }

    @Override
    protected String getFileNameWithoutExtension(RunHistory value) {
        return value.getDateTime().toString();
    }

    public static RunHistoryStore getUserStore() {
        return new RunHistoryStoreFSImpl(
                Paths.get(
                        System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
                        ".jobsui",
                        "runhistory"
                ).toFile());
    }

    @Override
    public Optional<RunHistory> getLast(Project project, Job job) {
        List<RunHistory> runHistories = get(project, job);
        return runHistories.stream().max(Comparator.comparing(RunHistory::getDateTime));
    }

    @Override
    public void save(Project project, Job job, RunHistory value) throws IOException {
        super.save(project, job, value);

        List<RunHistory> runHistories = get(project, job);

        if (runHistories.size() > MAX_ELEMENTS) {
            Set<String> keysToRetain = runHistories.stream()
                    .sorted(Comparator.comparing(RunHistory::getDateTime).reversed())
                    .limit(MAX_ELEMENTS)
                    .map(JobStoreElementImpl::getKey)
                    .collect(Collectors.toSet());
            runHistories.forEach(it -> {
                if (!keysToRetain.contains(it.getKey())) {
                    delete(project, job, it);
                }
            });
        }
    }
}
