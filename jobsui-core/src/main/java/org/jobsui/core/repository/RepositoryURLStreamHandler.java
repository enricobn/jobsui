package org.jobsui.core.repository;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Created by enrico on 5/4/17.
 */
public class RepositoryURLStreamHandler extends URLStreamHandler {
    private final RepositoriesProvider repositoriesProvider;

    public RepositoryURLStreamHandler(RepositoriesProvider repositoriesProvider) {
        this.repositoriesProvider = repositoriesProvider;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        for (Repository repository : repositoriesProvider.getRepositories()) {
            try {
                return repository.openConnection(url);
            } catch (Exception e) {
            }
        }
        throw new IOException("Cannot find a repository for resource " + url);
    }

}
