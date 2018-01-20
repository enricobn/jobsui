package org.jobsui.ui.common;

import org.jobsui.core.ui.StringConverter;
import org.jobsui.core.ui.StringConverterString;
import org.jobsui.core.ui.UIValue;

import java.io.Serializable;

public abstract class UIValueAbstract<C> implements UIValue<C> {
    private StringConverter<Serializable> converter = new StringConverterString();

    @Override
    public void setConverter(StringConverter<Serializable> converter) {
        this.converter = converter;
    }

    public StringConverter<Serializable> getConverter() {
        return converter;
    }
}
