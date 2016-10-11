package org.jobsui.core.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 10/11/16.
 */
public class CallXML extends ParameterXML {

    private String project;
    private String job;
    private Map<String, String> map = new HashMap<>();

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
}
