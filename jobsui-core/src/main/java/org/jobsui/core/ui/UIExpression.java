package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIExpression<T extends Serializable,C> extends UIComponent<T,C>{

    void setValue(T value);

}
