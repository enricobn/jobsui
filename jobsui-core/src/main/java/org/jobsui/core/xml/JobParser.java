package org.jobsui.core.xml;

import java.net.URL;

/**
 * Created by enrico on 4/5/17.
 */
public interface JobParser {

    JobXML parse(String id, URL url) throws Exception;

}
