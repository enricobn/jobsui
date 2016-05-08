package org.bef.core.ui;

import rx.functions.Action1;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIButton<C> extends FakeUIComponent<Void,C> implements UIButton<C> {

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setText(String add) {

    }

    @Override
    public C getComponent() {
        return null;
    }

    public void click() {
        for (Action1<Void> action : actions) {
            action.call(null);
        }
    }

    @Override
    public Void getValue() {
        return null;
    }
}
