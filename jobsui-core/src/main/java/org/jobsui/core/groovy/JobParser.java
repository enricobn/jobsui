package org.jobsui.core.groovy;

import org.jobsui.core.xml.JobXML;

import java.io.InputStream;

/**
 * Created by enrico on 4/5/17.
 */
public interface JobParser {

    JobXML parse(String id, InputStream inputStream) throws Exception;

}
