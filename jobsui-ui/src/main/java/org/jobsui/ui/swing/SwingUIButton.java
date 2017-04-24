package org.jobsui.ui.swing;

import org.jobsui.core.ui.UIButton;
import rx.Observable;

import javax.swing.*;
import java.io.Serializable;

/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIButton implements UIButton<JComponent> {
    private final JButton component = new JButton();
    private final Observable<Serializable> observable;

    public SwingUIButton() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            component.addActionListener(e -> subscriber.onNext(null));
        });
    }

    @Override
    public Observable<Serializable> getObservable() {
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
    public Serializable getValue() {
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
    public void setValue(Serializable value) {
    }

    @Override
    public void setTitle(String label) {
        component.setText(label);
    }

}
