package org.jobsui.core.xml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by enrico on 4/5/17.
 */
public class ProjectFSXMLImpl extends ProjectXMLImpl implements ProjectFSXML {
    private final Collection<File> groovyFiles = new ArrayList<>();

    public ProjectFSXMLImpl(File folder, String id, String name) throws MalformedURLException, URISyntaxException {
        super(folder.toURI().toURL(), id, name);

        for (URL url : getScripsLocationsURLS()) {
            File groovy = new File(url.toURI().getPath());
            if (groovy.exists()) {
                File[] files = groovy.listFiles(File::isFile);
                if (files != null) {
                    groovyFiles.addAll(Arrays.asList(files));
                }
            }
        }
    }

    @Override
    public Collection<File> getGroovyFiles() {
        return groovyFiles;
    }

}
