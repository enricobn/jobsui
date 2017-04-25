package org.jobsui.core;

import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UnsupportedComponentException;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by enrico on 2/18/17.
 */
public interface JobParameterUI {

    <C> UIComponent<C> createComponent(UI<C> ui) throws UnsupportedComponentException;

    /**
     *
     * @param values key = parameterDef key
     */
    void onDependenciesChange(UIWidget widget, Map<String, Serializable> values);

    boolean isVisible();

}
