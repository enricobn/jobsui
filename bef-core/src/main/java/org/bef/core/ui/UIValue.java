package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIValue<T,C> extends UIComponent<T,C>{

    Observable<T> getObservable();

    T getValue();

}
