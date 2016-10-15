package org.jobsui.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public static String join(Collection<String> strings, String delimiter) {
        return strings.stream().collect(Collectors.joining(delimiter));
    }

    public static boolean isNullOrEmptyOrSpaces(String s) {
        return s == null || s.trim().isEmpty();
    }
}
