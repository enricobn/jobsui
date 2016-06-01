package org.jobsui.core.ui;

import java.util.List;

/**
 * Created by enrico on 5/10/16.
 */
public interface UIWidget<T, C> {

    void setVisible(boolean visible);

    UIComponent<T,C> getComponent();

    void setValidationMessages(List<String> messages);

}
