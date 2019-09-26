package org.jobsui.core.runner;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;
import org.jobsui.core.xml.JobPage;
import org.jobsui.core.xml.JobPageImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by enrico on 5/1/17.
 */
class JobPagesState {
    private final Job<?> job;
    private final List<JobPage> pages;
    private int page = 0;
    private List<String> sortedDependencies;

    JobPagesState(Job<?> job) throws Exception {
        this.job = job;
        this.pages = new ArrayList<>(job.getJobPages());

        sortedDependencies = job.getSortedDependencies().stream()
                .map(JobDependency::getKey)
                .collect(Collectors.toList());

        Set<String> dependencies = pages.stream()
                .flatMap(step -> step.getDependencies().stream())
                .collect(Collectors.toSet());

        List<String> missedParameters;
        try {
            missedParameters = job.getSortedDependencies().stream()
                    .map(JobDependency::getKey)
                    .filter(key -> job.getParameter(key) != null && !dependencies.contains(key))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!missedParameters.isEmpty()) {
            JobPageImpl runStep = new JobPageImpl();
            runStep.setName("Run");

            missedParameters.forEach(runStep::addDependency);
            pages.add(runStep);
        }
    }

    boolean hasNext() {
        return (page + 1) < pages.size();
    }

    boolean hasPrevious() {
        return page > 0;
    }

    <T extends Serializable, C> void next(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        ++page;
        updateWindow(context, window);
    }

    <T extends Serializable, C> void updateWindow(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        JobPage jobPage = pages.get(page);

        window.clear();

        for (String dependency : sortedDependencies) {
            if (jobPage.getDependencies().contains(dependency)) {
                JobParameter jobParameter = job.getParameter(dependency);
                UIWidget<C> widget = context.getWidget(jobParameter);
                window.add(widget);
            }
        }
    }

    <T extends Serializable, C> void previous(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        --page;
        updateWindow(context, window);
    }
}
