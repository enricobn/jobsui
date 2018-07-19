package org.jobsui.core.utils;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
        if (!folder.delete()) {
            throw new IOException("Cannot delete " + folder);
        }

        if (!folder.mkdir()) {
            throw new IOException("Cannot create folder " + folder);
        }
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

    static String getVersion() {
        try (InputStream versionIs = JobsUIUtils.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(versionIs);
            return properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
