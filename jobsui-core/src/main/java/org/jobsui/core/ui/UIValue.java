package org.jobsui.core.ui;

/**
 * Created by enrico on 4/29/16.
 */
public interface UIValue<T,C> extends UIComponent<T,C>{

    void setConverter(StringConverter<T> converter);

    void setDefaultValue(T value);

}
