package org.jobsui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIMainParameters {
    private final String projectRoot;
    private final String jobKey;
    private final UIType uiType;

    private JobsUIMainParameters(String projectRoot, String jobKey, UIType uiType) {
        this.projectRoot = projectRoot;
        this.jobKey = jobKey;
        this.uiType = uiType;
    }

    public enum UIType {
        Swing,
        JavFX
    }

    public static void parse(String[] args, Consumer<JobsUIMainParameters> onSuccess, Consumer<List<String>> onError) {
        List<String> validation = new ArrayList<>();

        if (args.length < 2 || args.length > 3) {
            validation.add("Usage: jobsui projectFolder jobkey [ui]");
            onError.accept(validation);
            return;
        }

        String projectRoot = args[0];

        String jobKey = args[1];

        UIType uiType;
        if (args.length >= 3) {
            if ("swing".equals(args[2].toLowerCase())) {
                uiType = UIType.Swing;
            } else if ("javafx".equals(args[2].toLowerCase())) {
                uiType = UIType.JavFX;
            } else {
                uiType = null;
                validation.add("Unknown ui type '" + args[2]);
            }
        } else {
            uiType = UIType.JavFX;
        }
        onSuccess.accept(new JobsUIMainParameters(projectRoot, jobKey, uiType));
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public UIType getUiType() {
        return uiType;
    }

    public String getJobKey() {
        return jobKey;
    }
}
