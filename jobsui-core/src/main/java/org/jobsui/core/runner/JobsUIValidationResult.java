package org.jobsui.core.runner;

import java.util.List;

public interface JobsUIValidationResult {

    boolean isValid();

    List<String> getMessages();
}
