package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIValue<T> {

    Observable<T> getObservable();

    T getValue();

}
