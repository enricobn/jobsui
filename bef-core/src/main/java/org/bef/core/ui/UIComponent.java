package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 5/2/16.
 */
public interface UIComponent<T,C> {

    Observable<T> getObservable();

    C getComponent();

}
