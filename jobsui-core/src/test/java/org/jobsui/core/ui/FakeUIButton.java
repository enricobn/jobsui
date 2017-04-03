package org.jobsui.core.ui;

import rx.functions.Action1;

import java.io.Serializable;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIButton<C> extends FakeUIComponent<C> implements UIButton<C> {

    @Override
    public void setEnabled(boolean enabled) {

    }

    public void click() {
        for (Action1<Serializable> action : actions) {
            action.call(null);
        }
    }

    @Override
    public Serializable getValue() {
        return null;
    }

    @Override
    public void setValue(Serializable value) {
    }
}
