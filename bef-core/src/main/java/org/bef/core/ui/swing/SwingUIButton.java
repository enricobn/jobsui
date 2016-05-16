package org.bef.core.ui.swing;

import org.bef.core.ui.UIButton;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIButton implements UIButton<JComponent> {
    private final JButton component = new JButton();
    private final Observable<Void> observable;

    public SwingUIButton() {
        observable = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                subscriber.onStart();
                component.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        subscriber.onNext(null);
                    }
                });
            }
        });
    }

    @Override
    public Observable<Void> getObservable() {
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

    public void setText(String title) {
        component.setText(title);
    }

    public String getText() {
        return component.getText();
    }

    @Override
    public Void getValue() {
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
    public void setValue(Void value) {
    }

    @Override
    public void setTitle(String label) {

    }

}
