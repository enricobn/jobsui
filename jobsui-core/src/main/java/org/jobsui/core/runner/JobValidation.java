package org.jobsui.core.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 3/13/17.
 */
public class JobValidation {
    private boolean valid = true;
    private List<String> messages = new ArrayList<>();

    public void invalidate() {
        valid = false;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public boolean isValid() {
        return valid && messages.isEmpty();
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
