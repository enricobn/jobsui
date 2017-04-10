package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by enrico on 4/5/17.
 */
class ProjectFSXMLImpl extends ProjectXMLImpl implements ProjectFSXML {
    private File folder;
    private Map<String,Map<String,String>> scriptFiles = new HashMap<>();

    ProjectFSXMLImpl(File folder, String id, String name) throws MalformedURLException, URISyntaxException {
        super(folder.toURI().toURL(), id, name);
        this.folder = folder;

    }

    public void afterLoad() throws Exception {
        Charset utf8 = Charset.forName("UTF-8");

        for (String root : getScriptsLocations()) {
            File rootFolder = new File(folder, root);
            Map<String,String> scripts = new HashMap<>();
            scriptFiles.put(root, scripts);
            if (rootFolder.exists()) {
                File[] files = rootFolder.listFiles(File::isFile);
                if (files != null) {
                    for (File file : files) {
                        scripts.put(file.getName(), FileUtils.readFileToString(file, utf8));
                    }
                }
            }
        }
    }

    @Override
    public Map<String, String> getScriptFiles(String root) {
        return scriptFiles.get(root);
    }

    @Override
    public File getFolder() {
        return folder;
    }

    @Override
    public void setFolder(File folder) {
        this.folder = folder;
    }

}
