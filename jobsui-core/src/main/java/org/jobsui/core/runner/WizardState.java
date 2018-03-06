package org.jobsui.core.runner;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;
import org.jobsui.core.xml.WizardStep;
import org.jobsui.core.xml.WizardStepImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by enrico on 5/1/17.
 */
public class WizardState {
    private final Job<?> job;
    private final List<WizardStep> steps;
    private int step = 0;
    private List<String> sortedDependencies;

    public WizardState(Job<?> job) throws Exception {
        this.job = job;
        this.steps = new ArrayList<>(job.getWizardSteps());

        sortedDependencies = job.getSortedDependencies().stream()
                .map(JobDependency::getKey)
                .collect(Collectors.toList());

        Set<String> dependencies = steps.stream()
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

        WizardStepImpl runStep = new WizardStepImpl();
        runStep.setName("Run");

        missedParameters.forEach(runStep::addDependency);
        steps.add(runStep);
    }

    public boolean hasNext() {
        return (step + 1) < steps.size();
    }

    public boolean hasPrevious() {
        return step > 0;
    }

    public <T extends Serializable, C> void next(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        ++step;
        updateWindow(context, window);
    }

    public <T extends Serializable, C> void updateWindow(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        WizardStep wizardStep = steps.get(step);

        window.clear();

        for (String dependency : sortedDependencies) {
            if (wizardStep.getDependencies().contains(dependency)) {
                JobParameter jobParameter = job.getParameter(dependency);
                UIWidget<C> widget = context.getWidget(jobParameter);
                window.add(widget);
            }
        }
    }

    public <T extends Serializable, C> void previous(JobUIRunnerContext<T, C> context, UIWindow<C> window) {
        --step;
        updateWindow(context, window);
    }
}
