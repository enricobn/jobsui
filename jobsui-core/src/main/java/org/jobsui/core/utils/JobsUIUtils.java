package org.jobsui.core.utils;

import java.util.List;

/**
 * Created by enrico on 5/16/16.
 */
public abstract class JobsUIUtils {

    public static String getMessagesAsString(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(message);
        }
        return sb.toString();
    }
}
