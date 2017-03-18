package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIValue<C> extends UIComponent<C>{

    void setConverter(StringConverter<Serializable> converter);

    void setDefaultValue(Serializable value);

}
