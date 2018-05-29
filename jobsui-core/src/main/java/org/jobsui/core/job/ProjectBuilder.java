package org.jobsui.core.job;

import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.xml.ProjectXML;

public interface ProjectBuilder {

    Project build(ProjectXML projectXML, BookmarksStore bookmarksStore) throws Exception;

}
