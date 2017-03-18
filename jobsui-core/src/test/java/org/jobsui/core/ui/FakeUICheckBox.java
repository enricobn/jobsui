package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 5/31/16.
 */
public class FakeUICheckBox<C> extends FakeUIComponent<C> implements UICheckBox<C> {
    private Boolean value;

    @Override
    public C getComponent() {
        return null;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Serializable value) {
        this.value = (Boolean) value;
    }
}
