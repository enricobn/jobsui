package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by enrico on 4/30/17.
 */
public interface WizardStep extends ValidatingXML {

    String getName();

    Set<String> getDependencies();

    String getValidateScript();

    void setName(String name);

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
