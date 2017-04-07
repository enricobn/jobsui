package org.jobsui.core.xml;

import java.io.File;
import java.util.Collection;

/**
 * Created by enrico on 4/6/17.
 */
public interface ProjectFSXML extends ProjectXML {

    Collection<File> getGroovyFiles();

    void setName(String s);
}
