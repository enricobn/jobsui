package org.jobsui.core.xml;

import java.net.URL;

/**
 * Created by enrico on 3/22/17.
 */
public interface ProjectParser {

//    static ProjectParser getParser(String projectRoot) throws Exception {
//        File folder = new File(projectRoot);
//        return new ProjectParserImpl(folder);
//    }

    ProjectFSXMLImpl parse(URL url) throws Exception;
}
