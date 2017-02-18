package org.jobsui.core.ui.swing;

import org.jobsui.core.SerializableVoid;
import org.jobsui.core.ui.UIButton;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;

/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIButton implements UIButton<JComponent> {
    private final JButton component = new JButton();
    private final Observable<SerializableVoid> observable;

    public SwingUIButton() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            component.addActionListener(e -> subscriber.onNext(null));
        });
    }

    @Override
    public Observable<SerializableVoid> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return component.isEnabled();
    }

    public JComponent getComponent() {
        return component;
    }

    @Override
    public SerializableVoid getValue() {
        return null;
    }

    @Override
    public void notifySubscribers() {
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(SerializableVoid value) {
    }

    @Override
    public void setTitle(String label) {
        component.setText(label);
    }

}
