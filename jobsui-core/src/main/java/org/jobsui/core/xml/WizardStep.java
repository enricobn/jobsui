package org.jobsui.core.xml;

import java.util.List;

/**
 * Created by enrico on 4/30/17.
 */
public interface WizardStep {

    String getName();

    List<String> getDependencies();

    String getValidateScript();

}
