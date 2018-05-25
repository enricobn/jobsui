package org.jobsui.core.xml;

import java.io.File;
import java.util.List;

/**
 * Created by enrico on 4/6/17.
 */
public interface ProjectFSXML extends ProjectXML {

    List<String> getScriptFilesNames(String root);

    void setName(String name);

    String getScriptContent(String root, String name);

    void setScriptContent(String root, String name, String content);

    File getFolder();

    void setVersion(String version);

    void setFolder(File folder);

    void addScriptFile(String root, String name, String content);

    void removeScriptFile(String root, String name);

    void setId(String id);
}
