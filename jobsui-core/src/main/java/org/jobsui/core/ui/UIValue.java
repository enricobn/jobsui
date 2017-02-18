package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIValue<T extends Serializable,C> extends UIComponent<T,C>{

    void setConverter(StringConverter<T> converter);

    void setDefaultValue(T value);

}
