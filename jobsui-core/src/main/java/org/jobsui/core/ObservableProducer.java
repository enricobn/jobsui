package org.jobsui.core;

import rx.Observable;

import java.io.Serializable;

/**
 * Created by enrico on 3/8/17.
 */
public interface ObservableProducer<T extends Serializable> {

    Observable<T> getObservable();

}
