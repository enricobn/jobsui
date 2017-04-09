package org.jobsui.core.xml;

import java.io.File;
import java.util.Map;

/**
 * Created by enrico on 4/6/17.
 */
public interface ProjectFSXML extends ProjectXML {

    Map<String, String> getScriptFiles(String root);

    void setName(String name);

    File getFolder();

    void setVersion(String version);

    void setFolder(File folder);
}
