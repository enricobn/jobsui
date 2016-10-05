package org.jobsui.core.ui;

import rx.Observable;

/**
 * Created by enrico on 5/2/16.
 */
public interface UIComponent<T,C> {

    Observable<T> getObservable();

    C getComponent();

    T getValue();

    void notifySubscribers();

    void setVisible(boolean visible);

    void setValue(T value);

    void setTitle(String label);

    void setEnabled(boolean enable);

}