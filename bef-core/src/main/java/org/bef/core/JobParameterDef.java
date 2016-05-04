package org.bef.core;

import org.bef.core.ui.UI;
import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UnsupportedComponentException;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameterDef<T> {

    String getKey();

//    String getName();

    Class<T> getType();

    boolean isOptional();

    T getDefaultValue();

    UIComponent createComponent(UI ui) throws UnsupportedComponentException;

    List<JobParameterDef<?>> getDependencies();

    List<String> validate(T value);

    /**
     *
     * @param values key = parameterDef key
     */
    void onDependenciesChange(UIComponent component, Map<String,Object> values);

}
