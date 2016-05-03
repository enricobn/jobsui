package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 5/2/16.
 */
public interface UIComponent<T,C> {

    Observable<T> getObservable();

//    void setProperty(String key, Object value) throws InvalidpropertyException;
//
//    Object getProperty(String key) throws InvalidpropertyException;

    C getComponent();

}
