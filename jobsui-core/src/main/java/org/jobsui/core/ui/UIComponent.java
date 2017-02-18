package org.jobsui.core.ui;

import rx.Observable;

import java.io.Serializable;

/**
 * Created by enrico on 5/2/16.
 */
public interface UIComponent<T extends Serializable,C> {

    Observable<T> getObservable();

    C getComponent();

    T getValue();

    void notifySubscribers();

    void setVisible(boolean visible);

    void setValue(T value);

    void setTitle(String label);

    void setEnabled(boolean enable);

}
