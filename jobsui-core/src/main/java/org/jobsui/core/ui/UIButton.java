package org.jobsui.core.ui;

import org.jobsui.core.SerializableVoid;
import rx.Observable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIButton<C> extends UIComponent<SerializableVoid,C> {

    Observable<SerializableVoid> getObservable();

    void setEnabled(boolean enabled);

}
