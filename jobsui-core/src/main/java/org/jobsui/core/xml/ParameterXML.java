package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public abstract class ParameterXML extends JobDependencyXML implements ValidatingXML {
    private int order;
    private String name;

    ParameterXML(String key, String name) {
        super(key);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public boolean isOptional() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public List<String> validate() {
        List<String> messages = new ArrayList<>(0);
        if (JobsUIUtils.isNullOrEmptyOrSpaces(getKey())) {
            messages.add("Key is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(name)) {
            messages.add("Name is mandatory.");
        }
        return messages;
    }

}
