package org.jobsui.core.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
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
        return repl(" ", count);
    }

    static File createTempDir(String prefix, String suffix) throws IOException {
        File folder = File.createTempFile(prefix, suffix);
        folder.deleteOnExit();
        assert(folder.delete());

        assert(folder.mkdir());
        return folder;
    }

    static String repl(String s, int times) {
        return String.join("", Collections.nCopies(times, s));
    }

    static String toString(Throwable th) {
        try (StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw)) {

            th.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
