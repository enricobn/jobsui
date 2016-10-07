package org.jobsui.core;

import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UnsupportedComponentException;

import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameterDef<T> {

    String getKey();

    String getName();

    boolean isOptional();

    T getDefaultValue();

    <C> UIComponent<T, C> createComponent(UI<C> ui) throws UnsupportedComponentException;

    List<JobParameterDef<?>> getDependencies();

    List<String> validate(T value);

    /**
     *
     * @param values key = parameterDef key
     */
    void onDependenciesChange(UIWidget widget, Map<String,Object> values);

    boolean isVisible();
}
