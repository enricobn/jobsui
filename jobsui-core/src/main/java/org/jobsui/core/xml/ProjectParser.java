package org.jobsui.core.xml;

import java.io.File;
import java.net.URL;

/**
 * Created by enrico on 3/22/17.
 */
public interface ProjectParser {

    ProjectFSXML parse(File folder) throws Exception;

    ProjectXML parse(URL url) throws Exception;

    SimpleProjectXML parseSimple(URL url) throws Exception;
}
