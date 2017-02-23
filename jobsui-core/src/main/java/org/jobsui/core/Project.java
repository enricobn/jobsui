package org.jobsui.core;

import java.util.Set;

/**
 * Created by enrico on 5/6/16.
 */
public interface Project {

    <T> Job<T> getJob(String key);

    Set<String> getIds();

    String getName();
}
