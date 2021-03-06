package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 10/11/16.
 */
public class CallXML extends ParameterXML {
    private final Map<String, String> map = new HashMap<>();
    private String project;
    private String job;

    public CallXML(String key, String name) {
        super(key, name);
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void addMap(String in, String out) {
        map.put(in, out);
    }

    public String getProject() {
        return project;
    }

    public String getJob() {
        return job;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public List<String> validate() {
        List<String> messages = super.validate();
        if (JobsUIUtils.isNullOrEmptyOrSpaces(project)) {
            messages.add("Project is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(job)) {
            messages.add("Job is mandatory.");
        }

        // TODO map

        return messages;
    }

    @Override
    public ParameterXML copy() {
        CallXML copy = new CallXML(getKey() + "_copy", getName() + " copy");
        getDependencies().forEach(copy::addDependency);
        copy.setProject(project);
        copy.setJob(job);

        map.forEach(this::addMap);

        return copy;
    }

}
