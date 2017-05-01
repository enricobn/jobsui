package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 4/30/17.
 */
public interface WizardStep extends ValidatingXML {

    String getName();

    List<String> getDependencies();

    String getValidateScript();

    default List<String> validate() {
        List<String> messages = new ArrayList<>();
        if (getName() == null || getName().isEmpty()) {
            messages.add("Name is mandatory.");
        }

        if (getDependencies().isEmpty()) {
            messages.add("DependsOn is mandatory.");
        }
        return messages;
    }
}
