package org.jobsui.core.ui;

import org.jobsui.core.ObservableProducer;

import java.io.Serializable;

/**
 * Created by enrico on 5/2/16.
 */
public interface UIComponent<C> extends ObservableProducer {

    C getComponent();

    Serializable getValue();

    void notifySubscribers();

    void setVisible(boolean visible);

    void setValue(Serializable value);

    void setTitle(String label);

    void setEnabled(boolean enable);

}
