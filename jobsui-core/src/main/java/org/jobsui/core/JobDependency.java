package org.jobsui.core;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 3/19/17.
 */
public interface JobDependency {

    /**
     * Dependencies from other elements are ignored. Validation is out of scope for this method.
     */
    static List<String> getSortedDependenciesKeys(Collection<? extends JobDependency> jobDependencies) throws Exception {
        Map<String, List<String>> toSort = new LinkedHashMap<>();

        jobDependencies.forEach(jobDependency -> toSort.put(jobDependency.getKey(),
                new ArrayList<>(jobDependency.getDependencies())));

        // I remove dependencies which are not in jobDependencies.
        toSort.forEach((key,dep) -> dep.retainAll(toSort.keySet()));

        List<String> sorted = new ArrayList<>();

        while (!toSort.isEmpty()) {
            boolean found = false;
            for (Map.Entry<String, List<String>> entry : toSort.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    String key = entry.getKey();
                    sorted.add(key);
                    toSort.remove(key);
                    for (List<String> dependencies : toSort.values()) {
                        dependencies.remove(key);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                StringBuilder sb = new StringBuilder("Unresolved dependencies:\n");
                toSort.entrySet()
                        .forEach(entry -> sb.append(entry.getKey()).append(":").append(entry.getValue()).append('\n'));
                throw new Exception(sb.toString());
            }
        }
        return sorted;
    }

    static <T extends JobDependency> List<T> sort(List<T> jobDependencies) throws Exception {
        List<String> sortedDependencies = getSortedDependenciesKeys(jobDependencies);

        return jobDependencies.stream().sorted((o1, o2) -> {
            int i1 = sortedDependencies.indexOf(o1.getKey());
            int i2 = sortedDependencies.indexOf(o2.getKey());
            return i1 -i2;
        }).collect(Collectors.toList());
    }

    String getName();

    String getKey();

    List<String> getDependencies();

}
