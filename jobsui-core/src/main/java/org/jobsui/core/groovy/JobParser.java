package org.jobsui.core.groovy;

import org.jobsui.core.xml.ProjectXML;
import org.xml.sax.SAXException;

import java.io.File;

/**
 * Created by enrico on 3/22/17.
 */
public interface JobParser {

    static JobParser getParser(String projectRoot) throws SAXException {
        File folder = new File(projectRoot);
        return new JobParserImpl(folder);
    }

    ProjectXML parse() throws Exception;
}
