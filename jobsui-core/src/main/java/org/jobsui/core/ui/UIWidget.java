package org.jobsui.core.ui;

import java.util.List;

/**
 * Created by enrico on 5/10/16.
 */
public interface UIWidget<C> {

    void setVisible(boolean visible);

    UIComponent<C> getComponent();

    void setValidationMessages(List<String> messages);

}
