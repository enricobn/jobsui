package org.jobsui.core.xml;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXML {
    private final String key;
    private final String name;
    private String runScript;
    private String validateScript;

    public JobXML(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setRunScript(String runScript) {
        this.runScript = runScript;
    }

    public void setValidateScript(String validateScript) {
        this.validateScript = validateScript;
    }
}
