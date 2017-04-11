package org.jobsui.core;

import java.net.URL;
import java.util.List;

/**
 * Created by enrico on 3/29/17.
 */
public interface JobsUIPreferences {

    List<OpenedItem> getLastOpenedItems();

    void registerOpenedProject(URL url, String name) throws Exception;

}
