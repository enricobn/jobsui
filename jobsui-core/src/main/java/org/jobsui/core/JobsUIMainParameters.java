package org.jobsui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIMainParameters {
    private final UIType uiType;

    private JobsUIMainParameters(UIType uiType) {
        this.uiType = uiType;
    }

    public enum UIType {
        Swing,
        JavFX
    }

    public static boolean parse(String[] args, Consumer<JobsUIMainParameters> onSuccess, Consumer<List<String>> onError) {
        List<String> validation = new ArrayList<>();

        if (args.length > 1) {
            validation.add("Usage: jobsui [ui]");
            onError.accept(validation);
            return false;
        }

        UIType uiType;
        if (args.length >= 1) {
            String uiTypeString = args[0];

            if ("swing".equals(uiTypeString.toLowerCase())) {
                uiType = UIType.Swing;
            } else if ("javafx".equals(uiTypeString.toLowerCase())) {
                uiType = UIType.JavFX;
            } else {
                uiType = null;
                validation.add("Unknown ui type '" + uiTypeString);
            }
        } else {
            uiType = UIType.JavFX;
        }

        if (validation.isEmpty()) {
            onSuccess.accept(new JobsUIMainParameters(uiType));
            return true;
        } else {
            onError.accept(validation);
            return false;
        }
    }

    public UIType getUiType() {
        return uiType;
    }

}
