package org.jobsui.core.ui;

import rx.Observable;

import java.io.Serializable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIButton<C> extends UIComponent<C> {

    Observable<Serializable> getObservable();

    void setEnabled(boolean enabled);

}
