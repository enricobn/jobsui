package org.jobsui.core.utils;

import java.util.List;

/**
 * Created by enrico on 5/16/16.
 */
public interface JobsUIUtils {

    static String getMessagesAsString(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(message);
        }
        return sb.toString();
    }

    static boolean isNullOrEmptyOrSpaces(String s) {
        return s == null || s.trim().isEmpty();
    }

    static int leadingSpaces(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    static String spaces(int count) {
        if (count <= 0) {
            return "";
        }
        return new String(new char[count]).replace("\0", " ");
    }
}
