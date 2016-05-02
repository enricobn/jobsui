package org.bef.core;

import rx.Observable;

import java.util.Map;

/**
 * Created by enrico on 5/1/16.
 */
public interface JobParameterDefUIComponent<T> {

    Observable<T> getObservable();

    /**
     *
     * @param values key = parameterDef key
     */
    void onDependenciesChange(Map<String,Object> values);
}
