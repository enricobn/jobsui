package org.jobsui.core.repository;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Objects;

/**
 * Created by enrico on 5/4/17.
 */
public class RepositoryURLStreamHandlerFactory implements URLStreamHandlerFactory {
    public static final String PROTOCOL = "repository";
    private static final RepositoryURLStreamHandlerFactory instance = new RepositoryURLStreamHandlerFactory();
    private final RepositoriesProviderImpl provider = new RepositoriesProviderImpl();
    private final RepositoryURLStreamHandler streamHandler = new RepositoryURLStreamHandler(provider);

    public static RepositoryURLStreamHandlerFactory getInstance() {
        return instance;
    }

    private RepositoryURLStreamHandlerFactory() {
        URL.setURLStreamHandlerFactory(this);
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals(PROTOCOL)) {
            return streamHandler;
        }
        return null;
    }

    public void add(String id, Repository repository) {
        Objects.requireNonNull(repository);
        provider.add(id, repository);
    }

    public void clear() {
        provider.removeAll();
    }
}
