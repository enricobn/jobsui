package org.jobsui.core.xml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by enrico on 4/5/17.
 */
class ProjectFSXMLImpl extends ProjectXMLImpl implements ProjectFSXML {
    private final File folder;

    ProjectFSXMLImpl(File folder, String id, String name) throws MalformedURLException, URISyntaxException {
        super(folder.toURI().toURL(), id, name);
        this.folder = folder;
    }

    @Override
    public Collection<File> getScriptFiles(String root) {
        File rootFolder = new File(folder, root);
        if (rootFolder.exists()) {
            File[] files = rootFolder.listFiles(File::isFile);
            if (files != null) {
                return Arrays.asList(files);
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public File getFolder() {
        return folder;
    }

}
