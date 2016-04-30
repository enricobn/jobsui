package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIChoice<T> {

    Observable<T> getObservable();

    void setEnabled(boolean enable);

    T getSelectedItem();

    void setItems(T[] items);

}
