package org.bef.core.ui;

/**
 * Created by enrico on 5/31/16.
 */
public class FakeUICheckBox<C> extends FakeUIComponent<Boolean,C> implements UICheckBox<C> {
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
    public void setValue(Boolean value) {
        this.value = value;
    }
}
