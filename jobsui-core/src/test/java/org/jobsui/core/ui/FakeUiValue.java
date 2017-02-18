package org.jobsui.core.ui;

import rx.functions.Action1;

import java.io.Serializable;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUiValue<T extends Serializable, C> extends FakeUIComponent<T, C> implements UIValue<T, C> {
    private T value;

    @Override
    public C getComponent() {
        return null;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setConverter(StringConverter<T> converter) {

    }

    @Override
    public void setDefaultValue(T value) {
        setValue(value);
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        for (Action1<T> action1 : actions) {
            action1.call(value);
        }
    }
}
