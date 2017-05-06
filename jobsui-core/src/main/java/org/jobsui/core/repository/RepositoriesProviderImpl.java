package org.jobsui.core.repository;

import java.util.*;

/**
 * Created by enrico on 5/5/17.
 */
public class RepositoriesProviderImpl implements RepositoriesProvider {
    private final Map<String,Repository> repositories = new LinkedHashMap<>();

    @Override
    public List<Repository> getRepositories() {
        return new ArrayList<>(repositories.values());
    }

    public Repository add(String id, Repository repository) {
        return repositories.put(id, repository);
    }

    public Repository remove(String id) {
        return repositories.remove(id);
    }

    public void removeAll() {
        repositories.clear();
    }
}
