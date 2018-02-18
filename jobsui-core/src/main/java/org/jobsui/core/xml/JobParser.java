package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentRegistry;

import java.net.URL;

/**
 * Created by enrico on 4/5/17.
 */
public interface JobParser {

    JobXML parse(String id, URL url, UIComponentRegistry uiComponentRegistry) throws Exception;

}
