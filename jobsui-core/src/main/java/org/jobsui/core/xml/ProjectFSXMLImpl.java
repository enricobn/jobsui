package org.jobsui.core.xml;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/5/17.
 */
public class ProjectFSXMLImpl extends ProjectXMLImpl implements ProjectFSXML {
    private File folder;
    private final Map<String,Map<String,String>> scriptFiles = new HashMap<>();

    public ProjectFSXMLImpl(File folder, String id, String name, String version) throws MalformedURLException {
        super(folder.toURI().toURL(), id, name, version);
        this.folder = folder;

    }

    @Override
    public List<String> getScriptFilesNames(String root) {
        if (!scriptFiles.containsKey(root)) {
            return Collections.emptyList();
        }
        return scriptFiles.get(root).keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public String getScriptContent(String root, String name) {
        if (!scriptFiles.containsKey(root)) {
            return "";
        }
        String content = scriptFiles.get(root).get(name);
        if (content == null) {
            content = "";
        }
        return content;
    }

    @Override
    public void setScriptContent(String root, String name, String content) {
        Map<String, String> scripts = scriptFiles.computeIfAbsent(root, key -> new HashMap<>());
        scripts.put(name, content);
    }

    @Override
    public File getFolder() {
        return folder;
    }

    @Override
    public void setFolder(File folder) {
        this.folder = folder;
    }

    @Override
    public void addScriptFile(String root, String name, String content) {
        Map<String, String> map = scriptFiles.computeIfAbsent(root, key -> new HashMap<>());
        map.put(name, content);
    }

    @Override
    public void removeScriptFile(String root, String name) {
        Map<String, String> map = scriptFiles.get(root);
        if (map != null) {
            map.remove(name);
        }
    }

}
