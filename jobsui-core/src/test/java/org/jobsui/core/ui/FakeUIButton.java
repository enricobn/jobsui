package org.jobsui.core.ui;

import org.jobsui.core.SerializableVoid;
import rx.functions.Action1;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIButton<C> extends FakeUIComponent<SerializableVoid,C> implements UIButton<C> {

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public C getComponent() {
        return null;
    }

    public void click() {
        for (Action1<SerializableVoid> action : actions) {
            action.call(null);
        }
    }

    @Override
    public SerializableVoid getValue() {
        return null;
    }

    @Override
    public void setValue(SerializableVoid value) {
    }
}
