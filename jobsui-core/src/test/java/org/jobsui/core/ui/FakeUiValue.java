package org.jobsui.core.ui;

import rx.functions.Action1;

import java.io.Serializable;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUiValue< C> extends FakeUIComponent<C> implements UIValue<C> {
    private Serializable value;

    @Override
    public C getComponent() {
        return null;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public void setConverter(StringConverter<Serializable> converter) {

    }

    @Override
    public void setDefaultValue(Serializable value) {
        setValue(value);
    }

    @Override
    public void setValue(Serializable value) {
        this.value = value;
        for (Action1<Serializable> action1 : actions) {
            action1.call(value);
        }
    }
}
