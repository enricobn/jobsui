package org.jobsui.core.xml;

import java.io.InputStream;

/**
 * Created by enrico on 4/5/17.
 */
public interface JobParser {

    JobXML parse(String id, InputStream inputStream) throws Exception;

}
