package org.bef.core;

import org.bef.core.ui.UIContainer;
import rx.Observable;

import java.util.List;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameterDef<T> {

    String getKey();

    String getName();

    Class<T> getType();

    boolean isOptional();

    T getDefaultValue();

    Observable<T> addToUI(UIContainer container);

    List<JobParameterDef> getDependencies();

    List<String> validate(T value);

}
