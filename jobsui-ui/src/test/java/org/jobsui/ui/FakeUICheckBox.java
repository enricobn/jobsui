package org.jobsui.ui;

import org.jobsui.core.ui.UICheckBox;

import java.io.Serializable;

/**
 * Created by enrico on 5/31/16.
 */
public class FakeUICheckBox extends FakeUIComponent implements UICheckBox<FakeComponent> {
    private Boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Serializable value) {
        this.value = (Boolean) value;
    }
}
