package org.bef.core.ui;

import rx.Observable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIButton {

    Observable<Void> getObservable();

    void setEnabled(boolean enabled);
}
