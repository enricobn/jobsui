package org.bef.core;

import org.bef.core.ui.UIComponent;
import rx.Observable;

import java.util.Map;

/**
 * Created by enrico on 5/1/16.
 */
public interface JobParameterDefUIComponent {

    UIComponent getComponent();

    /**
     *
     * @param values key = parameterDef key
     */
    void onDependenciesChange(Map<String,Object> values);
}
