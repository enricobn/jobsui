package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIChoice<T,C> extends UIComponent<T,C> {

    Observable<T> getObservable();

    void setEnabled(boolean enable);

    T getSelectedItem();

    void setItems(T[] items);

}
